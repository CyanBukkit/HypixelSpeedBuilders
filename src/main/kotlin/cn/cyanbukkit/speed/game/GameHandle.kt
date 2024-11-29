package cn.cyanbukkit.speed.game

import cn.cyanbukkit.dragon.movement.DragonCircularMovement
import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.cc
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.checkTask
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.api.event.PlayerEliminateEvent
import cn.cyanbukkit.speed.command.ForceCommand
import cn.cyanbukkit.speed.data.ArenaIslandData
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.data.ScoreData
import cn.cyanbukkit.speed.game.GameVMData.backLobby
import cn.cyanbukkit.speed.game.GameVMData.build_second
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.GameVMData.dragon
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.game.GameVMData.lifeIsLand
import cn.cyanbukkit.speed.game.GameVMData.mapList
import cn.cyanbukkit.speed.game.GameVMData.needBuild
import cn.cyanbukkit.speed.game.GameVMData.nowMap
import cn.cyanbukkit.speed.game.GameVMData.playerBindIsLand
import cn.cyanbukkit.speed.game.GameVMData.playerStatus
import cn.cyanbukkit.speed.game.GameVMData.radius
import cn.cyanbukkit.speed.game.GameVMData.spectator
import cn.cyanbukkit.speed.game.GameVMData.storage
import cn.cyanbukkit.speed.game.GameVMData.templateList
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import cn.cyanbukkit.speed.game.task.GameCheckTask
import cn.cyanbukkit.speed.game.task.GameLoopTask
import cn.cyanbukkit.speed.game.task.GameLoopTask.playerTimeStatus
import cn.cyanbukkit.speed.scoreboard.BoardManager
import cn.cyanbukkit.speed.scoreboard.impl.DefaultBoardAdapter
import cn.cyanbukkit.speed.utils.*
import org.bukkit.*
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


object GameHandle {


    fun init() {
        if (mapList.isEmpty() || templateList.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(
                """
                找不到地图，请配置地图！谢谢。 ^ _ ^
                啥啥没有你想让我开啥啊？
                就算啥也不懂的都知道没有东西给你开个pi
                绑图绑模板去
            """.trimIndent()
            )
            return
        }
        val mapData = mapList[mapList.keys.random()]!! // 随机选择一个地图
        BoardManager(SpeedBuildReloaded.instance, DefaultBoardAdapter(mapData)) // 注册计分板
        PlayerListener().register() // 注册玩家监听事件
        BlockListener().register() // 注册方块监听事件
        ForceCommand.register() // 注册强制开始命令
        nowMap = mapData // 与插件进行地图绑定
        gameStatus = GameStatus.WAITING  // 放置等待模式标签
        val taskId = Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, GameLoopTask, 0, 20L).taskId
        GameVMData.nowTask = taskId
        Bukkit.getPluginManager().callEvent(GameChangeEvent(mapData, GameStatus.WAITING))
        Bukkit.getConsoleSender().sendMessage(mapData.worldName + "地图已经加载完毕！") // 通知控制台
    }


    /**
     * 开始游戏
     */
    fun startGame(playerList: MutableList<Player>, arena: ArenaSettingData) {
        gameStatus = GameStatus.STARTING  // 放置等待模式标签
        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GameStatus.STARTING))
        // 分队处理开始游戏部分
        playerList.forEach { p ->
            playerStatus[p] = PlayerStatus.LIFE
        }
        configSettings!!.gameRuleText.forEach {
            Bukkit.getOnlinePlayers().forEach { p ->
                p.sendMessage(it.cc(p))
            }
        }
        // 匹配机制
        // 打乱玩家列表
        playerList.shuffle()
        // 创建一个映射来跟踪每个岛的玩家数量
        val islandPlayerCount = mutableMapOf<ArenaIslandData, Int>()
        // 初始化映射
        arena.islandData.forEach { island ->
            islandPlayerCount[island] = 0
        }
        // 为每个玩家分配一个岛
        lifeIsLand.clear()
        playerList.forEach { player ->
            // 找到一个还没有达到玩家限制的岛
            val island = arena.islandData.find { island ->
                islandPlayerCount[island]!! < arena.isLandPlayerLimit
            }
            // 如果找到了岛，将玩家分配到这个岛，并更新岛的玩家数量
            if (island != null) {
                playerBindIsLand[player] = island
                // 放置岛屿
                player.useIsland(island.middleBlock, storage.getNowIslandTemplate(player))
                if (!lifeIsLand.contains(island)) lifeIsLand.add(island)
                islandPlayerCount[island] = islandPlayerCount[island]!! + 1
            }
            player.inventory.clear()
        }
        checkTask[SpeedBuildReloaded.instance] = Bukkit.getScheduler().scheduleSyncRepeatingTask(SpeedBuildReloaded.instance, GameCheckTask(arena), 20L, 20L)
        playerList.forEach {
            it.teleport(playerBindIsLand[it]!!.playerSpawn)
            it.gameMode = GameMode.SURVIVAL
            it.allowFlight = true
        }
        val dragon = arena.middleIsland.world.spawnEntity(arena.middleIsland, EntityType.ENDER_DRAGON) as EnderDragon
        dragon.isCustomNameVisible = true
        val dragonCraft = (dragon as CraftEnderDragon).handle
        dragonCraft.customName = "§c§lJudge Dragon"
        GameVMData.dragon = DragonCircularMovement(
            SpeedBuildReloaded.instance,
            dragonCraft,
            arena.middleIsland,
            height = 30.0,
        )
        GameVMData.dragon.startMoving()
    }

    /**
     * 结算方法
     */
    fun stopGame(arena: ArenaSettingData) {
        // 停线程
        Bukkit.getScheduler().cancelTask(GameVMData.nowTask)
        val settlementMessage = configSettings!!.mess.settlement
        gameStatus = GameStatus.END
        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GameStatus.END))
        // 根据allscoreData最大的前三个排列
        val scoreList = GameVMData.allScoreData.toList().sortedByDescending { (_, value) -> value }
        GameVMData.first_player = try {
            scoreList[0].first
        } catch (e: Exception) {
            null
        }
        GameVMData.second_player = try {
            scoreList[1].first
        } catch (e: Exception) {
            null
        }
        GameVMData.third_player = try {
            scoreList[2].first
        } catch (e: Exception) {
            null
        }

        val first = if (GameVMData.first_player != null) {
            GameVMData.first_player!!.name
        } else {
            "无"
        }
        val second = if (GameVMData.second_player != null) {
            GameVMData.second_player!!.name
        } else {
            "无"
        }
        val third = if (GameVMData.third_player != null) {
            GameVMData.third_player!!.name
        } else {
            "无"
        }

        val first_player_score = if (GameVMData.first_player != null) {
            GameVMData.allScoreData[GameVMData.first_player!!]
        } else {
            0
        }

        val second_player_score = if (GameVMData.second_player != null) {
            GameVMData.allScoreData[GameVMData.second_player!!]
        } else {
            0
        }

        val third_player_score = if (GameVMData.third_player != null) {
            GameVMData.allScoreData[GameVMData.third_player!!]
        } else {
            0
        }
        // 内容
        settlementMessage.forEach { s ->
            Bukkit.getOnlinePlayers().forEach {
                val ss = s.replace("%first_player%", first).replace("%second_player%", second)
                    .replace("%third_player%", third).replace("%first_player_score%", first_player_score.toString())
                    .replace("%second_player_score%", second_player_score.toString())
                    .replace("%third_player_score%", third_player_score.toString()).cc(it)
                it.sendMessage(ss)
                Title.title(it, configSettings!!.mess.end, "")
            }
        }
        // 传送并关服
        Bukkit.broadcastMessage("§630秒后关闭服务器")
        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
            // 所有传送到
            val lobbyServer = configSettings!!.endReturnToTheLobby
            if (lobbyServer.isNotEmpty()) {
                Bukkit.getOnlinePlayers().forEach {
                    it.connectTo(lobbyServer, SpeedBuildReloaded.instance)
                }
            }
            Bukkit.shutdown()
        }, 30 * 20L)
    }

    /**
     * 淘汰
     */
    private fun eliminate(lowestScoringPlayer: Player) {
        Bukkit.getOnlinePlayers().forEach {
            Title.title(it, "", configSettings!!.mess.eliminate.replace("%player%", lowestScoringPlayer.name))
        }
        playerStatus[lowestScoringPlayer] = PlayerStatus.OUT
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
            //DragonLookAtPlayer(Teacher.getBukkitEntity(), lowestScoringPlayer)
            /////////////////////////////
            // 带时候模板被龙炸开        //
            /////////////////////////////
            // 根据放置的worldedit的区域获取方块
            val island = playerBindIsLand[lowestScoringPlayer]!!
            dragon.eatIsland(island)
            Bukkit.getPluginManager().callEvent(PlayerEliminateEvent(lowestScoringPlayer, island.middleBlock))
            //将淘汰的玩家设置在旁观者集合里
            spectator.add(lowestScoringPlayer)
            lowestScoringPlayer.onSpectator()
            // 给观察者模式的物品
        }, 0)
        storage.addEliminate(lowestScoringPlayer)
    }


    fun worldEditRegion(isl: ArenaIslandData) {
        val blockList = mutableListOf<Block>()

        xx@for (x in -radius until radius) {
            zz@for (z in -radius until radius) {
                yy@for (y in 128 downTo -128) {
                    val block = isl.middleBlock.getRelative(x, y, z)
                    if (block.type == Material.AIR) continue
                    blockList.add(block)
                }
            }
        }
        blockList.boom(isl.middleBlock)
        lifeIsLand.remove(isl)
    }

    //实现玩家旁观者模式
    private fun Player.onSpectator() {
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance,{
            this.inventory.clear()
            this.inventory.armorContents = null
            this.spigot().collidesWithEntities = false
            this.foodLevel = 20
            this.maxHealth = 20.0
            this.health = 20.0
            this.fireTicks = 0
            this.level = 0
            this.exp = 0.0f
            this.gameMode = GameMode.SURVIVAL
            this.allowFlight = true
            this.isFlying = true
            this.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY,100000,255))
            this.giveItem()
        },10L)
    }

    //给予旁观者物品
    private fun Player.giveItem() {
        val compass = ItemBuilder(Material.COMPASS).amount(1).name("&a传送器")
        val setting = ItemBuilder(Material.DIODE).amount(1).name("&a旁观者设置")

        this.inventory.setItem(0, compass.build())
        this.inventory.setItem(4, setting.build())
        this.inventory.setItem(8, backLobby)
    }


    private fun getScoreColor(score: Int): String {
        when (score) {
            in 0..20 -> {
                return "§4"
            }

            in 21..40 -> {
                return "§c"
            }

            in 41..60 -> {
                return "§6"
            }

            in 61..80 -> {
                return "§e"
            }

            in 81..100 -> {
                return "§a"
            }

            else -> {
                return "§1"
            }
        }
    }


    fun likeHypixel(liftPlayerList: MutableList<Player>, round: Int) {
        val scoreMap = mutableMapOf<Player, ScoreData>()
        liftPlayerList.forEach { life ->
            val island = playerBindIsLand[life]!!
            val score = needBuild[island]!!.compare(island.middleBlock)
            if (score == 100) {
                build_second[life] = (System.currentTimeMillis() - playerTimeStatus[life]!!) / 1000.0
                storage.addRestoreBuild(life)
            }
            scoreMap[life] = ScoreData(score, if (build_second.containsKey(life)) build_second[life]!! else 0.0)
            Title.title(life, configSettings!!.mess.score.replace("%score%", score.toString()), "")
            // Update total scores
            if (GameVMData.allScoreData.contains(life)) {
                GameVMData.allScoreData[life] = GameVMData.allScoreData[life]!! + score
            } else {
                GameVMData.allScoreData[life] = score
            }
        }
        // Display scores (unchanged)
        val successMaxMin = scoreMap.toList()
            .sortedByDescending { (_, value) -> value.success }
            .toMap()
            .toMutableMap()
        val timeMaxMin = scoreMap.toList()
            .sortedByDescending { (_, value) -> value.useTime }
            .toMap()
            .toMutableMap()

        // Display scores to players (unchanged)
        Bukkit.getOnlinePlayers().forEach {
            var text = """
        §a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r
                                      §6Round $round Stats §r
    """.trimIndent()
            successMaxMin.forEach { (player, scoreData) ->
                text += "\n ${
                    configSettings!!.mess.roundFormat.replace(
                        "%success%",
                        "${getScoreColor(scoreData.success)}${scoreData.success}%§r"
                    ).replace("%second%", if (scoreData.useTime == 0.0) "" else "§6(${scoreData.useTime}s)§r")
                        .cc(player)
                } \n"
            }
            text += "§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬§r"
            it.sendMessage(text)
        }

        // Fixed elimination logic
        Bukkit.getScheduler().runTaskAsynchronously(SpeedBuildReloaded.instance) {
            var minusPlayer1: Player? = null
            var minusPlayer2: Player? = null
            try {
                if (scoreMap.values.all { it.success == 100 }) {
                    // timeMaxMin 的第一
                    val slowestPlayer = timeMaxMin.toList().reversed()[0].first
                    minusPlayer1 = slowestPlayer
                    if (scoreMap.size > 1) {
                        // timeMaxMin 的第二
                        val secondSlowestPlayer = timeMaxMin.toList().reversed()[1].first
                        minusPlayer2 = secondSlowestPlayer
                    }
                } else {
                    // successMaxMin 的最后一个
                    val lowestScoringPlayer = successMaxMin.toList().last().first
                    minusPlayer1 = lowestScoringPlayer
                    scoreMap.remove(minusPlayer1)
                    if (scoreMap.size > 1) {
                        // successMaxMin 的倒数第二个
                        val secondLowestScoringPlayer = successMaxMin.toList().last().first
                        minusPlayer2 = secondLowestScoringPlayer
                    }
                }

                // Perform eliminations
                if (scoreMap.size > 2) {
                    eliminate(minusPlayer1)
                    if (minusPlayer2 != null) {
                        eliminate(minusPlayer2)
                    }
                } else {
                    // In case of final rounds, eliminate the lowest scoring player
                    eliminate(minusPlayer1)
                }
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
    }



    /**
     * 对比的方法并屏分 满分100分
     */
    private fun List<TemplateBlockData>.compare(middle: Block): Int {
        val simpleScore = 100.0 / this.size
        var score = 100.0
        this.forEach {
            val block = middle.getRelative(
                it.x, it.y, it.z
            )
            if (block.type.name != it.type || !isMatchingBlock(block, it.data)) {
                score -= simpleScore
            }
        }
        return score.toInt()
    }




    private fun isMatchingBlock(block: Block, data: String): Boolean {
        val da = data.split(":")
        return if (block is Banner) {
            (block.state.rawData == da[1].toByte()) && (when (da[2].toInt()) {
                0 -> block.baseColor == DyeColor.WHITE
                1 -> block.baseColor == DyeColor.ORANGE
                2 -> block.baseColor == DyeColor.MAGENTA
                3 -> block.baseColor == DyeColor.LIGHT_BLUE
                4 -> block.baseColor == DyeColor.YELLOW
                5 -> block.baseColor == DyeColor.LIME
                6 -> block.baseColor == DyeColor.PINK
                7 -> block.baseColor == DyeColor.GRAY
                9 -> block.baseColor == DyeColor.CYAN
                10 -> block.baseColor == DyeColor.PURPLE
                11 -> block.baseColor == DyeColor.BLUE
                12 -> block.baseColor == DyeColor.BROWN
                13 -> block.baseColor == DyeColor.GREEN
                14 -> block.baseColor == DyeColor.RED
                15 -> block.baseColor == DyeColor.BLACK
                else -> false
            })
        } else if (block.state is Skull) {
            val skull = block.state as Skull
            (block.data == da[2].toByte()) && (when (da[1].toInt()) {
                0 -> skull.skullType == SkullType.SKELETON
                1 -> skull.skullType == SkullType.WITHER
                2 -> skull.skullType == SkullType.ZOMBIE
                3 -> skull.skullType == SkullType.PLAYER
                4 -> skull.skullType == SkullType.CREEPER
                else -> false
            }) && skull.rotation == org.bukkit.block.BlockFace.valueOf(da[3]) && (skull.owner == da[4])
        } else {
            block.state.rawData == da[0].toByte()
        }
    }

    /**
     * 根据模板判断是不是成功
     */
    fun isSuccessPlace(temp: ArenaIslandData): Boolean {
        needBuild[temp]!!.forEach {
            val block = temp.middleBlock.getRelative(it.x, it.y, it.z)
            if (block.type != Material.getMaterial(it.type)
                || !isMatchingBlock(block, it.data)
            ) {
                return false
            }
        }
        return true
    }


}
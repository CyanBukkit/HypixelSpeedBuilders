package cn.cyanbukkit.speed.game

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.cc
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.checkTask
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.command.ForceCommand
import cn.cyanbukkit.speed.data.ArenaIslandData
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.game.GameVMData.lifeIsLand
import cn.cyanbukkit.speed.game.GameVMData.mapList
import cn.cyanbukkit.speed.game.GameVMData.nowMap
import cn.cyanbukkit.speed.game.GameVMData.playerStatus
import cn.cyanbukkit.speed.game.GameVMData.spectator
import cn.cyanbukkit.speed.game.GameVMData.storage
import cn.cyanbukkit.speed.game.GameVMData.templateList
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import cn.cyanbukkit.speed.game.task.EnderDragonTask
import cn.cyanbukkit.speed.game.task.GameCheckTask
import cn.cyanbukkit.speed.game.task.GameLoopTask
import cn.cyanbukkit.speed.scoreboard.BoardManager
import cn.cyanbukkit.speed.scoreboard.impl.DefaultBoardAdapter
import cn.cyanbukkit.speed.utils.*
import net.minecraft.server.v1_8_R3.NBTTagCompound
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.atan2


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
        val nms = if (mapData.enableElderGuardian) {
            mapData.middleIsland.createEntity()
        } else {
            null
        }
        val taskId =
            Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, GameLoopTask(mapData, nms), 0, 20L).taskId
        GameVMData.nowTask = taskId
        Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, EnderDragonTask(nms), 0, 1)
        Bukkit.getPluginManager().callEvent(GameChangeEvent(mapData, GameStatus.WAITING))
        Bukkit.getConsoleSender().sendMessage(mapData.worldName + "地图已经加载完毕！") // 通知控制台
    }


    /**
     * 开始游戏
     */
    fun startGame(playerList: MutableList<Player>, arena: ArenaSettingData) {
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
                GameVMData.playerBindIsLand[player] = island
                if (!lifeIsLand.contains(island)) lifeIsLand.add(island)
                islandPlayerCount[island] = islandPlayerCount[island]!! + 1
            }
            player.inventory.clear()
        }
        checkTask[SpeedBuildReloaded.instance] =
            Bukkit.getScheduler().scheduleSyncRepeatingTask(SpeedBuildReloaded.instance, GameCheckTask(arena), 20L, 20L)
        gameStatus = GameStatus.STARTING  // 放置等待模式标签
        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GameStatus.STARTING))
        playerList.forEach {
            it.teleport(GameVMData.playerBindIsLand[it]!!.playerSpawn)
            it.gameMode = GameMode.SURVIVAL
            it.allowFlight = true
        }
    }

    /**
     * 结算方法
     * TODO: 与Hyp一样的结束
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
        // 所有传送到
        val lobbyServer = configSettings!!.endReturnToTheLobby
        if (lobbyServer.isNotEmpty()) {
            Bukkit.getOnlinePlayers().forEach {
                it.connectTo(lobbyServer, SpeedBuildReloaded.instance)
            }
        }
        // 关服
        Bukkit.broadcastMessage("§630秒后关闭服务器")
        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
            Bukkit.shutdown()
        }, 30 * 20L)
    }

    /**
     * 淘汰
     */
    private fun eliminate(lowestScoringPlayer: Player, teacher: Teacher?) {
        Bukkit.getOnlinePlayers().forEach {
            Title.title(
                it, "", configSettings!!.mess.eliminate.replace("%player%", lowestScoringPlayer.name)
            )
        }
        playerStatus[lowestScoringPlayer] = PlayerStatus.OUT
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
            if (teacher != null) {
                object : BukkitRunnable() {
                    override fun run() {
                        if (teacher.bukkitEntity.isDead) {
                            cancel()
                            return
                        }
                        DragonLookAtPlayer(teacher, lowestScoringPlayer)
                    }
                }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 1)
                lowestScoringPlayer.sendRay(teacher)
                // 爆炸效果
                //note 小徒弟真的帅~
            } else {
                Bukkit.getConsoleSender().sendMessage("§b[SpeedBuild]§6未找到 NMS 实现类")
            }
            // TODO：带时候模板被龙炸开
            /////////////////////////////
            lifeIsLand.remove(GameVMData.playerBindIsLand[lowestScoringPlayer])
            //将淘汰的玩家设置在旁观者集合里
            spectator.add(lowestScoringPlayer)
            lowestScoringPlayer.onSpectator()
            // 给观察者模式的物品
        }, 0)
        storage.addEliminate(lowestScoringPlayer)
    }


    //实现玩家旁观者模式
    private fun Player.onSpectator() {
        //隐藏玩家碰撞箱
        this.spigot().collidesWithEntities = false
        this.health = 20.0
        this.foodLevel = 20
        this.exp = 0.0f
        this.level = 0
        this.inventory.clear()
        this.gameMode = GameMode.SURVIVAL
        this.giveItem()
    }

    //给予旁观者物品
    private fun Player.giveItem() {
        val compass = ItemBuilder(Material.COMPASS).amount(1).name("&a传送器")
        val setting = ItemBuilder(Material.DIODE).amount(1).name("&a旁观者设置")
        val back = ItemBuilder(Material.BED).amount(1).name("&c返回大厅")

        this.inventory.setItem(0, compass.build())
        this.inventory.setItem(4, setting.build())
        this.inventory.setItem(8, back.build())
    }

    //实现EnderDragon冲向玩家
    fun DragonLookAtPlayer(dragon: Teacher, player: Player) {
        val dragonLocation = dragon.bukkitEntity.location
        val playerLocation = player.location
        val direction = playerLocation.direction.multiply(0.5) // 计算方向并设置速度

        // 计算需要旋转的角度
        val yawDifference = playerLocation.yaw - dragonLocation.yaw
        val locationYaw = if (yawDifference > 180) yawDifference - 360 else yawDifference

        // 旋转EnderDragon以面向玩家
        dragon.rotateGuardian(locationYaw)

        // 设置EnderDragon的速度
        dragon.bukkitEntity.velocity = direction
    }

    private fun Player.sendRay(teacher: Teacher) {
        val nbt = NBTTagCompound()
        teacher.c(nbt)
        nbt.setInt("NoAI", 0)
        teacher.f(nbt)
        val packet = PacketPlayOutEntityMetadata(
            teacher.id, teacher.upDataWatch(this), false
        )
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.playerConnection.sendPacket(packet)
        }
        teacher.c(nbt)
        nbt.setInt("NoAI", 1)
        teacher.f(nbt)
    }

    @Deprecated("旧版的")
    fun score(liftPlayerList: MutableList<Player>, nowBuildTarget: String, teacher: Teacher?) { // 评分
        val scoreMap = mutableMapOf<Player, Int>() // 获取分数低开始处决
        liftPlayerList.forEach { p ->
            val score = templateList[nowBuildTarget]!!.compare(GameVMData.playerBindIsLand[p]!!.middleBlock)
            // 如果分数等于100 添加一个ReStore分数
            if (score == 100) {
                storage.addRestoreBuild(p)
            }
            scoreMap[p] = score
            Title.title(
                p, configSettings!!.mess.score.replace("%score%", score.toString()), ""
            )
            // 做总的计分
            if (GameVMData.allScoreData.contains(p)) {
                GameVMData.allScoreData[p] = GameVMData.allScoreData[p]!! + score
            } else {
                GameVMData.allScoreData[p] = score
            }
        }
        // 告知本回合所有分数
        val lowestScoringPlayerEntry = scoreMap.minByOrNull { it.value }
        val lowestScoringPlayer = lowestScoringPlayerEntry?.key
        // 如果每个人都是100分则不进行淘汰
        if (scoreMap.values.all { it == 100 }) {
            Bukkit.getOnlinePlayers().forEach {
                it.playSound(it.location, Sound.LEVEL_UP, 1f, 1f)
                Title.title(it, "§b很好", "§b这个回合都是100分没有被淘汰继续努力!")
            }
            return
        } else {
            // 通知所有玩家
            Bukkit.getOnlinePlayers().forEach { p ->
                p.sendMessage("§6本回合得分情况:")
                p.sendMessage(scoreMap.entries.joinToString("\n") {
                    " §a${it.key.name} §6得分: §a${it.value} \n"
                })
            }
        }
        // 分数无全部满分
        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
            if (lowestScoringPlayer != null) {
                eliminate(lowestScoringPlayer, teacher)
            }
        }, 40)
    }


    /**
     * 清理监考老师
     */
    fun clearWatch(nms: Teacher) {
        val nbt = NBTTagCompound()
        nms.c(nbt)
        nbt.setInt("NoAI", 0)
        nms.f(nbt)
        val packet = PacketPlayOutEntityMetadata(
            nms.id, nms.clearWatch(), false
        )
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.playerConnection.sendPacket(packet)
        }
        nms.bukkitEntity.teleport(nowMap.middleIsland)
        nms.c(nbt)
        nbt.setInt("NoAI", 1)
        nms.f(nbt)
    }

    /**
     * 对比的方法并屏分 满分100分
     */
    fun List<TemplateBlockData>.compare(middle: Block): Int {
        val simpleScore = 100.0 / this.size
        var score = 100.0
        this.forEach {
            val block = middle.getRelative(
                it.x, it.y, it.z
            )
            if (block.type.name != it.type || block.data != it.data.toByte()) {
                score -= simpleScore
            }
        }
        return score.toInt()
    }


    /**
     * 生成老师
     */
    private fun Location.createEntity(): Teacher {
        val cWorld = this.world as org.bukkit.craftbukkit.v1_8_R3.CraftWorld
        val nmsWorld = cWorld.handle
        val w = Teacher(nmsWorld)
        EntityTypes.GUARDIAN.spawnEntity(w, this)
        // 设置无重力
        w.noclip = true
        clearWatch(w)
        return w
    }


    /**
     * 根据中心点计算 另一个位置位于中心点的那个yaw
     */
    fun Location.getYaw(data: ArenaSettingData): Float {
        val middle = data.middleIsland.block
        val x = this.x - middle.x
        val z = this.z - middle.z
        val yaw = Math.toDegrees(atan2(x, z)).toFloat()
        return if (yaw < 0) yaw + 360 else yaw
    }



    /**
     * 根据模板判断是不是成功
     */
    fun isSuccessPlace(temp: List<TemplateBlockData>, middle: Block): Boolean {
        temp.forEach {
            val block = middle.getRelative(it.x, it.y, it.z)
            if (block.type != Material.getMaterial(it.type) || block.data != it.data.toByte()) {
                return false
            }
        }
        return true
    }



}
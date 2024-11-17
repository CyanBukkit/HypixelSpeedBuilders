package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.cc
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.checkTask
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.command.ForceCommand
import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.GameHandle.compare
import cn.cyanbukkit.speed.game.GameHandle.createEntity
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.game.LoaderData.nowMap
import cn.cyanbukkit.speed.scoreboard.BoardManager
import cn.cyanbukkit.speed.scoreboard.impl.DefaultBoardAdapter
import cn.cyanbukkit.speed.utils.Teacher
import cn.cyanbukkit.speed.utils.Title
import cn.cyanbukkit.speed.utils.connectTo
import net.minecraft.server.v1_8_R3.NBTTagCompound
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player


object GameInitTask {


    fun init() {
        if (LoaderData.mapList.isEmpty() || LoaderData.templateList.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage("找不到地图，请配置地图！谢谢。 ^ _ ^")
            Bukkit.getConsoleSender().sendMessage("啥啥没有你想让我开啥啊？")
            Bukkit.getConsoleSender().sendMessage("就算啥也不懂的都知道没有东西给你开个pi")
            Bukkit.getConsoleSender().sendMessage("绑图绑模板去")
            return
        }
        val mapData = LoaderData.mapList[LoaderData.mapList.keys.random()]!!
        val taskId = Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, GameWaitTask(mapData), 20L, 20L)
        LoaderData.nowTask[SpeedBuildReloaded.instance] = taskId.taskId // 记录任务ID 方便外置控制
        BoardManager(SpeedBuildReloaded.instance, DefaultBoardAdapter(mapData)) // 注册计分板
        Bukkit.getConsoleSender().sendMessage(mapData.worldName + "地图已经加载完毕！") // 通知控制台
        PlayerListener().register() // 注册玩家监听事件
        BlockListener().register() // 注册方块监听事件
        ForceCommand.register() // 注册强制开始命令
        nowMap[SpeedBuildReloaded.instance] = mapData // 与插件进行地图绑定
        LoaderData.gameStatus[mapData] = GameStatus.WAITING  // 放置等待模式标签
        Bukkit.getPluginManager().callEvent(GameChangeEvent(mapData, GameStatus.WAITING))
    }


    /**
     * 开始游戏
     */
    fun startGame(playerList: MutableList<Player>, arena: ArenaSettingData) {
        Bukkit.getScheduler().cancelTask(LoaderData.nowTask[SpeedBuildReloaded.instance]!!)
        val nms = if (arena.enableElderGuardian) {
            arena.middleIsland.toLocation(arena.worldName).createEntity()
        } else {
            null
        }
        val taskId = Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, GameLoopTask(arena, nms),2L, 2L)
        LoaderData.nowTask[SpeedBuildReloaded.instance] = taskId.taskId
        // 分队处理开始游戏部分
        playerList.forEach { p ->
            LoaderData.playerStatus[p] = PlayerStatus.LIFE
        }
        LoaderData.configSettings!!.gameRuleText.forEach {
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
        playerList.forEach { player ->
            // 找到一个还没有达到玩家限制的岛
            val island = arena.islandData.find { island ->
                islandPlayerCount[island]!! < arena.isLandPlayerLimit
            }
            // 如果找到了岛，将玩家分配到这个岛，并更新岛的玩家数量
            if (island != null) {
                GameVMData.playerBindIsLand[player] = island
                GameVMData.playerBindMiddle[player] = island.middleBlock.toLocation(arena.worldName).block
                islandPlayerCount[island] = islandPlayerCount[island]!! + 1
            }
            player.inventory.clear()
        }
        //
        checkTask[SpeedBuildReloaded.instance] =
            Bukkit.getScheduler().scheduleSyncRepeatingTask(SpeedBuildReloaded.instance, GameCheckTask(arena), 20L, 20L)
        LoaderData.gameStatus[arena] = GameStatus.STARTING  // 放置等待模式标签
        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GameStatus.STARTING))
        playerList.forEach {
            it.teleport(GameVMData.playerBindIsLand[it]!!.playerSpawn.toLocation(arena.worldName))
            it.gameMode = GameMode.SURVIVAL
            it.allowFlight = true
        }
        // 加守卫者
    }

    /**
     * 结算方法
     */
    fun stopGame(arena: ArenaSettingData) {
        // 停线程
        Bukkit.getScheduler().cancelTask(LoaderData.nowTask[SpeedBuildReloaded.instance]!!)
        val settlementMessage = LoaderData.configSettings!!.mess.settlement

        LoaderData.gameStatus[arena] = GameStatus.END
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
                val ss = s.replace("%first_player%", first)
                    .replace("%second_player%", second)
                    .replace("%third_player%", third)
                    .replace("%first_player_score%", first_player_score.toString())
                    .replace("%second_player_score%", second_player_score.toString())
                    .replace("%third_player_score%", third_player_score.toString()).cc(it)
                it.sendMessage(ss)
                Title.title(it, LoaderData.configSettings!!.mess.end, "")
            }
        }
        // 所有传送到
        val lobbyServer = LoaderData.configSettings!!.endReturnToTheLobby
        if (lobbyServer.isNotEmpty()) {
            Bukkit.getOnlinePlayers().forEach {
                it.connectTo(lobbyServer, SpeedBuildReloaded.instance)
            }
        }
        // 关服
        Bukkit.broadcastMessage("§630秒后关闭服务器")
        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance,{
            Bukkit.shutdown()
        }, 30 * 20L)
    }

    /**
     * 淘汰
     */
    private fun eliminate(lowestScoringPlayer: Player, score: Int, teacher: Teacher?) {
        Bukkit.getOnlinePlayers().forEach {
            Title.title(it, "", LoaderData.configSettings!!.mess.eliminate
                    .replace("%player%", lowestScoringPlayer.name)
            )
        }
        LoaderData.playerStatus[lowestScoringPlayer] = PlayerStatus.OUT
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
            if (teacher != null) {
                lowestScoringPlayer.sendRay(teacher)
                // 爆炸效果
                //note 小徒弟真的帅~
            } else {
                Bukkit.getConsoleSender().sendMessage("§b[SpeedBuild]§6未找到 NMS 实现类")
            }
            val list = GameVMData.playerBindIsLand[lowestScoringPlayer]!!.islandRegions.boom(
                GameVMData.playerBindMiddle[lowestScoringPlayer]!!
            )

            Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
                list.forEach {
                    it.remove()
                }
            }, 60)
            //
            lowestScoringPlayer.gameMode = GameMode.SPECTATOR
            // 给观察者模式的物品
        }, 0)
        LoaderData.storage.addEliminate(lowestScoringPlayer)
    }

    private fun Player.sendRay(teacher: Teacher) {
        val nbt = NBTTagCompound()
        teacher.c(nbt)
        nbt.setInt("NoAI", 0)
        teacher.f(nbt)
        val packet = PacketPlayOutEntityMetadata(
            teacher.id,
            teacher.upDataWatch(this), false
        )
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.playerConnection.sendPacket(packet)
        }
        teacher.c(nbt)
        nbt.setInt("NoAI", 1)
        teacher.f(nbt)
    }


    fun score(liftPlayerList: MutableList<Player>,
        nowBuildtarget: TemplateData,
        arena: ArenaSettingData,
        teacher: Teacher?
    ) { // 评分
        val scoreMap = mutableMapOf<Player, Int>() // 获取分数低开始处决
        liftPlayerList.forEach { p ->
            val score = LoaderData.templateList[nowBuildtarget]!!.compare(GameVMData.playerBindMiddle[p]!!)
            // 如果分数等于100 添加一个ReStore分数
            if (score == 100) {
                LoaderData.storage.addRestoreBuild(p)
            }
            scoreMap[p] = score
            Title.title(p, LoaderData.configSettings!!.mess.score
                .replace("%score%", score.toString()), "")
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
                eliminate(lowestScoringPlayer, lowestScoringPlayerEntry.value, teacher)
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
            nms.id,
            nms.clearWatch(), false
        )
        Bukkit.getOnlinePlayers().forEach {
            (it as CraftPlayer).handle.playerConnection.sendPacket(packet)
        }

        val map = nowMap[SpeedBuildReloaded.instance]!!
        nms.bukkitEntity.teleport(map.middleIsland.toLocation(map.worldName))
        nms.c(nbt)
        nbt.setInt("NoAI", 1)
        nms.f(nbt)
    }


}
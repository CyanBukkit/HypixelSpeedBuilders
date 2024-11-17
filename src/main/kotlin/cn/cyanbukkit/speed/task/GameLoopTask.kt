package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.BuildStatus
import cn.cyanbukkit.speed.data.GameStatus.*
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.data.TemplateData
import cn.cyanbukkit.speed.game.GameRegionManager
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.task.SendSign.send
import cn.cyanbukkit.speed.utils.Teacher
import cn.cyanbukkit.speed.utils.Title
import cn.cyanbukkit.speed.utils.getProgressBar
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat
import java.util.*

class GameLoopTask(
    private val arena: ArenaSettingData,
    private val nms: Teacher?
) : Runnable {

    private var startTime = LoaderData.configSettings!!.time.start * 10.0 // 进入后等待时间
    private val maxStartTime = LoaderData.configSettings!!.time.start * 10.0  // 进入后等待时间

    private var gameStartTime = LoaderData.configSettings!!.time.gameStart * 10.0  // 进入后等待时间
    private val maxGameStartTime = LoaderData.configSettings!!.time.gameStart * 10.0  // 进入后等待时间

    private var showTime = LoaderData.configSettings!!.time.show * 10.0 // 观察建筑时间
    private val maxShowTime = LoaderData.configSettings!!.time.show * 10.0  // 观察建筑时间

    private var buildTime = LoaderData.configSettings!!.time.build * 10.0  // 建筑时间 (秒)
    private val maxBuildTime = LoaderData.configSettings!!.time.build * 10.0  // 建筑时间 (秒)

    private var judgeTime = LoaderData.configSettings!!.time.judge * 10.0  // 评分时间 (秒)
    private val maxJudgeTime = LoaderData.configSettings!!.time.judge * 10.0  // 评分时间 (秒)

    private var nowBuildtarget: TemplateData? = null // 当前建造的模板
    private lateinit var nowItem: List<ItemStack>
    private var round = 1

    private val playerTimeStatus = mutableMapOf<Player, Long>()

    override fun run() {

        val liftPlayerList = mutableListOf<Player>()
        LoaderData.playerStatus.forEach { (player, playerStatus) ->
            run {
                if (playerStatus == PlayerStatus.LIFE) {
                    liftPlayerList.add(player)
                }
            }
        }

        when (LoaderData.gameStatus[arena]) {
            STARTING -> {
                when (startTime) {
                    in -10.0..10.0 -> {// 当等于0就下个环节
                        LoaderData.gameStatus[arena] = GAME_STARTING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GAME_STARTING))
                    }
                    maxStartTime -> { // 与默认时间一致
                        GameVMData.playerBindIsLand.forEach { (p, island) ->
                            island.buildRegions.clean()
                            p.inventory.clear()
                            p.activePotionEffects.forEach { p.removePotionEffect(it.type) }
                        }
                        nms.apply { // 重置
                            if (this != null) GameInitTask.clearWatch(this) else gg()
                        } // 清理掉落物
                        GameRegionManager.clearItem(arena)
                        playerTimeStatus.clear()
                    }
                }
                startTime -= 1.0 //每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    Title.actionbar(it, "§6§l游戏加载中... 等待${startTime.toInt() / 10}秒")
                }
            }
            GAME_STARTING -> {
                when (gameStartTime) {
                    in -10.0..10.0 -> {
                        LoaderData.gameStatus[arena] = OBSERVING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, OBSERVING))
                    }
                }
                val times = listOf(50.0, 40.0, 30.0, 20.0, 10.0)
                if (gameStartTime in times) {
                    liftPlayerList.forEach {
                        it.playSound(
                            it.location,
                            Sound.NOTE_PLING,
                            (gameStartTime / 10).toFloat(),
                            (gameStartTime / 10).toFloat()
                        )
                    }
                }
                gameStartTime -= 1.0//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.getProgressBar("§6§l 游戏开始", gameStartTime / 10.0, maxGameStartTime / 10.0)
                }
            }

            //---------------------------
            //
            //   下面是我加的
            //
            //---------------------------
            OBSERVING -> {
                if (judgeTime <= 0) { // 重置
                    judgeTime = maxJudgeTime
                }
                when (showTime) {
                    in -10.0..0.0 -> {
                        liftPlayerList.forEach { p ->
                            val island = GameVMData.playerBindIsLand[p]!!// 获取岛屿
                            nowItem.forEach { p.inventory.addItem(it) } // 给建筑用品
                            p.level = 0 // 恢复灯虎
                            LoaderData.playerBuildStatus.remove(p) // 清除建造状态
                            playerTimeStatus[p] = System.currentTimeMillis()
                            p.send(
                                LoaderData.configSettings!!.mess.viewEnd,
                                0,
                                title = false,
                                subTitle = true,
                                actionBar = true,
                                sound = Sound.NOTE_PLING,
                                volume = 5f,
                                pitch = 5f
                            ) // 开始建筑
                            p.sendMessage(LoaderData.configSettings!!.mess.startBuild)
                            // 清场地让他们舒舒服服的建造
                            island.buildRegions.clean()
                            p.playSound(p.location, Sound.EXPLODE, 1f, 1f)
                        } // 切换模式
                        LoaderData.gameStatus[arena] = BUILDING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, BUILDING))
                    }
                    maxShowTime -> {
                        nowBuildtarget = LoaderData.templateList.keys.random() // 抽目标
                        liftPlayerList.forEach { p ->
//                            p.allowFlight = true
                            val island = GameVMData.playerBindIsLand[p]!!
                            p.gameMode = GameMode.SURVIVAL
                            p.teleport(island.playerSpawn.toLocation(arena.worldName))
                            island.buildRegions.clean() // 清场地放展示物品用
                            p.activePotionEffects.forEach { p.removePotionEffect(it.type) }// 清理全身的效果
                            p.inventory.clear() // 清理身上的东西
                            Title.title(p, "§c"+ nowBuildtarget!!.name, "")
                            p.sendMessage("§a请牢记展示的建筑,稍后需要还原")
                            nowItem = nowBuildtarget!!.showTemplate(GameVMData.playerBindMiddle[p]!!) // 获取展示物品
                        }
                        nms.apply {
                            if (this != null) GameInitTask.clearWatch(this) else gg()
                        }
                        GameRegionManager.clearItem(arena)
                        playerTimeStatus.clear()
                    }

                }
                val times = listOf(50.0, 40.0, 30.0, 20.0, 10.0)
                if (showTime in times) {
                    liftPlayerList.forEach {
                        it.send(
                            LoaderData.configSettings!!.mess.countdownSub,
                            showTime.toInt() / 10, subTitle = true, actionBar = false,
                            volume = (showTime * 2 / 10).toFloat() , pitch = (showTime * 2/ 10).toFloat()
                        )
                    }
                }
                showTime -= 1.0//每2Tick执行下  删除10倍的秒数来计算真实的秒数
            }
            BUILDING -> { // 开始建造
                if (showTime <= 0) {
                    showTime = maxShowTime
                }
                when (buildTime) {
                    maxBuildTime -> {
                        liftPlayerList.forEach { p ->
                            Title.title(p, "", "§c比赛开始")
                        }
                    }
                    in -10.0..0.0 -> {
                        LoaderData.gameStatus[arena] = SCORE
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, SCORE))
                        liftPlayerList.forEach { p ->
                            p.gameMode = GameMode.SPECTATOR
                            p.send(
                                LoaderData.configSettings!!.mess.judge,  // 来自远古守卫者 铛 的一声
                                0,
                                title = true,
                                subTitle = true,
                                actionBar = true,
                                sound = Sound.ANVIL_LAND,
                                volume = 1f,
                                pitch = 1f
                            )
                            if (nms == null) gg() else nms.sendHit(liftPlayerList)
                        }
                    }

                    50.0 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有5秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    40.0 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有4秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    30.0 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有3秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    20.0 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有2秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    10.0 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有1秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }
                }
                buildTime -= 1.0//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.getProgressBar("§c时间结束 ", buildTime / 10.0, maxBuildTime / 10.0)
                }
                liftPlayerList.forEach { p ->
                    if (GameRegionManager.isSuccessPlace(LoaderData.templateList[nowBuildtarget]!!,
                            GameVMData.playerBindMiddle[p]!!)) {
                        if (playerTimeStatus.contains(p)) {
                            val t = (System.currentTimeMillis() - playerTimeStatus[p]!!) / 1000.0
                            LoaderData.storage.setFastestBuildTime(p, t, nowBuildtarget!!.name)
                            LoaderData.playerBuildStatus[p] = BuildStatus.CANTBUILD
                            p.sendMessage("§a恭喜你! 成功建造了 ${nowBuildtarget!!.name} 耗时 $t 秒")
                            p.playSound(p.location, Sound.LEVEL_UP, 1f, 1f)
                            Title.title(p, "§a恭喜你!", "§6 成功建造了 ${nowBuildtarget!!.name} 耗时 $t 秒")
                            playerTimeStatus.remove(p)
                            if (playerTimeStatus.isEmpty()) { // 九转大肠
                                buildTime = 0.0
                                LoaderData.gameStatus[arena] = SCORE
                                Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, SCORE))
                                liftPlayerList.forEach { p ->
                                    p.gameMode = GameMode.SPECTATOR
                                    p.send(
                                        "§a",  // 来自远古守卫者 铛 的一声
                                        0,
                                        title = true,
                                        subTitle = true,
                                        actionBar = true,
                                        sound = Sound.ANVIL_LAND,
                                        volume = 1f,
                                        pitch = 1f
                                    )
                                    if (nms == null) gg() else nms.sendHit(liftPlayerList)
                                }
                                return
                            }
                        }
                    }
                }

            }


            SCORE -> {
                if (buildTime <= 0) { // 如果小于等于0 恢复
                    buildTime = maxBuildTime
                }
                when (judgeTime) {
                    in -1.0..0.0 -> {
                        round++
                        LoaderData.gameStatus[arena] = OBSERVING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, OBSERVING))
                    }

                    maxJudgeTime -> {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
                            GameInitTask.score(liftPlayerList, nowBuildtarget!!, arena, nms) // 评分
                        }, 10L)
                    }

                }
                judgeTime -= 1.0//每2Tick执行下  删除10倍的秒数来计算真实的秒数
            }


            // 固定
            END -> { // 游戏结束
                GameInitTask.stopGame(arena)
            }

            WAITING -> {}
            null -> {}

        }
        // 计分板

        val newList = mutableListOf<String>()
        val max = (arena.islandData.size * arena.isLandPlayerLimit)
        val now = liftPlayerList.size
        val time = SimpleDateFormat("yy/MM/dd").format(Date())

        val tn = if (nowBuildtarget != null) {
            nowBuildtarget!!.name
        } else {
            ""
        }

        LoaderData.configSettings!!.scoreBroad["Gaming"]!!.line.forEach {
            newList.add(
                it
                    .replace("%now%", now.toString())
                    .replace("%mapName%", arena.worldName)
                    .replace("%max%", max.toString())
                    .replace("%target%", tn)
                    .replace("%round%", round.toString())
                    .replace("%time%", time)
            )
        }

        LoaderData.hotScoreBroadLine = newList


        // 最大回合判定
        if (round >= 20) {
            liftPlayerList.forEach {
                LoaderData.storage.addWins(it)
            }
            LoaderData.gameStatus[arena] = END
            Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, END))
        }

    }


    private fun gg() {
        Bukkit.getConsoleSender().sendMessage("§b[SpeedBuild]§6未找到 NMS 实现类")
    }
}
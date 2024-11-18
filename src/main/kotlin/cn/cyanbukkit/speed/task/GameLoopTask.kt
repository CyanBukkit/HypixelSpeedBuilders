package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.build.TemplateData
import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.GameRegionManager
import cn.cyanbukkit.speed.game.GameStatus.*
import cn.cyanbukkit.speed.task.GameTask.startGame
import cn.cyanbukkit.speed.task.GameVMData.configSettings
import cn.cyanbukkit.speed.task.GameVMData.gameStatus
import cn.cyanbukkit.speed.task.GameVMData.hotScoreBroadLine
import cn.cyanbukkit.speed.task.GameVMData.lifeIsLand
import cn.cyanbukkit.speed.task.GameVMData.playerBuildStatus
import cn.cyanbukkit.speed.task.GameVMData.playerStatus
import cn.cyanbukkit.speed.task.GameVMData.storage
import cn.cyanbukkit.speed.task.GameVMData.templateList
import cn.cyanbukkit.speed.task.SendSign.send
import cn.cyanbukkit.speed.utils.Teacher
import cn.cyanbukkit.speed.utils.Title
import cn.cyanbukkit.speed.utils.getProgressBar
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class GameLoopTask(
    private val arena: ArenaSettingData,
    private val nms: Teacher?
) : Runnable {

    private var startTime = configSettings!!.time.start // 进入后等待时间
    private val maxStartTime = configSettings!!.time.start  // 进入后等待时间

    private var gameStartTime = configSettings!!.time.gameStart // 进入后等待时间
    private val maxGameStartTime = configSettings!!.time.gameStart  // 进入后等待时间

    private var showTime = configSettings!!.time.show // 观察建筑时间
    private val maxShowTime = configSettings!!.time.show // 观察建筑时间

    private var buildTime = configSettings!!.time.build // 建筑时间 (秒)
    private val maxBuildTime = configSettings!!.time.build  // 建筑时间 (秒)

    private var judgeTime = configSettings!!.time.judge  // 评分时间 (秒)
    private val maxJudgeTime = configSettings!!.time.judge  // 评分时间 (秒)

    private var nowBuildTarget: TemplateData? = null // 当前建造的模板

    private var round = 1

    private val playerTimeStatus = mutableMapOf<Player, Long>()
    private var time = configSettings!!.time.wait

    override fun run() {
        val liftPlayerList = playerStatus.filter { it.value == PlayerStatus.LIFE }.keys
        updateGameStatus(liftPlayerList)
        updateScoreBoard(liftPlayerList)
    }

    private fun updateScoreBoard(liftPlayerList: Set<Player>) { // 计分板
        val newList = mutableListOf<String>()
        val max = (arena.islandData.size * arena.isLandPlayerLimit)
        val now = liftPlayerList.size
        val time = SimpleDateFormat("yy/MM/dd").format(Date())
        val tn = if (nowBuildTarget != null) {
            nowBuildTarget!!.name
        } else {
            ""
        }
        configSettings!!.scoreBroad["Gaming"]!!.line.forEach {
            newList.add(it
                    .replace("%now%", now.toString())
                    .replace("%mapName%", arena.worldName)
                    .replace("%max%", max.toString())
                    .replace("%target%", tn)
                    .replace("%round%", round.toString())
                    .replace("%time%", time)
            )
        }
        hotScoreBroadLine = newList
    }


    private fun Player.sendTitleAndActionBar(time: Int) {
        playSound(location, Sound.NOTE_BASS, 1f, 1f)
        Title.title(this, "§c${time}", "§e游戏即将开始！", 10, 20, 10)
        Title.actionbar(this, configSettings!!.mess.countdown.replace("%time%", time.toString()))
    }

    private fun updateGameStatus(liftPlayerList: Set<Player>) {
        when (gameStatus) {
            WAITING -> {
                val playerList = mutableListOf<Player>()
                playerStatus.forEach { (player, playerStatus) ->
                    run {
                        if (playerStatus == PlayerStatus.WAITING) {
                            playerList.add(player)
                        }
                    }
                }
                val newList = mutableListOf<String>()
                val now = playerList.size
                val max = (arena.islandData.size * arena.isLandPlayerLimit)
                if (now >= arena.minimumPlayers) {
                    if (now >= max / 2 && time > configSettings!!.time.forwardWaiting) {
                        time = configSettings!!.time.forwardWaiting
                        playerList.forEach {
                            it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                        }
                    }
                    time--
                } else {
                    time = configSettings!!.time.wait
                }
                val timeLists = listOf(59, 30, 10, 5, 4, 3, 2, 1)
                if (time in timeLists) {
                    playerList.forEach {
                        it.sendTitleAndActionBar(time)
                    }
                    if (time <= 1) {
                        playerList.forEach { // actionbar
                            it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                        }
                        startGame(playerList, arena)
                        return
                    }
                }
                val countdown = time
                val need = max - now
                val time = SimpleDateFormat("yy/MM/dd").format(Date())
                configSettings!!.scoreBroad["Wait"]!!.line.forEach {
                    newList.add(it
                            .replace("%now%", now.toString())
                            .replace("%mapName%", arena.worldName)
                            .replace("%max%", max.toString())
                            .replace("%countdown%", countdown.toString())
                            .replace("%remainPlayer%", need.toString())
                            .replace("%time%", time)
                    )
                }
                hotScoreBroadLine = newList
            }

            STARTING -> {
                when (startTime) {
                    in -1..1 -> {// 当等于0就下个环节
                        gameStatus = GAME_STARTING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, GAME_STARTING))
                    }
                    maxStartTime -> { // 与默认时间一致
                        cleanShowTemplate(lifeIsLand)
                        GameVMData.playerBindIsLand.forEach { (p, island) ->
                            p.inventory.clear()
                            p.activePotionEffects.forEach { p.removePotionEffect(it.type) }
                        }
                        nms.apply { // 重置
                            if (this != null) GameTask.clearWatch(this) else gg()
                        } // 清理掉落物
                        GameRegionManager.clearItem(arena)
                        playerTimeStatus.clear()
                    }
                }
                startTime -= 1  // 每秒
                Bukkit.getOnlinePlayers().forEach {
                    Title.actionbar(it, "§6§l游戏加载中... 等待${startTime}秒")
                }
            }

            GAME_STARTING -> {
                when (gameStartTime) {
                    in -1..1 -> {
                        gameStatus = OBSERVING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, OBSERVING))
                    }
                }
                val times = listOf(5, 4, 3, 2, 1)
                if (gameStartTime in times) {
                    liftPlayerList.forEach {
                        it.playSound(it.location, Sound.NOTE_PLING, (gameStartTime / 10).toFloat(), (gameStartTime / 10).toFloat())
                    }
                }
                gameStartTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.getProgressBar("§6§l 游戏开始", gameStartTime, maxGameStartTime)
                }
            }

            OBSERVING -> {
                if (judgeTime <= 0) { // 重置
                    judgeTime = maxJudgeTime
                }
                when (showTime) {
                    7 ->  {
                        cleanShowTemplate(lifeIsLand)
                    }
                    in -1..1 -> { // 切换模式
                        liftPlayerList.forEach { p ->
                            playerBuildStatus.remove(p) // 清除建造状态
                            playerTimeStatus[p] = System.currentTimeMillis()
                            p.level = 0 // 恢复灯虎
                            p.send(configSettings!!.mess.viewEnd, 0, title = false, subTitle = true, actionBar = true, sound = Sound.NOTE_PLING, volume = 5f, pitch = 5f) // 开始建筑
                            p.sendMessage(configSettings!!.mess.startBuild)
                            // 清场地让他们舒舒服服的建造
                            p.playSound(p.location, Sound.EXPLODE, 1f, 1f)
                        }
                        gameStatus = BUILDING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, BUILDING))
                    }

                    maxShowTime -> {
                        // 添加安全检查
                        if (templateList.isEmpty()) {
                            Bukkit.getLogger().severe("[SpeedBuildReloaded] No templates available!")
                            gameStatus = END
                            Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, END))
                            return
                        }
                        nowBuildTarget = try {
                            templateList.keys.random() // 添加异常处理
                        } catch (e: Exception) {
                            Bukkit.getLogger().severe("[SpeedBuildReloaded] Failed to select random template: ${e.message}")
                            gameStatus = END
                            Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, END))
                            return
                        }
                        cleanShowTemplate(lifeIsLand)
                        GameVMData.playerBindIsLand.forEach { (t, u) ->
                            t.activePotionEffects.forEach { t.removePotionEffect(it.type) }
                            t.inventory.clear()
                            t.teleport(u.playerSpawn.toLocation())
                            t.gameMode = GameMode.SURVIVAL
                        }
                        liftPlayerList.forEach { p ->
                            try {
                                nowBuildTarget?.let { target ->
                                    Title.title(p, "§c${target.name}", "")
                                    p.sendMessage("§a请牢记展示的建筑,稍后需要还原")
                                }
                                showTemplate(lifeIsLand,nowBuildTarget!!)
                            } catch (e: Exception) {
                                Bukkit.getLogger().severe("[SpeedBuildReloaded] Error processing player ${p.name}: ${e.message}")
                            }
                        }
                        nms.apply {
                            if (this != null) GameTask.clearWatch(this) else gg()
                        }
                        GameRegionManager.clearItem(arena)
                        playerTimeStatus.clear()
                    }
                }
                val times = listOf(5, 4, 3, 2, 1)
                if (showTime in times) {
                    liftPlayerList.forEach {
                        it.send(configSettings!!.mess.countdownSub,
                            showTime / 10, subTitle = true, actionBar = false,
                            volume = (showTime * 2 / 10).toFloat(), pitch = (showTime * 2 / 10).toFloat()
                        )
                    }
                }
                showTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
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
                    in -1..1 -> {
                        gameStatus = SCORE
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, SCORE))
                        liftPlayerList.forEach { p ->
                            p.gameMode = GameMode.SPECTATOR
                            p.send(configSettings!!.mess.judge,  // 来自远古守卫者 铛 的一声
                                0, title = true, subTitle = true, actionBar = true,
                                sound = Sound.ANVIL_LAND, volume = 1f, pitch = 1f
                            )
                            if (nms == null) gg() else nms.sendHit(liftPlayerList.toMutableList())
                        }
                    }
                    5 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有5秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    4 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有4秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    3 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有3秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }

                    2 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有2秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }
                }
                buildTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.getProgressBar("§c时间结束 ", buildTime , maxBuildTime)
                }
                liftPlayerList.forEach { p ->
                    if (GameRegionManager.isSuccessPlace(templateList[nowBuildTarget]!!, GameVMData.playerBindMiddle[p]!!)) {
                        if (playerTimeStatus.contains(p)) {
                            val t = (System.currentTimeMillis() - playerTimeStatus[p]!!) / 1000.0
                            storage.setFastestBuildTime(p, t, nowBuildTarget!!.name)
                            playerBuildStatus[p] = BuildStatus.CANTBUILD
                            p.sendMessage("§a恭喜你! 成功建造了 ${nowBuildTarget!!.name} 耗时 $t 秒")
                            p.playSound(p.location, Sound.LEVEL_UP, 1f, 1f)
                            Title.title(p, "§a恭喜你!", "§6 成功建造了 ${nowBuildTarget!!.name} 耗时 $t 秒")
                            playerTimeStatus.remove(p)
                            if (playerTimeStatus.isEmpty()) { // 九转大肠
                                buildTime = 0
                                gameStatus = SCORE
                                Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, SCORE))
                                liftPlayerList.forEach { pp ->
                                    p.gameMode = GameMode.SPECTATOR
                                    p.send("§a", 0, title = true, subTitle = true, actionBar = true, sound = Sound.ANVIL_LAND, volume = 1f, pitch = 1f)
                                }
                                nms?.apply {
                                    GameTask.clearWatch(this)
                                } ?: gg()
                                GameRegionManager.clearItem(arena)
                                playerTimeStatus.clear()
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
                    in -1..0 -> {
                        round++
                        gameStatus = OBSERVING
                        Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, OBSERVING))
                    }

                    maxJudgeTime -> {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
                            GameTask.score(liftPlayerList.toMutableList(), nowBuildTarget!!, arena, nms) // 评分
                        }, 10L)
                    }

                }
                judgeTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
            }


            // 固定
            END -> { // 游戏结束
                GameTask.stopGame(arena)
            }

            NULL -> {}

        }

        // 最大回合判定
        if (round >= 20) {
            liftPlayerList.forEach {
                storage.addWins(it)
            }
            gameStatus = END
            Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, END))
        }
    }


    private fun gg() {
        Bukkit.getConsoleSender().sendMessage("§b[SpeedBuild]§6未找到 NMS 实现类")
    }
}
package cn.cyanbukkit.speed.game.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.api.event.GameChangeEvent
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.BuildStatus
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.GameHandle
import cn.cyanbukkit.speed.game.GameHandle.isSuccessPlace
import cn.cyanbukkit.speed.game.GameHandle.startGame
import cn.cyanbukkit.speed.game.GameStatus.*
import cn.cyanbukkit.speed.game.GameVMData
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.game.GameVMData.hotScoreBroadLine
import cn.cyanbukkit.speed.game.GameVMData.lifeIsLand
import cn.cyanbukkit.speed.game.GameVMData.playerBuildStatus
import cn.cyanbukkit.speed.game.GameVMData.playerStatus
import cn.cyanbukkit.speed.game.GameVMData.storage
import cn.cyanbukkit.speed.game.GameVMData.templateList
import cn.cyanbukkit.speed.game.build.Template
import cn.cyanbukkit.speed.game.build.Template.cleanShowTemplate
import cn.cyanbukkit.speed.game.build.Template.fastCleanRegion
import cn.cyanbukkit.speed.game.build.Template.showTemplate
import cn.cyanbukkit.speed.utils.Teacher
import cn.cyanbukkit.speed.utils.Title
import cn.cyanbukkit.speed.utils.send
import cn.cyanbukkit.speed.utils.showProgressBar
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class GameLoopTask(
    private val arena: ArenaSettingData, private val nms: Teacher?
) : Runnable {
    private lateinit var nowBuildTarget: String // 当前建造的模板

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
        val tn = if (::nowBuildTarget.isInitialized) {
            nowBuildTarget
        } else {
            "还未选择"
        }
        configSettings!!.scoreBroad["Gaming"]!!.line.forEach {
            newList.add(
                it.replace("%now%", now.toString()).replace("%mapName%", arena.worldName)
                    .replace("%max%", max.toString()).replace("%target%", tn).replace("%round%", round.toString())
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
                    newList.add(
                        it.replace("%now%", now.toString()).replace("%mapName%", arena.worldName)
                            .replace("%max%", max.toString()).replace("%countdown%", countdown.toString())
                            .replace("%remainPlayer%", need.toString()).replace("%time%", time)
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
                        liftPlayerList.forEach { p ->
                            p.inventory.clear()
                            p.activePotionEffects.forEach { p.removePotionEffect(it.type) }
                        }
                        nms.apply { // 重置
                            if (this != null) GameHandle.clearWatch(this) else gg()
                        } // 清理掉落物
                        Template.clearItem(arena)
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
                        it.playSound(
                            it.location,
                            Sound.NOTE_PLING,
                            (gameStartTime / 10).toFloat(),
                            (gameStartTime / 10).toFloat()
                        )
                    }
                }
                gameStartTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.showProgressBar("§6§l 游戏开始", gameStartTime, maxGameStartTime)
                }
                fastCleanRegion(lifeIsLand)
            }

            OBSERVING -> {
                if (judgeTime <= 0) { // 重置
                    judgeTime = maxJudgeTime
                }
                when (showTime) {
                    7 -> {
                        cleanShowTemplate(lifeIsLand)
                    }

                    in -1..0 -> { // 切换模式
                        liftPlayerList.forEach { p ->
                            playerBuildStatus.remove(p) // 清除建造状态
                            playerTimeStatus[p] = System.currentTimeMillis()
                            p.level = 0 // 恢复灯虎
                            p.send(
                                configSettings!!.mess.viewEnd,
                                0,
                                title = false,
                                subTitle = true,
                                actionBar = true,
                                sound = Sound.NOTE_PLING,
                                volume = 5f,
                                pitch = 5f
                            )
                            // 开始建筑
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
                            Bukkit.getLogger()
                                .severe("[SpeedBuildReloaded] Failed to select random template: ${e.message}")
                            gameStatus = END
                            Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, END))
                            return
                        }
                        GameVMData.playerBindIsLand.forEach { (t, u) ->
                            t.activePotionEffects.forEach { t.removePotionEffect(it.type) }
                            t.inventory.clear()
                            t.teleport(u.playerSpawn)
                            t.gameMode = GameMode.SURVIVAL
                        }
                        liftPlayerList.forEach { p ->
                            try {
                                Title.title(p, "§c$nowBuildTarget", "")
                                p.sendMessage("§a请牢记展示的建筑,稍后需要还原")
                            } catch (e: Exception) {
                                Bukkit.getLogger()
                                    .severe("[SpeedBuildReloaded] Error processing player ${p.name}: ${e.message}")
                            }
                        }
                        showTemplate(lifeIsLand, nowBuildTarget) // 显示建筑
                        nms.apply {
                            if (this != null) GameHandle.clearWatch(this) else gg()
                        }
                        Template.clearItem(arena) // 清理掉落物
                        playerTimeStatus.clear() // 玩家计时器
                    }
                }
                val times = listOf(5, 4, 3, 2, 1)
                if (showTime in times) {
                    liftPlayerList.forEach {
                        it.send(
                            configSettings!!.mess.countdownSub,
                            showTime,
                            subTitle = true,
                            actionBar = false,
                            volume = (showTime * 2).toFloat(),
                            pitch = (showTime * 2).toFloat()
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
                            p.send(
                                configSettings!!.mess.judge,  // 来自远古守卫者 铛 的一声
                                0,
                                title = true,
                                subTitle = true,
                                actionBar = true,
                                sound = Sound.ANVIL_LAND,
                                volume = 1f,
                                pitch = 1f
                            )
                            if (nms == null) gg() else nms.sendHit(liftPlayerList.toMutableList())
                        }
                    }

                    in 2..5 -> {
                        liftPlayerList.forEach {
                            it.sendMessage("§b还有${buildTime}秒进入评分环节了快点建...")
                            it.playSound(it.location, Sound.NOTE_PLING, 1f, 1f)
                        }
                    }
                }
                buildTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
                Bukkit.getOnlinePlayers().forEach {
                    it.showProgressBar("§c时间结束 ", buildTime, maxBuildTime)
                }
                // 每秒检查是否建造完成 (此时可以计算毫秒)
                liftPlayerList.forEach { p ->
                    if (isSuccessPlace(templateList[nowBuildTarget]!!, GameVMData.playerBindIsLand[p]!!.middleBlock)) {
                        if (playerTimeStatus.contains(p)) {
                            val t = (System.currentTimeMillis() - playerTimeStatus[p]!!) / 1000.0
                            storage.setFastestBuildTime(p, t, nowBuildTarget)
                            playerBuildStatus[p] = BuildStatus.CANTBUILD
                            p.sendMessage("§a恭喜你! 成功建造了 $nowBuildTarget 耗时 $t 秒")
                            p.playSound(p.location, Sound.LEVEL_UP, 1f, 1f)
                            Title.title(p, "§a恭喜你!", "§6 成功建造了 $nowBuildTarget 耗时 $t 秒")
                            playerTimeStatus.remove(p)
                            if (playerTimeStatus.isEmpty()) { // 九转大肠
                                buildTime = 0
                                gameStatus = SCORE
                                p.gameMode = GameMode.SPECTATOR
                                Bukkit.getPluginManager().callEvent(GameChangeEvent(arena, SCORE))
                                p.send(
                                    "§a",
                                    0,
                                    title = true,
                                    subTitle = true,
                                    actionBar = true,
                                    sound = Sound.ANVIL_LAND,
                                    volume = 1f,
                                    pitch = 1f
                                )
                                nms?.apply {
                                    GameHandle.clearWatch(this)
                                } ?: gg()
                                Template.clearItem(arena)
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
                        fastCleanRegion(lifeIsLand)
                    }

                    maxJudgeTime -> {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(SpeedBuildReloaded.instance, {
                            GameHandle.score(liftPlayerList.toMutableList(), nowBuildTarget, nms) // 评分
                        }, 10L)
                    }

                }
                judgeTime -= 1//每2Tick执行下  删除10倍的秒数来计算真实的秒数
            }


            // 固定
            END -> { // 游戏结束
                GameHandle.stopGame(arena)
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
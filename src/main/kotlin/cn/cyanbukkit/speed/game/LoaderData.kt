package cn.cyanbukkit.speed.game

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.command.AddTemplateCommand
import cn.cyanbukkit.speed.command.SelectIslandCommand
import cn.cyanbukkit.speed.command.SetUpCommand
import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.GameVMData.mapList
import cn.cyanbukkit.speed.game.GameVMData.storage
import cn.cyanbukkit.speed.game.GameVMData.templateList
import cn.cyanbukkit.speed.game.build.Template
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import cn.cyanbukkit.speed.game.build.TemplateData
import cn.cyanbukkit.speed.utils.loadIslandData
import cn.cyanbukkit.speed.utils.storage.HikariLink
import cn.cyanbukkit.speed.utils.storage.YamlLink
import cn.cyanbukkit.speed.utils.toLocation
import org.bukkit.Bukkit
import java.util.*

/**
 * 数据加载专用
 */
object LoaderData {

    fun init() {
        // 配置文件加载
        configSettings = null
        val serverMode = SpeedBuildReloaded.instance.config.getString("ServerMode") ?: "BungeeCord"
        val wait = SpeedBuildReloaded.instance.config.getInt("TimeSetting.Wait")
        val forwardWaiting = SpeedBuildReloaded.instance.config.getInt("TimeSetting.ForwardWaiting")
        val startTime = SpeedBuildReloaded.instance.config.getInt("TimeSetting.start-time")
        val gameStart = SpeedBuildReloaded.instance.config.getInt("TimeSetting.game-start-time")
        val observation = SpeedBuildReloaded.instance.config.getInt("TimeSetting.showcase-time")
        val buildTime = SpeedBuildReloaded.instance.config.getInt("TimeSetting.build-time")
        val judgement = SpeedBuildReloaded.instance.config.getInt("TimeSetting.judge-time")
        val td = ConfigTimeData(wait, forwardWaiting, startTime, gameStart, observation, buildTime, judgement)
        val gameRuleText = SpeedBuildReloaded.instance.config.getStringList("GameRuleText")
        val scoreBroad = mutableMapOf<String, ConfigScoreBroadData>()
        SpeedBuildReloaded.instance.config.getConfigurationSection("ScoreBroad")!!.getKeys(false).forEach {
            val line = SpeedBuildReloaded.instance.config.getStringList("ScoreBroad.$it.Line")
            val title = SpeedBuildReloaded.instance.config.getString("ScoreBroad.$it.Title")
            scoreBroad[it] = ConfigScoreBroadData(title, line)
        }
        val endReturnToTheLobby = SpeedBuildReloaded.instance.config.getString("EndReturnToTheLobby")
        val commands = mutableMapOf<String, MutableList<String>>()
        (SpeedBuildReloaded.instance.config.getConfigurationSection("Commands")
            ?: SpeedBuildReloaded.instance.config.createSection("Commands")).getKeys(false).forEach {
            commands[it] = SpeedBuildReloaded.instance.config.getStringList("Commands.$it")
        }
        val join = SpeedBuildReloaded.instance.config.getString("Message.Join")
        val quit = SpeedBuildReloaded.instance.config.getString("Message.Quit")
        val start = SpeedBuildReloaded.instance.config.getString("Message.Start")
        val countdown = SpeedBuildReloaded.instance.config.getString("Message.Countdown")
        val tig = SpeedBuildReloaded.instance.config.getString("Message.Tig")
        val countdownSub = SpeedBuildReloaded.instance.config.getString("Message.CountdownSub")
        val viewEnd = SpeedBuildReloaded.instance.config.getString("Message.ViewEnd")
        val lastTime = SpeedBuildReloaded.instance.config.getString("Message.LastTime")
        val startBuild = SpeedBuildReloaded.instance.config.getString("Message.StartBuild")
        val judgementMess = SpeedBuildReloaded.instance.config.getString("Message.Judgement")
        val score = SpeedBuildReloaded.instance.config.getString("Message.Score")
        val elimination = SpeedBuildReloaded.instance.config.getString("Message.Eliminate")
        val noLeaveRegion = SpeedBuildReloaded.instance.config.getString("Message.NoLeaveRegion")
        val end = SpeedBuildReloaded.instance.config.getString("Message.End")
        val settlement = SpeedBuildReloaded.instance.config.getStringList("Message.Settlement")
        val noPermission = SpeedBuildReloaded.instance.config.getString("Message.NoPermission")
        val roundFormat = SpeedBuildReloaded.instance.config.getString("Message.RoundFormat")
        val messData = ConfigMessageData(
            join,
            quit,
            start,
            countdown,
            tig,
            countdownSub,
            viewEnd,
            lastTime,
            startBuild,
            judgementMess,
            score,
            elimination,
            noLeaveRegion,
            end,
            settlement,
            noPermission,
            roundFormat
        )
        val dataStorageMode = SpeedBuildReloaded.instance.config.getString("DataStorageMode") ?: "Yaml"
        val sqlUrl = SpeedBuildReloaded.instance.config.getString("MySQL.Url")!!
        val user = SpeedBuildReloaded.instance.config.getString("MySQL.User")!!
        val password = SpeedBuildReloaded.instance.config.getString("MySQL.Password")!!
        val mysql = ConfigMySQLData(sqlUrl, user, password)
        configSettings = ConfigData(
            serverMode, td, gameRuleText, scoreBroad, endReturnToTheLobby, commands, messData, dataStorageMode, mysql
        )
        when (dataStorageMode) {
            "Yaml" -> {
                storage = YamlLink()
            }

            "MySQL" -> {
                storage = HikariLink()
            }
        }
        storage.link()
        SelectIslandCommand.register()
        if (serverMode == "Lobby") {
            Bukkit.getConsoleSender().sendMessage("已开启大厅模式")
            return
        }
        // 地图加载
        loadMap()
        // 步骤设置
        SetUpCommand().register()
        //templateList
        loadTemplate()
        loadIslandData()
        // 添加建筑模板
        AddTemplateCommand().register()
        Template.register()
        // 游戏加载
        GameHandle.init()
    }

    fun loadTemplate() {
        templateList.clear()
        val lists = SpeedBuildReloaded.instance.blockTemplate.getConfigurationSection("Lists")
            ?: SpeedBuildReloaded.instance.blockTemplate.createSection("Lists")
        lists.getKeys(false).forEach { name ->
            val temp = lists.getConfigurationSection(name)!!
            val templateBlockDataList = mutableListOf<TemplateBlockData>()
            temp.getConfigurationSection("Blocks").getKeys(false).forEach { key ->
                val type = temp.getString("Blocks.$key.type")!!
                val data = temp.getString("Blocks.$key.data")
                val split = key.split(",")
                val x = split[0].toInt()
                val y = split[1].toInt()
                val z = split[2].toInt()
                val templateBlockData = TemplateBlockData(x, y, z, type, data)
                templateBlockDataList.add(templateBlockData)
            }
            val difficulty = temp.getString("Difficulty")!!
            templateList[name] = TemplateData(difficulty, templateBlockDataList)
        }
    }

    fun loadMap() {
        mapList.clear()
        SpeedBuildReloaded.instance.settings.getKeys(false).forEach { name ->
            val mapName =
                SpeedBuildReloaded.instance.settings.getString("$name.WorldName") ?: "未命名地图${UUID.randomUUID()}}"
            Bukkit.getConsoleSender().sendMessage("加载地图 $mapName")
            val miniPlayers = SpeedBuildReloaded.instance.settings.getInt("$name.MinimumPlayers")
            Bukkit.getConsoleSender().sendMessage("最小玩家数 $miniPlayers")
            val isLandPlayerLimit = SpeedBuildReloaded.instance.settings.getInt("$name.IsLandPlayerLimit")
            Bukkit.getConsoleSender().sendMessage("岛屿玩家限制 $isLandPlayerLimit")
            val enableElderGuardian = SpeedBuildReloaded.instance.settings.getBoolean("$name.EnableElderGuardian")
            Bukkit.getConsoleSender().sendMessage("是否启用守卫者 $enableElderGuardian")
            val middleIsland = SpeedBuildReloaded.instance.settings.getString("$name.MiddleIsland")!!.toLocation()
            Bukkit.getConsoleSender().sendMessage("中心岛坐标 $middleIsland")
            val waitingLobby = SpeedBuildReloaded.instance.settings.getString("$name.WaitingLobby")!!.toLocation()
            Bukkit.getConsoleSender().sendMessage("等待大厅坐标 $waitingLobby")
            val islandData = mutableListOf<ArenaIslandData>()
            val isd = SpeedBuildReloaded.instance.settings.getConfigurationSection("$name.IsLand")
                ?: SpeedBuildReloaded.instance.settings.createSection("$name.IsLand")
            //  # 岛屿id
            isd.getKeys(false).forEach {
                //淘汰后岛会炸掉所有岛上的方块变为FallingBlock并且 呈现爆炸效果
                val playerSpawn = isd.getString("$it.PlayerSpawn")!!.toLocation()
                Bukkit.getConsoleSender().sendMessage("玩家出生点 $playerSpawn")
                // 岛屿平台中心方块 以此方块为中心 审查 建筑区域上的方块是否完整
                val middleBlock = isd.getString("$it.MiddleBlock")!!.toLocation().block
                Bukkit.getConsoleSender().sendMessage("岛屿中心方块 $middleBlock")
                val face = IslandFace.valueOf(isd.getString("$it.IsLandFace") ?: "NORTH")
                Bukkit.getConsoleSender().sendMessage("岛屿朝向 $face")
                islandData.add(ArenaIslandData(playerSpawn, middleBlock, face))
            }
            Bukkit.getConsoleSender().sendMessage("岛屿数据加载完成")
            mapList[name] = ArenaSettingData(
                mapName,
                miniPlayers,
                isLandPlayerLimit,
                enableElderGuardian,
                middleIsland,
                waitingLobby,
                islandData
            )
        }
    }

}
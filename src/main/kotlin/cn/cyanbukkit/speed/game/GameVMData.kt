package cn.cyanbukkit.speed.game

import cn.cyanbukkit.dragon.movement.DragonCircularMovement
import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import cn.cyanbukkit.speed.game.build.TemplateData
import cn.cyanbukkit.speed.utils.storage.Storage
import org.bukkit.Material
import org.bukkit.entity.Player

object GameVMData {
    // 负责存储游戏中的数据

    var first_player: Player? = null
    var second_player: Player? = null
    var third_player: Player? = null
    val build_second = mutableMapOf<Player, Double>() // 建造了多少秒
    val allScoreData = mutableMapOf<Player, Int>()
    val playerBindIsLand = mutableMapOf<Player, ArenaIslandData>()
    val lifeIsLand = mutableListOf<ArenaIslandData>()
    var nowTask = 0
    val needBuild = mutableMapOf<ArenaIslandData, List<TemplateBlockData>>()
    val buildSign = mutableListOf<Player>()
    val mapList = mutableMapOf<String, ArenaSettingData>() // 记录所有地图
    var configSettings: ConfigData? = null // 配置文件设置
    val playerStatus = mutableMapOf<Player, PlayerStatus>() // 玩家标记统计
    val playerPerfected = mutableMapOf<Player, Int>() // 玩家是否完美
    val playerBuildStatus = mutableMapOf<Player, BuildStatus>() // 玩家标记统计
    var gameStatus = GameStatus.NULL
    lateinit var nowMap: ArenaSettingData // 当前地图
    val spectator = mutableListOf<Player>() //玩家旁观者
    var hotScoreBroadLine = mutableMapOf<Player,MutableList<String>>() // 热更新计分板
    val backLobby = org.bukkit.inventory.ItemStack(Material.BED, 1).apply {
        val meta = itemMeta
        meta.displayName = "§b返回大厅"
        itemMeta = meta
    }
    lateinit var storage: Storage
    val templateList = mutableMapOf<String, TemplateData>() // 记录所有模板

    fun isInitNowMap() : Boolean {
        return ::nowMap.isInitialized
    }

    lateinit var dragon: DragonCircularMovement
}
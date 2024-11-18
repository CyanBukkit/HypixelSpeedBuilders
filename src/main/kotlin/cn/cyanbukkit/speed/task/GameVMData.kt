package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.build.TemplateBlockData
import cn.cyanbukkit.speed.build.TemplateData
import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.GameStatus
import cn.cyanbukkit.speed.storage.Storage
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

object GameVMData {
    // 负责存储游戏中的数据

    var first_player: Player? = null
    var second_player: Player? = null
    var third_player: Player? = null

    val allScoreData = mutableMapOf<Player, Int>()
    val playerBindIsLand = mutableMapOf<Player, ArenaIslandData>()
    val lifeIsLand = mutableListOf<ArenaIslandData>()
    val playerBindMiddle = mutableMapOf<Player, Block>()


    var nowTask = 0

    val needBuild = mutableMapOf<ArenaIslandData, List<TemplateBlockData>>()

    val buildSign = mutableListOf<Player>()

    val mapList = mutableMapOf<String, ArenaSettingData>() // 记录所有地图
    var configSettings: ConfigData? = null // 配置文件设置
    val playerStatus = mutableMapOf<Player, PlayerStatus>() // 玩家标记统计
    val playerBuildStatus = mutableMapOf<Player, BuildStatus>() // 玩家标记统计
    var gameStatus = GameStatus.NULL
    val nowMap = mutableMapOf<SpeedBuildReloaded, ArenaSettingData>()
    val spectator = mutableListOf<Player>() //玩家旁观者
    var hotScoreBroadLine = mutableListOf<String>() // 热更新计分板
    val backLobby = org.bukkit.inventory.ItemStack(Material.BED, 1).apply {
        val meta = itemMeta
        meta.displayName = "§b返回大厅"
        itemMeta = meta
    }
    lateinit var storage: Storage


    val templateList = mutableMapOf<TemplateData, List<TemplateBlockData>>() // 记录所有模板

}
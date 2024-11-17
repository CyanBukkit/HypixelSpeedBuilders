package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.data.ArenaIslandData
import org.bukkit.block.Block
import org.bukkit.entity.Player

object GameVMData {
    // 负责存储游戏中的数据

    var first_player : Player ? = null
    var second_player : Player ? = null
    var third_player : Player ? = null

    val allScoreData = mutableMapOf<Player, Int>()
    val playerBindIsLand = mutableMapOf<Player, ArenaIslandData>()
    val playerBindMiddle = mutableMapOf<Player, Block>()


}
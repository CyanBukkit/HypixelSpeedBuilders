package cn.cyanbukkit.speed.data

import cn.cyanbukkit.speed.game.GameVMData.radius
import org.bukkit.Location
import org.bukkit.block.Block

data class ArenaSettingData(
    val worldName: String,
    val minimumPlayers: Int,
    val isLandPlayerLimit: Int,
    val enableElderGuardian: Boolean,
    val middleIsland: Location,
    val waitingLobby: Location,
    val islandData: List<ArenaIslandData>
)


data class ArenaIslandData(
    val playerSpawn: Location,
    val middleBlock: Block,
    val face: IslandFace
) {

    val list = mutableListOf<Block>()

    init {
        //先把 middleBlock的 y是1到7 x和z 是 -3到3  全部保存在list里面
        for (y in 1..7) {
            for (x in -3..3) {
                for (z in -3..3) {
                    list.add(middleBlock.world.getBlockAt(middleBlock.x + x, middleBlock.y + y, middleBlock.z + z))
                }
            }
        }
    }


    fun get(y: Int): MutableList<Block> {
        // y是1到7
        return list.filter { it.y - middleBlock.y == y }.toMutableList()
    }

    /**
     * 算法是基于 middleBlock 的位置来计算的middleBlock的 y是1到7 x和z 是 -3到3 在这个区域的能放其他区域不能放
     */
    fun canBuild(bPut: Block): Boolean {
        return bPut.y - middleBlock.y in 1..7
                && bPut.x - middleBlock.x in -3..3
                && bPut.z - middleBlock.z in -3..3
                && middleBlock.world == bPut.world
    }

    /**
     * 以middle为中心半径为15的位置
     */
    fun inThisChunk(loc: Location): Boolean {
        return loc.world == middleBlock.world
                && (loc.x - middleBlock.x).toInt() in -radius..radius
                && (loc.z - middleBlock.z).toInt() in -radius..radius
    }

}

enum class IslandFace {
    NORTH,
    EAST,
    SOUTH,
    WEST
}

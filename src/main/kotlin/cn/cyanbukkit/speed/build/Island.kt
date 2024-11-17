package cn.cyanbukkit.speed.build

enum class IslandFace {
    NORTH,
    EAST,
    SOUTH,
    WEST
}


data class IslandBlockData(
    val x: Int,
    val y: Int,
    val z: Int,
    val type: String,
    val data: String
)
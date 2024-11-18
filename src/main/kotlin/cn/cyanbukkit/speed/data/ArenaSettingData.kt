package cn.cyanbukkit.speed.data

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block

data class ArenaSettingData(
    val worldName: String,
    val minimumPlayers: Int,
    val isLandPlayerLimit: Int,
    val enableElderGuardian: Boolean,
    val middleIsland: LocationString,
    val waitingLobby: LocationString,
    val waitingRegion: Region,
    val arenaRegions: Region,
    val islandData: List<ArenaIslandData>
)


data class ArenaIslandData(
    val playerSpawn: LocationString,
    val middleBlock: LocationString,
    val islandRegions: Region,
    val buildRegions: Region,
    val face : IslandFace
)

enum class IslandFace {
    NORTH,
    EAST,
    SOUTH,
    WEST
}

data class LocationString(
    val loc: String,
    val world: String
) {
    fun toLocation(): Location {
        val split = loc.split(",")
        return when (split.size) {
            3 -> Location(Bukkit.getWorld(world), split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
            5 -> Location(Bukkit.getWorld(world), split[0].toDouble(), split[1].toDouble(), split[2].toDouble(), split[3].toFloat(), split[4].toFloat())
            else -> throw IllegalArgumentException("LocationString must be 4 or 6")
        }
    }

    fun toBlock() : Block {
        return  toLocation().block
    }


}
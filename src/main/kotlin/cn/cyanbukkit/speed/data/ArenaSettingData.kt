package cn.cyanbukkit.speed.data

import org.bukkit.Location

data class ArenaSettingData(
    val worldName: String,
    val minimumPlayers: Int,
    val isLandPlayerLimit: Int,
    val enableElderGuardian: Boolean,
    val middleIsland: Location,
    val waitingLobby: Location,
    val waitingRegion: Region,
    val arenaRegions: Region,
    val islandData: List<ArenaIslandData>
)


data class ArenaIslandData(
    val playerSpawn: Location,
    val middleBlock: Location,
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

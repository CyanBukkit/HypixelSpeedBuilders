package cn.cyanbukkit.speed.data

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
    val buildRegions: Region
)
package cn.cyanbukkit.speed.data

data class ConfigData(
    val serverMode: String,
    val time: ConfigTimeData,
    val gameRuleText: List<String>,
    val scoreBroad: Map<String, ConfigScoreBroadData>,
    val endReturnToTheLobby: String,
    val commandList: Map<String, List<String>>,
    val mess: ConfigMessageData,
    val dataStorageMode: String,
    val configMySQLData: ConfigMySQLData
)




data class ConfigTimeData(
    val wait: Int,
    val forwardWaiting: Int,
    val start: Int,
    val gameStart: Int,
    val show: Int,
    val build: Int,
    val judge: Int
)

data class ConfigMessageData(
    val join: String,
    val quit: String,
    val start: String,
    val countdown: String,
    val tig: String,
    val countdownSub: String,
    val viewEnd: String,
    val lastTime: String,
    val startBuild: String,
    val judge: String,
    val score: String,
    val eliminate: String,
    val noLeaveRegion: String,
    val end: String,
    val settlement: List<String>,
    val noPermission: String
)

data class ConfigMySQLData(
    val url: String,
    val user: String,
    val password: String
)

data class ConfigScoreBroadData(
    val title: String,
    val line: List<String>
)
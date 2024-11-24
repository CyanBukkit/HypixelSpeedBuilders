package cn.cyanbukkit.speed.data

enum class PlayerStatus {
    LIFE,
    OUT,
    WAITING,
    LEAVE;
}

enum class BuildStatus {
    CANTBUILD,
}


data class ScoreData(
    // 完成度
    var success: Int = 0,
    // 如果没有完成为0.0
    var useTime: Double = 0.0
)

fun main() {
    for (i in 50 downTo 10 step 10){
        println("$i")
    }
}
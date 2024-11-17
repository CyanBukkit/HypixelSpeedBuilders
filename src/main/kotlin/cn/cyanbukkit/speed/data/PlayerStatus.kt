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

fun main() {
    for (i in 50 downTo 10 step 10){
        println("$i")
    }
}
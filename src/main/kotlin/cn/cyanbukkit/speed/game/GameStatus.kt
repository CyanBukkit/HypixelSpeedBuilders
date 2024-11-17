package cn.cyanbukkit.speed.game

enum class GameStatus {

    NULL,

    //等待玩家中
    WAITING,

    //开始游戏
    STARTING,

    GAME_STARTING,

    //观察阶段(读配置10s)
    OBSERVING,

    //建造阶段(读配置)
    BUILDING,

    //评分
    SCORE,

    //游戏结束
    END
}
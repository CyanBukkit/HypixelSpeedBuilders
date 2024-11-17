package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.GameStatus
import cn.cyanbukkit.speed.game.GameHandle
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.utils.Teacher

class EnderDragonTask: Runnable {
    private val dragon: Teacher?= null

    override fun run() {
        val mapData = LoaderData.nowMap[SpeedBuildReloaded.instance]!!
        if (LoaderData.gameStatus[mapData] == GameStatus.BUILDING || LoaderData.gameStatus[mapData] == GameStatus.OBSERVING) {
            if (dragon == null) {
                return
            }
            if (dragon.bukkitEntity.isDead) {
                return
            }
            
        }
    }
}
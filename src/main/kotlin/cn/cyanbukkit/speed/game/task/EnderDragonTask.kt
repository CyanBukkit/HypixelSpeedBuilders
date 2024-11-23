package cn.cyanbukkit.speed.game.task

import cn.cyanbukkit.speed.game.GameStatus
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.utils.Teacher
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class EnderDragonTask(private val dragon: Teacher?) : Runnable {

    private val radius: Double = 50.0 // 转圈的半径
    private var angle: Double = 0.0 // 角度，以Double为单位

    override fun run() {
        if (gameStatus == GameStatus.BUILDING || gameStatus == GameStatus.OBSERVING) {
            if (dragon == null) {
                return
            }
            if (dragon.bukkitEntity.isDead) {
                return
            }
            if (gameStatus != GameStatus.WAITING) {
                val center = dragon.bukkitEntity.location
                val newAngleRad = Math.toRadians(angle)
                val direction = Vector(
                    radius * cos(newAngleRad),
                    0.0,
                    radius * sin(newAngleRad)
                )
                // 计算目标位置
                val targetLocation = center.clone().add(direction)
                // 移动EnderDragon到新位置
                dragon.bukkitEntity.teleport(targetLocation)
                // 更新角度
                angle += 10.0
                if (angle >= 360) {
                    angle = 0.0 // 重置角度为0
                }
            }
        }
    }
}
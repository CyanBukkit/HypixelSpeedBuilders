package cn.cyanbukkit.speed.game.task

import cn.cyanbukkit.speed.game.GameStatus
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.utils.Teacher
import org.bukkit.Location
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class EnderDragonTask(
    private val dragon: Teacher?,
    private val centerLocation: Location
) : Runnable {

    private val radius: Double = 50.0
    private var angle: Double = 0.0
    private val angleIncrement: Double = 2.0 // 减小角度增量使运动更平滑
    private val heightOffset: Double = 20.0
    private val flySpeed: Double = 1.0 // 飞行速度系数

    override fun run() {
        if (dragon == null || dragon.bukkitEntity.isDead) {
            return
        }

        if (gameStatus == GameStatus.BUILDING || gameStatus == GameStatus.OBSERVING) {
            val currentLocation = dragon.bukkitEntity.location
            val angleRadians = Math.toRadians(angle)

            // 计算目标位置
            val targetX = centerLocation.x + radius * cos(angleRadians)
            val targetZ = centerLocation.z + radius * sin(angleRadians)
            val targetY = centerLocation.y + heightOffset

            val targetLocation = Location(
                centerLocation.world,
                targetX,
                targetY,
                targetZ
            )

            // 计算从当前位置到目标位置的方向向量
            val direction = targetLocation.toVector().subtract(currentLocation.toVector())

            // 计算朝向中心点的方向
            val lookDirection = centerLocation.toVector()
                .subtract(currentLocation.toVector())
                .normalize()

            // 设置龙的朝向
            val yaw = Math.toDegrees(
                atan2(-lookDirection.x, lookDirection.z)
            ).toFloat()
            val pitch = Math.toDegrees(
                asin(lookDirection.y)
            ).toFloat()

            // 应用朝向
            currentLocation.yaw = yaw
            currentLocation.pitch = pitch
            dragon.bukkitEntity.teleport(currentLocation)

            // 设置速度向量来实现平滑移动
            val velocity = direction.normalize().multiply(flySpeed)
            dragon.bukkitEntity.velocity = velocity

            // 更新角度
            angle += angleIncrement
            if (angle >= 360.0) {
                angle = 0.0
            }
        }
    }
}
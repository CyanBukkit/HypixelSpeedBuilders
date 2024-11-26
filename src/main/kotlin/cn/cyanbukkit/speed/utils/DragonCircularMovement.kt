package cn.cyanbukkit.dragon.movement

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.ArenaIslandData
import cn.cyanbukkit.speed.game.GameHandle.worldEditRegion
import net.minecraft.server.v1_8_R3.EntityEnderDragon
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderDragon
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.EntityType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.*

class DragonCircularMovement(
    private val plugin: JavaPlugin,
    private val dragon: EntityEnderDragon,
    private val centerLocation: Location,
    private val radius: Double = 50.0,
    private var speed: Double = 0.05,
    private var height: Double = 0.0
) {
    private var angle: Double = 0.0
    private var bukkitRunnable: BukkitRunnable? = null
    private var lastLocation: Location? = null
    private val dragons = mutableListOf<EnderDragon>()


    fun eatIsland(isl: ArenaIslandData) {
        val direction = isl.middleBlock.location.toVector().subtract(dragon.bukkitEntity.location.toVector()).normalize()
        val newLocation = dragon.bukkitEntity.location.clone().add(direction.clone().multiply(0.0))
        val yaw = Math.toDegrees(atan2(direction.z, direction.x)).toFloat() - 90
        val pitch = Math.toDegrees(asin(direction.y)).toFloat()
        newLocation.yaw = yaw
        newLocation.pitch = pitch
        val dragon1 = newLocation.world.spawnEntity(newLocation, EntityType.ENDER_DRAGON) as EnderDragon
        val craftDragon = (dragon1 as CraftEnderDragon).handle
        craftDragon.customName = "§c§lJudge Dragon"
        craftDragon.customNameVisible = true
        dragons.add(dragon1)
        dragon1.world.playSound(dragon1.location,Sound.ENDERDRAGON_GROWL,3f,1f)
        smoothDragonMovement(dragon1, isl.middleBlock.location) {
            worldEditRegion(isl)
        }
    }


    fun smoothDragonMovement(dragon: EnderDragon, targetLocation: Location, un:  () -> Unit = { } ) {
        val startLocation = dragon.location
        val movementDuration = 40L // 2秒 (20 ticks/second * 2)

        object : BukkitRunnable() {
            var tick = 0

            override fun run() {
                if (tick >= movementDuration) {
                    dragon.teleport(targetLocation.apply {
                        yaw = calculateYaw(startLocation, targetLocation)
                        pitch = calculatePitch(startLocation, targetLocation)
                    })
                    dragon.health = 0.0
                    dragon.remove()
                    un()
                    this.cancel()
                    return
                }

                // 使用平滑的插值函数（三次缓动）
                val progress = tick.toFloat() / movementDuration
                val smoothProgress = -2 * progress * progress * progress + 3 * progress * progress

                val interpolatedLocation = startLocation.clone().add(
                    targetLocation.x.minus(startLocation.x) * smoothProgress,
                    targetLocation.y.minus(startLocation.y) * smoothProgress,
                    targetLocation.z.minus(startLocation.z) * smoothProgress
                )

                // 计算平滑的角度
                val currentYaw = startLocation.yaw + (calculateYaw(startLocation, targetLocation) - startLocation.yaw) * smoothProgress + 180
                val currentPitch = startLocation.pitch + (calculatePitch(startLocation, targetLocation) - startLocation.pitch) * smoothProgress

                interpolatedLocation.yaw = currentYaw
                interpolatedLocation.pitch = currentPitch

                dragon.teleport(interpolatedLocation)
                tick++
            }
        }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 1L)
    }

    // 计算水平方向角度（Yaw）
    fun calculateYaw(from: Location, to: Location): Float {
        val dx = to.x - from.x
        val dz = to.z - from.z
        val angle = Math.toDegrees(atan2(dz, dx)).toFloat()
        return angle - 90
    }

    // 计算垂直方向角度（Pitch）
    fun calculatePitch(from: Location, to: Location): Float {
        val distance = from.distance(to)
        val dy = to.y - from.y
        return Math.toDegrees(asin(dy / distance)).toFloat()
    }


    fun startMoving() {
        bukkitRunnable = object : BukkitRunnable() {
            override fun run() {
                if (dragon.bukkitEntity.isDead) {
                    this.cancel()
                    return
                }

                // 计算当前位置 - 使用负角度实现逆时针运动
                val x = centerLocation.x + radius * cos(-angle)
                val z = centerLocation.z + radius * sin(-angle)
                val currentY = centerLocation.y + height
                val currentLocation = Location(
                    centerLocation.world,
                    x,
                    currentY,
                    z
                )

                // 计算下一个位置（用于确定移动方向）
                val nextAngle = angle + speed
                val nextX = centerLocation.x + radius * cos(-nextAngle)
                val nextZ = centerLocation.z + radius * sin(-nextAngle)

                // 如果存在上一个位置，使用它来计算更平滑的方向
                val prevLocation = lastLocation ?: currentLocation

                // 计算移动向量
                val directionX = nextX - x
                val directionZ = nextZ - z
                val directionY = currentY - prevLocation.y

                // 计算水平面上的yaw角度
                val yaw = Math.toDegrees(atan2(-directionX, directionZ)).toFloat()

                // 计算垂直movement的pitch角度
                // 使用3D向量来计算pitch
                val horizontalDistance = sqrt(directionX * directionX + directionZ * directionZ)
                val pitch = Math.toDegrees(atan2(-directionY, horizontalDistance)).toFloat()

                // 设置龙的朝向
                currentLocation.yaw = yaw + 180 // 设置yaw角度
                currentLocation.pitch = pitch.coerceIn(-30f, 30f) // 限制pitch角度范围，使动作更自然

                // 移动龙到新位置
                dragon.bukkitEntity.teleport(currentLocation)

                // 保存当前位置用于下一帧计算
                lastLocation = currentLocation

                // 更新角度
                angle += speed
                if (angle >= 2 * PI) {
                    angle = 0.0
                }
            }
        }
        bukkitRunnable?.runTaskTimer(plugin, 0L, 3L)
    }

    fun stopMoving() {
        bukkitRunnable?.cancel()
        bukkitRunnable = null
        lastLocation = null
    }

    fun setHeight(newHeight: Double) {
        height = newHeight
    }

    fun setSpeed(newSpeed: Double) {
        speed = newSpeed
    }

    fun getCurrentAngle(): Double = angle
}
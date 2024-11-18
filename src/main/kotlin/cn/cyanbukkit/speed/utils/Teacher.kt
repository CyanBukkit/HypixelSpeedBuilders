package cn.cyanbukkit.speed.utils

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.task.GameVMData.nowMap
import com.google.common.base.Predicate
import net.minecraft.server.v1_8_R3.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class Teacher(world: World) : EntityEnderDragon(world) {


    init {
        this.health = 300F
        this.maxNoDamageTicks = 10
        this.customName = "Jude Dragon"
        this.targetSelector = PathfinderGoalSelector(world.methodProfiler)
        this.goalSelector = PathfinderGoalSelector(world.methodProfiler)

    }

//    //无目标
//    override fun m() {
//        //遍历玩家距离实体10格以内的玩家
//
//    }

    private fun getNearbyPlayers(location: Location, radius: Double): List<Player> {
        val players: MutableList<Player> = ArrayList()
        for (e in location.world.getNearbyEntities(location, radius, radius, radius)) {
            if (e is Player && e.getLocation().distance(location) <= radius) {
                players.add(e)
            }
        }
        return players
    }

    fun rotateGuardian(locationYaw: Float) {
        val craftGuardian = this.bukkitEntity
        val nbtTagCompound = NBTTagCompound();
        craftGuardian.handle.c(nbtTagCompound);
        nbtTagCompound.setInt("NoAI", 0);
        craftGuardian.handle.f(nbtTagCompound);
        val location = this.bukkitEntity.location
        if (locationYaw == 7.5F) {
            location.yaw += 7.5F;
        } else {
            location.yaw = 0.0F;
        }
        this.bukkitEntity.teleport(location);
        craftGuardian.handle.c(nbtTagCompound);
        nbtTagCompound.setInt("NoAI", 1);
        craftGuardian.handle.f(nbtTagCompound);
    }

    fun upDataWatch(loser: Player): DataWatcher? {// 更新datawatcher
        val dWatcher = this.dataWatcher
        try {
            val watchMethod: Method = dWatcher.javaClass.getMethod("watch", Integer.TYPE, Any::class.java)
            watchMethod.invoke(dWatcher, 17, loser.entityId)
        } catch (_: IllegalArgumentException) {
        } catch (_: InvocationTargetException) {
        } catch (_: NoSuchMethodException) {
        } catch (_: SecurityException) {
        } catch (_: IllegalAccessException) {
        }

        return dWatcher

    }


    fun clearWatch(): DataWatcher {
        val dWatcher = this.dataWatcher
        try {
            val watchMethod: Method = dWatcher.javaClass.getMethod("watch", Integer.TYPE, Any::class.java)
            watchMethod.invoke(dWatcher, 17, 0)
        } catch (_: IllegalArgumentException) {
        } catch (_: InvocationTargetException) {
        } catch (_: NoSuchMethodException) {
        } catch (_: SecurityException) {
        } catch (_: IllegalAccessException) {
        }

        return dWatcher
    }

    fun sendHit(player: List<Player>) {
        val craftGuardian = this.bukkitEntity
        val nbtTagCompound = NBTTagCompound();
        craftGuardian.handle.c(nbtTagCompound);
        nbtTagCompound.setInt("NoAI", 0);
        craftGuardian.handle.f(nbtTagCompound);
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
            val map = nowMap[SpeedBuildReloaded.instance]!!
            this.bukkitEntity.teleport(map.middleIsland.toLocation())
            craftGuardian.handle.c(nbtTagCompound);
            nbtTagCompound.setInt("NoAI", 1);
            craftGuardian.handle.f(nbtTagCompound);
        }, 40L)
    }

    internal class EntitySelectorGuardianTargetHumanSquid(private val a: EntityGuardian) :
        Predicate<EntityLiving> {

        override fun apply(p0: EntityLiving?): Boolean {
            return (p0 is EntityHuman || p0 is EntitySquid) && p0.h(
                this.a
            ) > 9.0
        }
    }


}
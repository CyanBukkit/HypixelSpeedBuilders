package cn.cyanbukkit.speed.game

import net.minecraft.server.v1_8_R3.DamageSource
import net.minecraft.server.v1_8_R3.NBTTagCompound
import net.minecraft.server.v1_8_R3.World
import org.bukkit.entity.Player

@Deprecated("这个类已经被废弃了")
class NMS(p0: World) : net.minecraft.server.v1_8_R3.EntityGuardian(p0) {


    override fun setCustomName(s: String?) {
        super.setCustomName("监考老师")
    }

    override fun setCustomNameVisible(flag: Boolean) {
        super.setCustomNameVisible(true)
    }

    //实体伤害
    override fun damageEntity(p0: DamageSource?, p1: Float): Boolean {
        return false
    }

    //实体移动
    override fun move(d0: Double, d1: Double, d2: Double) {
        //
    }

    //发动音效
    override fun makeSound(s: String?, f: Float, f1: Float) {
        //
    }

//     fun spawnGuardian(loc : Location) : Guardian {
//        val mcWorld = loc.world as CraftWorld
//        val customEntity = NMS(mcWorld)
//        customEntity.setPosition(loc.x, loc.y, loc.z)
//        customEntity.aK = loc.yaw
//        customEntity.yaw = loc.yaw
//        customEntity.pitch = loc.pitch
//        (customEntity.getBukkitEntity() as CraftLivingEntity).removeWhenFarAway = false
//        mcWorld.handle.addEntity(customEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)
//        removeSounds(customEntity)
//        removeAI(customEntity)
//        (customEntity.getBukkitEntity() as CraftGuardian).isElder = true
//        return customEntity.getBukkitEntity() as Guardian
//    }


    private fun removeSounds(customEntity: NMS) {
        val craftEntity = customEntity.getBukkitEntity()
        val nbtTag = NBTTagCompound()
        craftEntity.handle.c(nbtTag)
        nbtTag.setInt("Silent", 1)
        craftEntity.handle.f(nbtTag)
    }

    private fun removeAI(customEntity: NMS) {
        val craftEntity = customEntity.getBukkitEntity()
        val nbtTag= NBTTagCompound()
        craftEntity.handle.c(nbtTag)
        nbtTag.setInt("NoAI", 1)
        craftEntity.handle.f(nbtTag)
    }

    fun sendLook(p: Player) {
        // 看人的包
    }


}
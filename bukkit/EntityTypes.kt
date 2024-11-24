package cn.cyanbukkit.speed.utils

import net.minecraft.server.v1_8_R3.Entity
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent


enum class EntityTypes(
    n: String, id: Int, custom: Class<out Entity>
) {
    GUARDIAN("EnderDragon", 63, ::class.java);

    init {
        addToMaps(custom, n, id)
    }

    fun spawnEntity(entity: Entity, loc: Location) {
        entity.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        (loc.world as CraftWorld).handle.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    private fun getPrivateField(fieldName: String, clazz: Class<*>, obj: Any?): Any? {
        return try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(obj)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addToMaps(clazz: Class<*>, name: String, id: Int) {
        (getPrivateField("c", net.minecraft.server.v1_8_R3.EntityTypes::class.java, null) as MutableMap<String, Class<*>>)[name] = clazz
        (getPrivateField("d", net.minecraft.server.v1_8_R3.EntityTypes::class.java, null) as MutableMap<Class<*>, String>)[clazz] = name
        (getPrivateField("f", net.minecraft.server.v1_8_R3.EntityTypes::class.java, null) as MutableMap<Class<*>, Int>)[clazz] = id
    }


}
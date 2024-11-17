package cn.cyanbukkit.speed.data

import org.bukkit.Bukkit
import org.bukkit.Location

data class LocationString(
    val loc: String
) {
    fun toLocation(world: String): Location {
        val split = loc.split(",")
        return when (split.size) {
            3 -> Location(Bukkit.getWorld(world), split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
            5 -> Location(Bukkit.getWorld(world), split[0].toDouble(), split[1].toDouble(), split[2].toDouble(), split[3].toFloat(), split[4].toFloat())
            else -> throw IllegalArgumentException("LocationString must be 4 or 6")
        }
    }
}
package cn.cyanbukkit.speed.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class Utils {

    fun getNearbyPlayers(location: Location, radius: Double): List<Player> {
        val players: MutableList<Player> = ArrayList()
        for (e in location.world.getNearbyEntities(location, radius, radius, radius)) {
            if (e is Player && e.getLocation().distance(location) <= radius) {
                players.add(e)
            }
        }
        return players
    }

}





/**
 * 模拟进度条 25个▇  按照  分数比 返回  绿色▇（已完成的部分§a） 与红色▇（未完成的部分 颜色§c）
 */
fun Player.getProgressBar(mess: String, time: Double, maxTime: Double) {
    val percent = time / maxTime
    val progress = 25 * percent
    val green = "§a"
    val red = "§c"
    val sb = StringBuilder()
    for (i in 0 until progress.toInt()) {
        sb.append(green).append("▌")
    }
    for (i in 0 until 25 - progress.toInt()) {
        sb.append(red).append("▌")
    }
    Title.actionbar(this, "$mess §7${sb} §f${time}s")
}



fun Player.connectTo(server: String, plugin: Plugin) {
    Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord")
    val buf = ByteArrayOutputStream()
    val out = DataOutputStream(buf)
    try {
        out.writeUTF("Connect")
        out.writeUTF(server)
        sendPluginMessage(plugin, "BungeeCord", buf.toByteArray())
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
}
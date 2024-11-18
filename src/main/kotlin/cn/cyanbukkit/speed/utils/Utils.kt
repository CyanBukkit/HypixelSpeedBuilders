package cn.cyanbukkit.speed.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException


/**
 * 模拟进度条 25个▇  按照  分数比 返回  绿色▇（已完成的部分§a） 与红色▇（未完成的部分 颜色§c）
 */
fun Player.getProgressBar(mess: String, time: Int, maxTime: Int) {
    val percent = time / maxTime
    val progress = 25 * percent
    val green = "§a§l"
    val red = "§c§l"
    val sb = StringBuilder()
    for (i in 0 until progress) {
        sb.append(green).append("|")
    }
    for (i in 0 until 25 - progress) {
        sb.append(red).append("|")
    }
    Title.actionbar(this, "$mess §7${sb} §f${time}s 结束")
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
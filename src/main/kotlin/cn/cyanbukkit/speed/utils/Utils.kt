package cn.cyanbukkit.speed.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

fun Player.send(
    mess: String,
    time: Int,
    title: Boolean = false,
    subTitle: Boolean = false,
    actionBar: Boolean = false,
    sound: Sound = Sound.NOTE_PLING,
    volume: Float = 1f,
    pitch: Float = 1f
) {
    val message = mess.replace("%time%", time.toString())
    this.sendMessage(message)
    if (title) {
        Title.title(this, message, "")
    }
    if (subTitle) {
        Title.title(this, "", message)
    }
    if (actionBar) {
        Title.actionbar(this, message)
    }

    this.playSound(this.location, sound, volume, pitch)
    this.level = time
}


/**
 * 模拟进度条，使用25个▇显示进度
 * @param message 显示的信息
 * @param currentTime 当前时间
 * @param maxTime 最大时间
 */
fun Player.showProgressBar(
    message: String,
    currentTime: Int,
    maxTime: Int
) {
    val percent = currentTime.toFloat() / maxTime.toFloat()
    val progressCount = (25 * percent).toInt()

    val progressBar = buildString {
        // 使用 Kotlin 的 repeat 函数替代 for 循环
        repeat(progressCount) {
            append("§a§l|")  // 绿色已完成部分
        }
        repeat(25 - progressCount) {
            append("§c§l|")  // 红色未完成部分
        }
    }

    // 使用字符串模板
    Title.actionbar(this, "$message §7$progressBar §f${currentTime}s 结束")
}


fun Location.toConfig(): String {
    return "${world.name},${x},${y},${z},${yaw},${pitch}"
}
fun Block.toConfig(): String {
    return "${world.name},${x},${y-1},${z}"
}
fun String.toLocation(): Location {
    val split = split(",")

    when (split.size) {
        6 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                split[1].toDouble(),
                split[2].toDouble(),
                split[3].toDouble(),
                split[4].toFloat(),
                split[5].toFloat()
            )
        }
        5 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                split[1].toDouble(),
                split[2].toDouble(),
                split[3].toDouble(),
                split[4].toFloat(),
                0f
            )
        }

        4 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                split[1].toDouble(),
                split[2].toDouble(),
                split[3].toDouble()
            )
        }

        3 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                split[1].toDouble(),
                split[2].toDouble(),
                0.0
            )
        }

        2 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                split[1].toDouble(),
                0.0,
                0.0
            )
        }

        1 -> {
            return Location(
                Bukkit.getWorld(split[0]),
                0.0,
                0.0,
                0.0
            )
        }

        else -> {
            return Location(
                Bukkit.getWorlds()[0],
                0.0,
                0.0,
                0.0
            )
        }

    }
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
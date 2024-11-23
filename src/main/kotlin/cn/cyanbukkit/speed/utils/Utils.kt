package cn.cyanbukkit.speed.utils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
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
    enabledSound: Boolean = true,
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


fun Location.toConfig(): String {
    return "${world.name},${x},${y},${z},${yaw},${pitch}"
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
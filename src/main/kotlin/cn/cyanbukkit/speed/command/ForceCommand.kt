package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.task.GameTask
import cn.cyanbukkit.speed.task.GameVMData.buildSign
import cn.cyanbukkit.speed.task.GameVMData.configSettings
import cn.cyanbukkit.speed.task.GameVMData.nowMap
import cn.cyanbukkit.speed.task.GameVMData.nowTask
import cn.cyanbukkit.speed.task.GameVMData.playerStatus
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ForceCommand : Command("force") {

    init {
        permission = "speedbuildreloaded.force"
    }

    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        if (!p0.hasPermission(permission)) {
            p0.sendMessage(configSettings!!.mess.noPermission)
            return true
        }
        // force start
        if (p2.isEmpty()) {
            p0.sendMessage("§c start / stop")
            return true
        }
        when (p2[0]) {
            "start" -> {
                if (buildSign.isNotEmpty()) {
                    p0.sendMessage("§c 已经启动建图模式了 所以别开了重启服务器得了")
                    return true
                }

                val list = mutableListOf<Player>()
                playerStatus.forEach { (player, playerStatus) ->
                    run {
                        if (playerStatus == cn.cyanbukkit.speed.data.PlayerStatus.WAITING) {
                            list.add(player)
                        }
                    }
                }
                GameTask.startGame(list, nowMap[SpeedBuildReloaded.instance]!!)
                p0.sendMessage("§a 已经强制开启游戏")
            }
            "stop" -> {
                if (Bukkit.getScheduler().isCurrentlyRunning(nowTask)) {
                    Bukkit.getScheduler().cancelTask(nowTask)
                }
                // 开启建造
                buildSign.add(p0 as Player)
                p0.sendMessage("§a 已经强制关闭游戏你可以开始编辑地图了")
            }

            "addbuild" -> {
                val name = Bukkit.getPlayer(p2[1])?:return true
                // 开启建造
                buildSign.add(name)
                p0.sendMessage("§a 添加建造者成功")
            }

            else -> {
                p0.sendMessage("§c start / stop")
            }
        }
        return true
    }
}
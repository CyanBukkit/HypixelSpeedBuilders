package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.checkTask
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.GameStatus
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.LoaderData
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GameCheckTask(private val arena: ArenaSettingData) : Runnable {
    override fun run() {
        if (LoaderData.gameStatus[arena] == GameStatus.END) {
            if (Bukkit.getScheduler().isCurrentlyRunning(checkTask[SpeedBuildReloaded.instance]!!)) {
                Bukkit.getScheduler().cancelTask(checkTask[SpeedBuildReloaded.instance]!!)
            }
            return
        }

        val playerList = mutableListOf<Player>()

        LoaderData.playerStatus.forEach { (player, playerStatus) ->
            run {
                if (playerStatus == PlayerStatus.LIFE) {
                    playerList.add(player)
                }
            }
        }

        if (playerList.size <= 1) {
            GameInitTask.stopGame(arena)
            return
        }

    }
}
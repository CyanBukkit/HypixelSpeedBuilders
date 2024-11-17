package cn.cyanbukkit.speed.utils

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.GameStatus
import cn.cyanbukkit.speed.game.LoaderData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent

class MotdListener : Listener {

    @EventHandler
    fun onPing(e: ServerListPingEvent) {
        if (LoaderData.nowMap.isEmpty() || LoaderData.gameStatus.isEmpty()) {
            e.motd = "NoMap :("
            return
        }
        val map = LoaderData.nowMap[SpeedBuildReloaded.instance]
        if (LoaderData.gameStatus[map] == GameStatus.WAITING) {
            e.motd = "Waiting..."
        }else if (LoaderData.gameStatus[map] != GameStatus.STARTING
            || LoaderData.gameStatus[map] != GameStatus.OBSERVING
            || LoaderData.gameStatus[map] != GameStatus.BUILDING
            || LoaderData.gameStatus[map] != GameStatus.SCORE
            || LoaderData.gameStatus[map] != GameStatus.END){
            e.motd = "NoMap :("
        }else{
            e.motd = "Starting"
        }
    }
}
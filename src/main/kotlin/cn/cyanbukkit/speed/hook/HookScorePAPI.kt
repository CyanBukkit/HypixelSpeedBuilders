package cn.cyanbukkit.speed.hook

import cn.cyanbukkit.speed.task.GameVMData.storage
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class HookScorePAPI : PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "speedbuilder"
    }

    override fun getAuthor(): String {
        return "CyanBukkit"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }


    override fun onPlaceholderRequest(player: Player, params: String): String {
        when  {
            params.equals("wins" ,true)-> return storage.getWins(player).toString()
            params.equals("eliminate" ,true) -> return storage.getEliminate(player).toString()
            params.equals("restore_build"  ,true)-> return storage.getRestoreBuild(player).toString()
            params.split("_").size == 2 -> {
                val mapString = params.split("_")[0]
                val type = params.split("_")[1]
                if (mapString.equals("fastestbuildtime", true)) {
                    return storage.getFastestBuildTime(player, type).toString()
                }
            }
        }
        return ""
    }
}
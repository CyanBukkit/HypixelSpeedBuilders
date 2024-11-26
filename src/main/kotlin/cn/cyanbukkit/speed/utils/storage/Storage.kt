package cn.cyanbukkit.speed.utils.storage

import org.bukkit.entity.Player

interface Storage {

    fun link()

    fun reload()

    fun addWins(p: Player)
    fun getWins(p: Player) : Int

    fun addEliminate(p: Player)
    fun getEliminate(p: Player) : Int

    fun addRestoreBuild(p: Player)
    fun getRestoreBuild(p: Player) : Int


    fun setFastestBuildTime(p: Player, newValue : Double, mapString: String)
    fun getFastestBuildTime(p: Player, mapString: String) : Double

    fun onDefault(p: Player)

    fun getNowIslandTemplate(p: Player) : String
    fun setIslandTemplate(p: Player, useName: String)
    fun unlockIslandTemplateList(p: Player) : MutableList<String>
    fun unlockIslandTemplate(p: Player, name: String)

}
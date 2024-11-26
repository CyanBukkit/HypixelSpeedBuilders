package cn.cyanbukkit.speed.utils.storage

import cn.cyanbukkit.speed.SpeedBuildReloaded
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class YamlLink : Storage {

    lateinit var playerDataFile: File
    private lateinit var playerData: YamlConfiguration


    override fun link() {
        playerDataFile = File(SpeedBuildReloaded.instance.dataFolder, "playerdata.yml")
        if (!playerDataFile.exists()) { // 释放文件
            SpeedBuildReloaded.instance.saveResource("playerdata.yml", false)
        }
        playerData = YamlConfiguration.loadConfiguration(playerDataFile)
    }

    override fun reload() {
        playerData.load(playerDataFile)
    }

    override fun addWins(p: Player) {
        val wins = playerData.getInt("${p.uniqueId}.Wins")
        playerData.set("${p.uniqueId}.Wins", wins + 1)
        playerData.save(playerDataFile)
    }

    override fun getWins(p: Player): Int {
        return playerData.getInt("${p.uniqueId}.Wins")
    }

    override fun addEliminate(p: Player) {
        val eliminate = playerData.getInt("${p.uniqueId}.Eliminate")
        playerData.set("${p.uniqueId}.Eliminate", eliminate + 1)
        playerData.save(playerDataFile)
    }

    override fun getEliminate(p: Player): Int {
        return playerData.getInt("${p.uniqueId}.Eliminate")
    }

    override fun addRestoreBuild(p: Player) {
        val restoreBuild = playerData.getInt("${p.uniqueId}.RestoreBuild")
        playerData.set("${p.uniqueId}.RestoreBuild", restoreBuild + 1)
        playerData.save(playerDataFile)
    }

    override fun getRestoreBuild(p: Player): Int {
        return playerData.getInt("${p.uniqueId}.RestoreBuild")
    }

    override fun setFastestBuildTime(p: Player, newValue: Double, mapString: String) {

        // 获取现在的
        val value = playerData.getString("${p.uniqueId}.FastestBuildTime")
        val s = YamlConfiguration()
        s.loadFromString(value)
        val old = s.getInt(mapString)
        if (old > newValue) {
            s.set(mapString, newValue)
            playerData.set("${p.uniqueId}.FastestBuildTime", s.saveToString())
            p.sendMessage("§a你打破了${mapString}建造自己的纪录！")
            playerData.save(playerDataFile)
        } else if (old == 0 ) {
            s.set(mapString, newValue)
            playerData.set("${p.uniqueId}.FastestBuildTime", s.saveToString())
            p.sendMessage("§a你打破了${mapString}建造自己的纪录！")
            playerData.save(playerDataFile)
        }
    }

    override fun getFastestBuildTime(p: Player, mapString: String): Double {
        val value = playerData.getString("${p.uniqueId}.FastestBuildTime")
        val s = YamlConfiguration()
        s.loadFromString(value)
        return s.getDouble(mapString)
    }

    override fun onDefault(p: Player) {
        if (playerData.contains("${p.uniqueId}")) {
            return
        }
        playerData.set("${p.uniqueId}.Wins", 0)
        playerData.set("${p.uniqueId}.Eliminate", 0)
        playerData.set("${p.uniqueId}.RestoreBuild", 0)
        val s = YamlConfiguration()
        s.set("xx", 9999)
        val txt = s.saveToString()
        playerData.set("${p.uniqueId}.FastestBuildTime", txt)
        playerData.save(playerDataFile)
    }

    override fun getNowIslandTemplate(p: Player): String {
        return playerData.getString("${p.uniqueId}.IslandTemplate") ?: "default"
    }

    override fun setIslandTemplate(p: Player, useName: String) {
        playerData.set("${p.uniqueId}.IslandTemplate", useName)
        playerData.save(playerDataFile)
    }

    override fun unlockIslandTemplateList(p: Player): MutableList<String> {
        val list = playerData.getStringList("${p.uniqueId}.UnlockIslandTemplateList")
        return list.toMutableList()
    }

    override fun unlockIslandTemplate(p: Player, name: String) {
        val list = playerData.getStringList("${p.uniqueId}.UnlockIslandTemplateList")
        list.add(name)
        playerData.set("${p.uniqueId}.UnlockIslandTemplateList", list)
        playerData.save(playerDataFile)
    }

}
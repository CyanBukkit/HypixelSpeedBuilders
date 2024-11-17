package cn.cyanbukkit.speed

import cn.cyanbukkit.speed.command.HelpCommand
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.hook.HookSocrePAPI
import cn.cyanbukkit.speed.utils.RestoreMap
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.command.Command
import org.bukkit.command.SimpleCommandMap
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SpeedBuildReloaded : JavaPlugin() {
    // 差两个玩家统计的

    companion object {
        lateinit var instance: SpeedBuildReloaded

        var checkTask = mutableMapOf<SpeedBuildReloaded, Int>()

        fun Command.register() {
            val pluginManagerClazz = instance.server.pluginManager.javaClass
            val field = pluginManagerClazz.getDeclaredField("commandMap")
            field.isAccessible = true
            val commandMap = field.get(instance.server.pluginManager) as SimpleCommandMap
            commandMap.register(instance.name, this)
        }


        fun Listener.register() {
            instance.server.pluginManager.registerEvents(this, instance)
        }


        fun String.cc(p: Player) : String {
            return PlaceholderAPI.setPlaceholders(p,this.replace("&", "§"))
        }


    }


    lateinit var blockTemplateFile: File
    lateinit var settingsFile: File

    lateinit var blockTemplate: YamlConfiguration
    lateinit var settings: YamlConfiguration

    override fun onEnable() {
        instance = this
        RestoreMap.init()
        // Start
        checkTask[this] = 0
        HelpCommand().register()
        HookSocrePAPI().register()
        logger.info("SpeedBuildReloaded is enabled!")
        logger.info("SpeedBuildReloaded by CyanBukkit Code")
        logger.info("SpeedBuildReloaded 前期工作也已经完成加载处理现成") // 保存默认配置
        saveDefaultConfig()
    }



    override fun saveDefaultConfig() {
        super.saveDefaultConfig()
        // 加载其他配置文件
        blockTemplateFile = File(dataFolder, "blocktemplate.yml")
        if (!blockTemplateFile.exists()) { // 释放文件
            saveResource("blocktemplate.yml", false)
        }
        blockTemplate = YamlConfiguration.loadConfiguration(blockTemplateFile)
        settingsFile = File(dataFolder, "settings.yml")
        if (!settingsFile.exists()) { // 释放文件
            saveResource("settings.yml", false)
        }
        settings = YamlConfiguration.loadConfiguration(settingsFile)
        // 加载至缓存
        // 最后显示
        try {
            LoaderData.init()
        } catch (e: Exception) {
            settingsFile.copyTo(File(dataFolder, "settings.yml.bak.${(1..10000).random()}"), true)
            settingsFile.delete()
            logger.info("都不完整留他干啥呀")
            e.printStackTrace()
        }
    }

    override fun reloadConfig() {
        super.reloadConfig()
        blockTemplate.load(blockTemplateFile)
        settings.load(settingsFile)
    }

}
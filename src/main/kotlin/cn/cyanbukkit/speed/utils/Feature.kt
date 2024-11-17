package cn.cyanbukkit.speed.utils

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

open class Feature internal constructor() : Listener {
    protected var plugin: Plugin? = null
    fun init(plugin: Plugin) {
        this.plugin = plugin
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun validatePlugin() {
        if (plugin == null) {
            throw IllegalStateException("Please use ${this.javaClass.simpleName}.init(plugin) to initialize this feature.")
        }
    }
}
package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.game.LoaderData
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class HelpCommand() : Command("help") {

    init {
        // 总帮助信息
        permission = "speedbuildreloaded.help"
    }

    override fun execute(sender: CommandSender, command: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(LoaderData.configSettings!!.mess.noPermission)
            return true
        }


        sender.sendMessage("""
            CyanBukkit 插件
            
            
            /addtemplate 设置建筑模板
            /speedsetup/ssu  
                         设置房间
            /top         排行榜
            /force       强制开启当前游戏
            /help reload 重载配置文件(仅读取不更改内部task)
            
        """.trimIndent())

        if (args.isNotEmpty()) {
            if (args[0] == "reload") {
                SpeedBuildReloaded.instance.reloadConfig()
                sender.sendMessage("§a 重载配置文件成功")
            }
        }

        return true
    }

}
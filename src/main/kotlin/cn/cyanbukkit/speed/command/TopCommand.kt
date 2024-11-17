package cn.cyanbukkit.speed.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class TopCommand : Command("top"){
    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        // win  round
        return true
    }
}
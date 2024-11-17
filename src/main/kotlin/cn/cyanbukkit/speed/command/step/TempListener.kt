package cn.cyanbukkit.speed.command.step

import cn.cyanbukkit.speed.command.step.AllStep.stepMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class TempListener() : Listener {

    @EventHandler
    fun onPos1(e: BlockBreakEvent) {
        // 用什么物品 来破坏方块
        stepMap.keys.contains(e.player).let {
            if (it) {
                e.isCancelled = true
                // 获取步骤 进行保存
                if (stepMap[e.player] == 0) {
                    // 保存第一个点
                }
            }
        }
    }


    @EventHandler
    fun onPos2(e: PlayerInteractEvent) {
        // 用什么物品 来破坏方块
        stepMap.keys.contains(e.player).let {
            if (it) {
                e.isCancelled = true
                // 获取步骤 进行保存
                if (stepMap[e.player] == 0) {
                    // 保存第一个点
                }
            }
        }
    }


    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        // 用什么物品 来破坏方块
        stepMap.keys.contains(e.player).let {
            if (it) {
                e.isCancelled = true
                // 获取步骤 进行保存
                if (stepMap[e.player] == 1) {
                    // 保存第一个点
                }
            }
        }
    }



    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        stepMap.remove(e.player)
    }


}
package cn.cyanbukkit.speed.command.setup

import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object SetUpArena : Listener {

    lateinit var left: Block
    lateinit var right: Block

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onIntact(e: PlayerInteractEvent) {
        // 手里的工具是不是 BLAZE_ROD
        if (e.player.inventory.itemInHand.type.name == "BLAZE_ROD"
            && e.player.inventory.itemInHand.hasItemMeta()
            && e.player.inventory.itemInHand.itemMeta.hasDisplayName()
            && e.player.inventory.itemInHand.itemMeta.displayName.equals("§b绑图工具")
        ) {
            e.isCancelled = true
            // 左键 Pos1 右键 Pos2
            if (e.action.name == "LEFT_CLICK_BLOCK") {
                val b = e.clickedBlock
                // 保存第一个点
                left = b
                e.player.sendMessage("§b[SpeedBuild]§6已设置第一个点(${b.x},${b.y},${b.z} ${b.world.name})")
            } else if (e.action.name == "RIGHT_CLICK_BLOCK") {
                val b = e.clickedBlock
                // 保存第二个点
                right = b
                e.player.sendMessage("§b[SpeedBuild]§6已设置第二个点(${b.x},${b.y},${b.z} ${b.world.name})")
            }
        }
    }


    fun isLateInit(): Boolean {
        return SetUpArena::left.isInitialized && SetUpArena::right.isInitialized
    }

}
package cn.cyanbukkit.speed.utils

import net.minecraft.server.v1_8_R3.Item
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

object CompleteBlock {

    // 把各种半砖活版门 门花等非完整方块的Mateiral收集到一起
    // 用于判断是否是半砖
    val list = mutableListOf<Material>().apply {
        add(Material.STONE)
        add(Material.DIRT)
        add(Material.WOOD)
        add(Material.LOG)
        add(Material.WOOL)
        add(Material.GLASS)
    }

    /**
     * 根据Block获取物品
     */
    fun org.bukkit.block.Block.toItemStack(): ItemStack {
        val block: CraftBlock = this as CraftBlock
        val nmsBlock: net.minecraft.server.v1_8_R3.Block = try {
            val craftBlockClass = block::class.java
            val getNMSBlockMethod: Method = craftBlockClass.getDeclaredMethod("getNMSBlock")
            getNMSBlockMethod.isAccessible = true
            getNMSBlockMethod.invoke(block) as net.minecraft.server.v1_8_R3.Block
        } catch (e: Exception) {
            e.printStackTrace()
            return ItemStack(Material.AIR)
        }
        val worlds: CraftWorld = block.world as CraftWorld
//        val item = nmsBlock.getDropType(nmsBlock.fromLegacyData(block.data.toInt()), worlds.handle.random, 0)
        val item = Item.getItemOf(nmsBlock)
        val quantity = nmsBlock.getDropCount(0, worlds.handle.random)
        val nmsItemStack = net.minecraft.server.v1_8_R3.ItemStack(item, quantity, nmsBlock.getDropData(nmsBlock.fromLegacyData(block.data.toInt())))


        return  CraftItemStack.asBukkitCopy(nmsItemStack).apply {
            if (nmsItemStack.count < 1) {
                this.amount = 1
            } else {
                this.amount = nmsItemStack.count
            }
        }
    }


}
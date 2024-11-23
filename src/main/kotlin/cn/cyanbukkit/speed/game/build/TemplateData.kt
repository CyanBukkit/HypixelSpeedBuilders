package cn.cyanbukkit.speed.game.build

import net.minecraft.server.v1_8_R3.Item
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method


data class Dimensions(
    val chang: Int,
    val kuan: Int,
    val gao: Int
)

data class TemplateBlockData(
    val x: Int,
    val y: Int,
    val z: Int,
    val type: String,
    val data: String
) {
    /**
     * 获取angle的值进行旋转
     * 比如 原来这个方块在 middle block的左边
     * angle 为 90
     * 就是顺时针旋转九十度
     * 现在那个方块在 middle block 上边 并且方块data也会跟着更改比如箱子的朝向
     * 床的朝向
     * 他是根据原始在middle block增加的位置的
     * 要支持负数负数就是逆时针旋转
     * @see TemplateData.showTemplate
     * @param angle 旋转角度
     */
    fun rotate(angle: Int): TemplateBlockData {
        // 标准化角度到 0-360 范围
        val normalizedAngle = (angle % 360 + 360) % 360
        // 计算旋转次数(每次旋转90度)
        val rotations = normalizedAngle / 90
        var newX = x
        var newZ = z
        var newData = data
        // 执行旋转
        repeat(rotations) {
            // 坐标旋转 (x,z) -> (-z,x)
            val tempX = newX
            newX = -newZ
            newZ = tempX
            // 根据方块类型处理数据旋转
            when {
                // 处理箱子朝向 (0-5)
                type == "CHEST" || type == "TRAPPED_CHEST" -> {
                    // 箱子朝向: 北(2) -> 东(5) -> 南(3) -> 西(4)
                    val rotatedDirection = when (val direction = newData.split(":")[0].toInt()) {
                        2 -> 5  // 北 -> 东
                        5 -> 3  // 东 -> 南
                        3 -> 4  // 南 -> 西
                        4 -> 2  // 西 -> 北
                        else -> direction
                    }
                    newData = "$rotatedDirection:${newData.split(":").drop(1).joinToString(":")}"
                }
                // 处理床的朝向 (0-3)
                type == "BED_BLOCK" -> {
                    val direction = newData.split(":")[0].toInt() and 0x3
                    val isHead = newData.split(":")[0].toInt() and 0x8
                    val rotatedDirection = (direction + 1) % 4
                    newData = "${rotatedDirection or isHead}:${newData.split(":").drop(1).joinToString(":")}"
                }
                // 处理告示牌、墙上的告示牌等方块 (0-15)
                type == "SIGN_POST" || type == "WALL_SIGN" -> {
                    val direction = newData.split(":")[0].toInt()
                    val rotatedDirection = (direction + 4) % 16
                    newData = "$rotatedDirection:${newData.split(":").drop(1).joinToString(":")}"
                }
                // 处理楼梯朝向 (0-7)
                type.endsWith("_STAIRS") -> {
                    val direction = newData.split(":")[0].toInt()
                    val isUpsideDown = direction and 0x4
                    var facing = direction and 0x3
                    facing = (facing + 1) % 4
                    newData = "${facing or isUpsideDown}:${newData.split(":").drop(1).joinToString(":")}"
                }
                // 处理门的朝向 (0-3)
                type.endsWith("_DOOR") -> {
                    val direction = newData.split(":")[0].toInt() and 0x3
                    val otherBits = newData.split(":")[0].toInt() and 0xC
                    val rotatedDirection = (direction + 1) % 4
                    newData = "${rotatedDirection or otherBits}:${newData.split(":").drop(1).joinToString(":")}"
                }

                // 处理旗帜朝向
                type == "STANDING_BANNER" -> {
                    val direction = newData.split(":")[0].toInt()
                    val rotatedDirection = (direction + 4) % 16
                    newData = "$rotatedDirection:${newData.split(":").drop(1).joinToString(":")}"
                }

                // 处理墙上的旗帜
                type == "WALL_BANNER" -> {
                    val direction = newData.split(":")[0].toInt()
                    val rotatedDirection = (direction + 1) % 4
                    newData = "$rotatedDirection:${newData.split(":").drop(1).joinToString(":")}"
                }
                // 处理头颅朝向
                type == "SKULL" -> {
                    val skullData = newData.split(":")
                    if (skullData.size >= 3) {
                        val skullType = skullData[0]
                        val rotation = skullData[1]
                        val facing = when (skullData[2]) {
                            "NORTH" -> "EAST"
                            "EAST" -> "SOUTH"
                            "SOUTH" -> "WEST"
                            "WEST" -> "NORTH"
                            else -> skullData[2]
                        }
                        newData = "$skullType:$rotation:$facing"
                    }
                }
                else -> {
                    // 其他方块不需要处理

                }
            }
        }
        return TemplateBlockData(newX, y, newZ, type, newData)
    }
}

/**
 * 模板的大小与名字
 */
data class TemplateData(
    val name: String,
    val size: Dimensions
)



/**
 * 根据Block获取物品
 */
fun Block.toItemStack(): ItemStack {
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


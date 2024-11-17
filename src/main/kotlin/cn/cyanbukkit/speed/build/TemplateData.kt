package cn.cyanbukkit.speed.build

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.utils.CompleteBlock.toItemStack
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

val templateList = mutableMapOf<TemplateData, List<TemplateBlockData>>() // 记录所有模板

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
            }
        }
        return TemplateBlockData(newX, y, newZ, type, newData)
    }
}

/**
 * 保存了玩家该建造方向的模板和对应的物品
 */
data class PlayerNeedBuildTemplate(
    val item: List<ItemStack>,
    val template: List<TemplateBlockData>
)

/**
 * 模板的大小与名字
 */
data class TemplateData(
    val name: String,
    val size: Dimensions
) {
    fun showTemplate(middle: Block, islandOrientation: IslandFace): PlayerNeedBuildTemplate {
        val t = templateList[this]!!
        val buildItemStack = mutableListOf<ItemStack>()
        val newTemplate = mutableListOf<TemplateBlockData>()

        // 首先将所有方块按照旋转后的状态添加到newTemplate中
        t.forEach { templateBlock ->
            val rotatedBlock = when (islandOrientation) {
                IslandFace.NORTH -> templateBlock
                IslandFace.EAST -> templateBlock.rotate(90)
                IslandFace.SOUTH -> templateBlock.rotate(180)
                IslandFace.WEST -> templateBlock.rotate(270)
            }
            newTemplate.add(rotatedBlock)
        }

        // 按Y坐标排序，实现从下往上建造
        val sortedTemplate = newTemplate.sortedBy { it.y }
        // 获取所有唯一的Y坐标值
        val uniqueYLevels = sortedTemplate.map { it.y }.distinct().sorted()
        // 使用Bukkit调度器来延迟处理每一层
        var layerIndex = 0
        (object : BukkitRunnable() {
            override fun run() {
                if (layerIndex >= uniqueYLevels.size) {
                    // 如果所有层都已处理完，取消任务
                    cancel()
                    return
                }
                // 获取当前层的Y坐标
                val currentY = uniqueYLevels[layerIndex]
                // 获取并处理当前层的所有方块
                sortedTemplate.filter { it.y == currentY }.forEach { bd ->
                    try {
                        val block = middle.getRelative(bd.x, bd.y, bd.z)
                        val da = bd.data.split(":")
                        val blockState = block.state

                        when (bd.type) {
                            "STANDING_BANNER", "WALL_BANNER" -> {
                                if (blockState is Banner) {
                                    blockState.type = Material.getMaterial(bd.type)
                                    blockState.rawData = da[0].toByte()
                                    when (da[1].toInt()) {
                                        0 -> blockState.baseColor = org.bukkit.DyeColor.WHITE
                                        1 -> blockState.baseColor = org.bukkit.DyeColor.ORANGE
                                        2 -> blockState.baseColor = org.bukkit.DyeColor.MAGENTA
                                        3 -> blockState.baseColor = org.bukkit.DyeColor.LIGHT_BLUE
                                        4 -> blockState.baseColor = org.bukkit.DyeColor.YELLOW
                                        5 -> blockState.baseColor = org.bukkit.DyeColor.LIME
                                        6 -> blockState.baseColor = org.bukkit.DyeColor.PINK
                                        7 -> blockState.baseColor = org.bukkit.DyeColor.GRAY
                                        9 -> blockState.baseColor = org.bukkit.DyeColor.CYAN
                                        10 -> blockState.baseColor = org.bukkit.DyeColor.PURPLE
                                        11 -> blockState.baseColor = org.bukkit.DyeColor.BLUE
                                        12 -> blockState.baseColor = org.bukkit.DyeColor.BROWN
                                        13 -> blockState.baseColor = org.bukkit.DyeColor.GREEN
                                        14 -> blockState.baseColor = org.bukkit.DyeColor.RED
                                        15 -> blockState.baseColor = org.bukkit.DyeColor.BLACK
                                    }
                                }
                            }
                            "BED_BLOCK" -> {
                                blockState.type = Material.getMaterial(bd.type)
                                blockState.rawData = da[0].toByte()
                            }
                            "SKULL" -> {
                                // 先设置方块类型，确保它是头颅方块
                                block.type = Material.SKULL
                                // 重新获取更新后的 BlockState
                                val updatedState = block.state
                                if (updatedState is org.bukkit.block.Skull) {
                                    updatedState.rawData = da[1].toByte()
                                    when (da[0].toInt()) {
                                        0 -> updatedState.skullType = org.bukkit.SkullType.SKELETON
                                        1 -> updatedState.skullType = org.bukkit.SkullType.WITHER
                                        2 -> updatedState.skullType = org.bukkit.SkullType.ZOMBIE
                                        3 -> updatedState.skullType = org.bukkit.SkullType.PLAYER
                                        4 -> updatedState.skullType = org.bukkit.SkullType.CREEPER
                                    }
                                    updatedState.rotation = org.bukkit.block.BlockFace.valueOf(da[2])
                                    updatedState.update(true)
                                }
                            }
                            else -> {
                                blockState.type = Material.getMaterial(bd.type)
                                blockState.rawData = da[0].toByte()
                            }
                        }
                        blockState.update(true)
                        buildItemStack.add(block.toItemStack())
                    } catch (e: Exception) {
                        // 记录错误但继续执行
                        SpeedBuildReloaded.instance.logger.warning("Error processing block at x:${bd.x} y:${bd.y} z:${bd.z} type:${bd.type}")
                        SpeedBuildReloaded.instance.logger.warning(e.toString())
                    }
                }
                layerIndex++
            }
        }).runTaskTimer(SpeedBuildReloaded.instance, 0L, 20L)
        return PlayerNeedBuildTemplate(buildItemStack, newTemplate)
    }
}

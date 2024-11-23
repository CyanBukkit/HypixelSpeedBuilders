package cn.cyanbukkit.speed.data

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.build.Template
import cn.cyanbukkit.speed.build.TemplateBlockData
import cn.cyanbukkit.speed.build.TemplateData
import cn.cyanbukkit.speed.task.GameVMData.needBuild
import cn.cyanbukkit.speed.task.GameVMData.templateList
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.entity.FallingBlock
import org.bukkit.scheduler.BukkitRunnable


data class Pos12(
    val pos1: Block?,
    val pos2: Block?
)


data class Region(
    val pos1: Location,
    val pos2: Location
) {
    fun isInIsland(to: Location): Boolean {
        // 如果这个to 在 pos1 与pos2 就为true
        val minX = minOf(pos1.x, pos2.x)
        val maxX = maxOf(pos1.x, pos2.x)
        val minY = minOf(pos1.y, pos2.y)
        val maxY = maxOf(pos1.y, pos2.y)
        val minZ = minOf(pos1.z, pos2.z)
        val maxZ = maxOf(pos1.z, pos2.z)
        return to.x in minX..maxX && to.y in minY..maxY && to.z in minZ..maxZ
    }


    /**
     * 所有方块转成FallBlock
     */
    fun boom(middle: Block): List<FallingBlock> {
        val list = mutableListOf<FallingBlock>()
        val coordinates = Template.getAllCoordinates(pos1.block, pos2.block)

        coordinates.forEach {
            if (it.type != Material.AIR) {
                val asE = middle.world.spawnFallingBlock(
                    it.location.add(0.0, 1.0, 0.0),
                    it.type,
                    it.state.rawData
                ) as FallingBlock
                asE.dropItem = false
                it.type = Material.AIR
                list.add(asE)
            }
        }
        list.forEach {
            it.velocity = it.location.toVector().subtract(middle.location.toVector()).normalize().multiply(1.5)
            middle.world.playSound(it.location, Sound.EXPLODE, 2f, 0f)
        }
        return list
    }

    fun inBuild(block: Block): Boolean {
        // 如果这个to 在 pos1 与pos2 就为true
        val minX = minOf(pos1.x, pos2.x)
        val maxX = maxOf(pos1.x, pos2.x)
        val minY = minOf(pos1.y, pos2.y)
        val maxY = maxOf(pos1.y, pos2.y)
        val minZ = minOf(pos1.z, pos2.z)
        val maxZ = maxOf(pos1.z, pos2.z)
        return block.x.toDouble() in minX..maxX && block.y.toDouble() in minY..maxY && block.z.toDouble() in minZ..maxZ
    }
}

fun Block.putBlock(tpb: TemplateBlockData) {
    val da = tpb.data.split(":")
    when (tpb.type) {
        "STANDING_BANNER", "WALL_BANNER" -> {
            if (this is Banner) {
                state.type = Material.getMaterial(tpb.type)
                state.rawData = da[0].toByte()
                when (da[1].toInt()) {
                    0 -> baseColor = org.bukkit.DyeColor.WHITE
                    1 -> baseColor = org.bukkit.DyeColor.ORANGE
                    2 -> baseColor = org.bukkit.DyeColor.MAGENTA
                    3 -> baseColor = org.bukkit.DyeColor.LIGHT_BLUE
                    4 -> baseColor = org.bukkit.DyeColor.YELLOW
                    5 -> baseColor = org.bukkit.DyeColor.LIME
                    6 -> baseColor = org.bukkit.DyeColor.PINK
                    7 -> baseColor = org.bukkit.DyeColor.GRAY
                    9 -> baseColor = org.bukkit.DyeColor.CYAN
                    10 -> baseColor = org.bukkit.DyeColor.PURPLE
                    11 -> baseColor = org.bukkit.DyeColor.BLUE
                    12 -> baseColor = org.bukkit.DyeColor.BROWN
                    13 -> baseColor = org.bukkit.DyeColor.GREEN
                    14 -> baseColor = org.bukkit.DyeColor.RED
                    15 -> baseColor = org.bukkit.DyeColor.BLACK
                }
            }
        }

        "BED_BLOCK" -> {
            type = Material.getMaterial(tpb.type)
            state.rawData = da[0].toByte()
        }

        "SKULL" -> {
            // 先设置方块类型，确保它是头颅方块
            state.type = Material.SKULL
            // 重新获取更新后的 BlockState
            if ( state is Skull) {
                state.rawData = da[1].toByte()
                when (da[0].toInt()) {
                    0 -> (state as Skull).skullType = org.bukkit.SkullType.SKELETON
                    1 -> (state as Skull).skullType = org.bukkit.SkullType.WITHER
                    2 -> (state as Skull).skullType = org.bukkit.SkullType.ZOMBIE
                    3 -> (state as Skull).skullType = org.bukkit.SkullType.PLAYER
                    4 -> (state as Skull).skullType = org.bukkit.SkullType.CREEPER
                }
                (state as Skull).rotation = org.bukkit.block.BlockFace.valueOf(da[2])
                (state as Skull).update(true)
            }
        }
        else -> {
            type = Material.getMaterial(tpb.type)
            state.rawData = da[0].toByte()
        }
    }
    state.update(true)
}

/**
 * 想Hyp那样像打字机一样打印出来
 */
fun  showTemplate(island: List<ArenaIslandData>, templateData: TemplateData)  {
    val t = templateList[templateData]!! //获取默认建筑
    var nowYBuild = 1
    val templateList = mutableMapOf<ArenaIslandData, MutableList<TemplateBlockData>>()
    object : BukkitRunnable() {
        override fun run() {
            if (nowYBuild > templateData.size.gao) {
                needBuild.clear()
                needBuild.putAll(templateList)
                cancel()
                return
            }
            // 筛选要建造出来的模板所对应的y坐标的所有东西
            val template = t.filter { it.y == nowYBuild }
            island.forEach { il ->
                template.forEach { bl ->
                    val rotatedBlock = when (il.face) {
                        IslandFace.NORTH -> bl
                        IslandFace.EAST -> bl.rotate(90)
                        IslandFace.SOUTH -> bl.rotate(180)
                        IslandFace.WEST -> bl.rotate(270)
                    }
                    val block = il.middleBlock.toBlock().getRelative(rotatedBlock.x, rotatedBlock.y, rotatedBlock.z)
                    block.putBlock(rotatedBlock)
                    block.world.playEffect(block.location, Effect.STEP_SOUND, block.type)
                    println(block)
                    // 如果 templateList 里面没有这个岛屿的话就添加进去
                    if (templateList.containsKey(il)) {
                        templateList[il]!!.add(rotatedBlock)
                    } else {
                        templateList[il] = mutableListOf(rotatedBlock)
                    }
                }
            }
            nowYBuild++
        }
    }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 20L)
}


fun cleanShowTemplate(list: List<ArenaIslandData>) {
    val world = list.first().buildRegions.pos1.world
    object : BukkitRunnable() {
        var y = 0
        override fun run() {
            list.forEach {
                val reg = it.buildRegions
                val minX = reg.pos1.blockX.coerceAtMost(reg.pos2.blockX)
                var minY = reg.pos1.blockY.coerceAtMost(reg.pos2.blockY) // 往上曾
                val minZ = reg.pos1.blockZ.coerceAtMost(reg.pos2.blockZ)
                val maxX = reg.pos1.blockX.coerceAtLeast(reg.pos2.blockX)
                val maxY = reg.pos1.blockY.coerceAtLeast(reg.pos2.blockY)
                val maxZ = reg.pos1.blockZ.coerceAtLeast(reg.pos2.blockZ)
                for (x in minX..maxX) {
                    for (z in minZ..maxZ) {
                        minY += y
                        if (minY > maxY) {
                            cancel()
                            return
                        }
                        world.playEffect(
                            world.getBlockAt(x, minY, z).location,
                            Effect.STEP_SOUND,
                            world.getBlockAt(x, minY, z).type
                        )
                        world.getBlockAt(x, minY, z).type = Material.AIR
                    }
                }
            }
            y++
        }
    }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 20L)
}


package cn.cyanbukkit.speed.data

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.build.Template
import org.bukkit.*
import org.bukkit.block.Block
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
                val asE = middle.world.spawnFallingBlock(it.location.add(0.0, 1.0, 0.0), it.type, it.state.rawData) as FallingBlock
                asE.dropItem = false
                it.type = Material.AIR
                list.add(asE)
            }
        }
//        val fl = this.getRegionDiameter().toFloat() / 2
//        middle.world.createExplosion(middle.location, fl)
        list.forEach {
            it.velocity = it.location.toVector().subtract(middle.location.toVector()).normalize().multiply(1.5)
            middle.world.playSound(it.location, Sound.EXPLODE, 2f, 0f)
        }
        return list
    }

    fun clean() {
        // 清除区域 根据与pos1 pos2的距离
        val world = Bukkit.getWorld(pos1.world.name)
        val distance = pos1.distance(pos2)
        val minX = pos1.blockX.coerceAtMost(pos2.blockX)
        val minY = pos1.blockY.coerceAtMost(pos2.blockY)
        val minZ = pos1.blockZ.coerceAtMost(pos2.blockZ)
        val maxX = pos1.blockX.coerceAtLeast(pos2.blockX)
        val maxY = pos1.blockY.coerceAtLeast(pos2.blockY)
        val maxZ = pos1.blockZ.coerceAtLeast(pos2.blockZ)
        // 创建BukkitRunnable来异步执行删除操作
        object : BukkitRunnable() {
            var currentY = maxY
            override fun run() {
                if (currentY < minY) {
                    // 当前Y值超过最大Y值，任务完成
                    cancel()
                    return
                }
                for (x in minX..maxX) {
                    for (z in minZ..maxZ) {
                        val location = Location(world, x.toDouble(), currentY.toDouble(), z.toDouble())
                        if (pos1.distance(location) <= distance) { // 删除方块并播放音效
                            world.playEffect(location, Effect.STEP_SOUND, world.getBlockAt(location).type)
                            world.getBlockAt(location).type = Material.AIR
                        }
                    }
                }
                // 处理下一层
                currentY--
            }
        }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 20L) // 0L为首次执行的延迟，1L为每次执行的间隔（单位：tick）

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
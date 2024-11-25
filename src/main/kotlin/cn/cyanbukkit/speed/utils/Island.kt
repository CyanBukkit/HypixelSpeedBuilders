package cn.cyanbukkit.speed.utils

import cn.cyanbukkit.speed.SpeedBuildReloaded
import com.sk89q.worldedit.CuboidClipboard
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitUtil
import com.sk89q.worldedit.schematic.SchematicFormat
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import java.io.File


/**‘
 * 从插件文件夹下找到所有的Schematic数据并且加载到缓存方便玩家设置
 */
/**
 * 从插件文件夹下找到所有的Schematic数据并且加载到缓存方便玩家设置
 */
fun loadIslandData() {
    val schematicDirectory = File(SpeedBuildReloaded.instance.dataFolder, "island")
    if (!schematicDirectory.exists()) {
        schematicDirectory.mkdirs()
        return
    }
    try {
        for (file in schematicDirectory.listFiles() ?: emptyArray()) {
            if (file.name.endsWith(".schematic")) {
                try {
                    // Check if the file is a valid schematic file
                    if (!file.canRead() || file.length() == 0L) {
                        SpeedBuildReloaded.instance.logger.warning("Invalid schematic file: ${file.name}")
                        continue
                    }
                    // WorldEdit 6.0 使用 SchematicFormat 来加载schematic文件
                    val schematic = SchematicFormat.MCEDIT.load(file)
                    islandTemplates[file.nameWithoutExtension] = schematic
                    SpeedBuildReloaded.instance.logger.info("Loaded schematic: ${file.name}")
                } catch (e: Exception) {
                    SpeedBuildReloaded.instance.logger.warning("Failed to load schematic ${file.name}: ${e.message}")
                }
            }
        }
    } catch (e: Exception) {
        SpeedBuildReloaded.instance.logger.severe("Error while loading schematics: ${e.message}")
        e.printStackTrace()
    }
}
/**
 * 将岛的数据加载到缓存
 */
val islandTemplates = mutableMapOf<String, CuboidClipboard>()

/**
 * 设置玩家的岛
 * @param block 是复制点所以放置时根据这点放置
 * @param name 岛名
 */
fun Player.useIsland(block: Block, name: String) {
    val world = this.location.world
    val clipboard = islandTemplates[name] ?: return // 如果没有找到对应的岛屿模板，则返回
    // WorldEdit 6.0 使用不同的方式创建EditSession
    val worldEditWorld = BukkitUtil.getLocalWorld(world)
    val editSession = WorldEdit.getInstance().editSessionFactory.getEditSession(worldEditWorld, -1)
    try {
        // 在 6.0 中，直接使用 clipboard 的 paste 方法
        clipboard.paste(editSession, Vector(block.x, block.y + 1, block.z), false) // false 表示不忽略空气块
        editSession.flushQueue() // 确保所有更改都被应用
    } catch (e: Exception) {
        e.printStackTrace()
    }
}




fun List<Block>.boom(middle: Block) {
    val block = mutableListOf<FallingBlock>()
    // 找出this的y最高的那一层的所有方块
    val maxY = this.maxOfOrNull { it.y } ?: return
    this.forEach {
        if (it.type != Material.AIR && it.y == maxY) {
            val fall = it.world.spawnFallingBlock(it.location.add(0.0, 1.0, 0.0), it.type, it.state.rawData)as FallingBlock
            fall.dropItem = false
            it.type = Material.AIR
            block.add(fall)
        } else {
            it.type = Material.AIR
        }
    }
    block.forEach {
        it.velocity = it.location.toVector().subtract(middle.location.toVector()).normalize().multiply(1.5)
        middle.world.playSound(it.location, Sound.EXPLODE, 2f, 0f)
        Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
            it.remove()
        }, 20 * 3)
    }
}
package cn.cyanbukkit.speed.build

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.Pos12
import cn.cyanbukkit.speed.data.Region
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.abs

object Template : Listener {

    val templatingBind = mutableMapOf<Player, String>()
    val templatingDate = mutableMapOf<Player, Pos12>()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onIntact(e: PlayerInteractEvent) {
        if (templatingBind.keys.contains(e.player) && e.hasBlock()) {
            // 手里的工具是不是 BLAZE_ROD
            if (e.player.inventory.itemInHand.type.name != "BLAZE_ROD") {
                e.player.sendMessage("§b[SpeedBuild]§6请手持模板工具")
                return
            } else {
                e.isCancelled = true
                // 左键 Pos1 右键 Pos2
                if (e.action.name == "LEFT_CLICK_BLOCK") {
                    // 保存第一个点
                    templatingDate[e.player] = if (templatingDate[e.player] == null) {
                        Pos12(e.clickedBlock, null)
                    } else {
                        Pos12(e.clickedBlock, templatingDate[e.player]?.pos2)
                    }
                    e.player.sendMessage("§b[SpeedBuild]§6已设置第一个点")
                } else if (e.action.name == "RIGHT_CLICK_BLOCK") {
                    // 保存第二个点
                    templatingDate[e.player] = if (templatingDate[e.player] == null) {
                        Pos12(null, e.clickedBlock)
                    } else {
                        Pos12(templatingDate[e.player]?.pos1, e.clickedBlock)
                    }
                    e.player.sendMessage("§b[SpeedBuild]§6已设置第二个点")
                }
            }
        }
    }



    /**
     * @note 生成平台
     */
    fun Player.buildPlatform() {
        // 玩家脚下放绿色羊毛
        val mid = this.location.add(0.0, -1.0, 0.0).block
        // 然后循环放
        //  -5 到 5 循环
        for (a in -5..5) {
            for (b in -5..5) {
                val block = mid.getRelative(a, 0, b)
                block.type = Material.STAINED_CLAY
                block.data = 0
            }
        }

        mid.type = Material.WOOL
        mid.data = 5
    }

    /**
     *  @note 根据两个点获取所有坐标
     */
    fun getAllCoordinates(pos1: Block, pos2: Block): List<Block> {
        val coordinates = mutableListOf<Block>()
        // 现编
        val minX = minOf(pos1.x, pos2.x)
        val maxX = maxOf(pos1.x, pos2.x)
        val minY = minOf(pos1.y, pos2.y)
        val maxY = maxOf(pos1.y, pos2.y)
        val minZ = minOf(pos1.z, pos2.z)
        val maxZ = maxOf(pos1.z, pos2.z)

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    coordinates.add(pos1.world.getBlockAt(x, y, z))
                }
            }
        }

        return coordinates
    }

    /**
     * @note 创建模板
     */
    fun createTemplate(middleBlock: Block, reg: Region, name: String) {
        val list = getAllCoordinates(reg.pos1.block, reg.pos2.block)
        for (block in list) {
            if (block.type == Material.AIR) {
                continue
            }
            // 计算与中心点的距离 并 Relative
            val xxx = block.x - middleBlock.x
            val yyy = block.y - middleBlock.y
            val zzz = block.z - middleBlock.z
            val coordinate = "$xxx,$yyy,$zzz"

            val start = block.state

            when {
                start.type.toString() == "STANDING_BANNER" || start.type.toString() == "WALL_BANNER" -> {
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.type", start.type.toString())
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.data",
                        (15 - (start as Banner).baseColor.ordinal).toString() + ":" + (start.getRawData().toInt()))
                }

                start.type.toString() == "BED_BLOCK" -> {
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.type", start.type.toString())
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.data",
                        "0:" + (start.rawData.toInt()))
                }

                start.type.toString() == "SKULL" -> {
                    val skull = block.state as Skull
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.type", skull.type.toString())
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.data",
                        skull.skullType.ordinal.toString() + ":" + (start.rawData.toInt()) + ":" + skull.rotation.toString())
                }

                else -> {
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.type", start.type.toString())
                    SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Blocks.$coordinate.data", start.rawData)
                }

            }

        }
        // 让reg.pos1.block.x - reg.pos2.block.x 是正整数
        val chang = abs(reg.pos1.block.x - reg.pos2.block.x) + 1
        val kuan = abs(reg.pos1.block.z - reg.pos2.block.z) + 1
        val gao = abs(reg.pos1.block.y - reg.pos2.block.y) + 1
        SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Dimensions.chang", chang)
        SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Dimensions.kuan", kuan )
        SpeedBuildReloaded.instance.blockTemplate.set("Lists.$name.Dimensions.gao", gao)
        SpeedBuildReloaded.instance.blockTemplate.save(SpeedBuildReloaded.instance.blockTemplateFile)
    }

    /**
     * @note 列出模板列表
     */
    fun Player.templateList() {
        val list = SpeedBuildReloaded.instance.blockTemplate.getConfigurationSection("Lists")
        list.getKeys(false).forEach {
            this.sendMessage("§b[SpeedBuild]§6模板 - $it")
        }
    }



    /**
     * @note 重现建筑敕令上的
     */
    fun Player.returnTemplate(name: String) {
        val middle = this.location.add(0.0, -1.0, 0.0).block
        val list = SpeedBuildReloaded.instance.blockTemplate.getConfigurationSection("Lists.$name.Blocks")
        list.getKeys(false).forEach {
            val pos = it.split(",")
            val x = pos[0].toInt()
            val y = pos[1].toInt()
            val z = pos[2].toInt()
            val block = middle.getRelative(x, y, z)
            val da = list.getString("$it.data").split(":")
            val blockState = block.state
            when (list.getString("$it.type")) {
                "STANDING_BANNER", "WALL_BANNER" -> {
                    blockState.type = Material.getMaterial(list.getString("$it.type"))
                    blockState.rawData = da[0].toByte()
                    val banner: Banner = blockState as Banner
                    when (da[1].toInt()) {
                        0 -> banner.baseColor = org.bukkit.DyeColor.WHITE
                        1 -> banner.baseColor = org.bukkit.DyeColor.ORANGE
                        2 -> banner.baseColor = org.bukkit.DyeColor.MAGENTA
                        3 -> banner.baseColor = org.bukkit.DyeColor.LIGHT_BLUE
                        4 -> banner.baseColor = org.bukkit.DyeColor.YELLOW
                        5 -> banner.baseColor = org.bukkit.DyeColor.LIME
                        6 -> banner.baseColor = org.bukkit.DyeColor.PINK
                        7 -> banner.baseColor = org.bukkit.DyeColor.GRAY
                        9 -> banner.baseColor = org.bukkit.DyeColor.CYAN
                        10 -> banner.baseColor = org.bukkit.DyeColor.PURPLE
                        11 -> banner.baseColor = org.bukkit.DyeColor.BLUE
                        12 -> banner.baseColor = org.bukkit.DyeColor.BROWN
                        13 -> banner.baseColor = org.bukkit.DyeColor.GREEN
                        14 -> banner.baseColor = org.bukkit.DyeColor.RED
                        15 -> banner.baseColor = org.bukkit.DyeColor.BLACK
                    }
                }
                "SKULL" -> {
                    blockState.type = Material.getMaterial(list.getString("$it.type"))
                    blockState.rawData = da[1].toByte()
                    val skull = blockState as org.bukkit.block.Skull
                    when (da[0].toInt()) {
                        0 -> skull.skullType = org.bukkit.SkullType.SKELETON
                        1 -> skull.skullType = org.bukkit.SkullType.WITHER
                        2 -> skull.skullType = org.bukkit.SkullType.ZOMBIE
                        3 -> skull.skullType = org.bukkit.SkullType.PLAYER
                        4 -> skull.skullType = org.bukkit.SkullType.CREEPER
                    }
                    skull.rotation = org.bukkit.block.BlockFace.valueOf(da[2])
                }
                else -> {
                    blockState.type = Material.getMaterial(list.getString("$it.type"))
                    blockState.rawData = da[0].toByte()
                }
            }
            blockState.update(true)
        }

    }


}
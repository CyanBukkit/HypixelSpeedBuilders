package cn.cyanbukkit.speed.data

import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.utils.CompleteBlock.toItemStack
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack



data class TemplateBlockData(
    val x: Int,
    val y: Int,
    val z: Int,
    val type: String,
    val data: String
)


data class TemplateData(
    val name: String,
    val size: Dimensions
) {
    fun showTemplate(middle: Block): List<ItemStack> {
        val t = LoaderData.templateList[this]!!
        val buildItemStack = mutableListOf<ItemStack>()
        t.forEach {
            val block = middle.getRelative(it.x, it.y, it.z)
            val da = it.data.split(":")
            val blockState = block.state
            when (it.type) {
                "STANDING_BANNER", "WALL_BANNER" -> {
                    blockState.type = Material.getMaterial(it.type)
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
                "BED_BLOCK" -> {
                    blockState.type = Material.getMaterial(it.type)
                    blockState.rawData = da[0].toByte()
                }
                "SKULL" -> {
                    blockState.type = Material.getMaterial(it.type)
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
                    blockState.type = Material.getMaterial(it.type)
                    blockState.rawData = da[0].toByte()
                }
            }
            blockState.update(true)
            buildItemStack.add(block.toItemStack())
        }
        return buildItemStack
    }


    private fun getBlockPlaceSound(material: Material): Sound {
        return when (material) {
            Material.STONE -> Sound.STEP_STONE
            Material.WOOD -> Sound.STEP_WOOD
            Material.LOG -> Sound.STEP_WOOD
            Material.LOG_2 -> Sound.STEP_WOOD
            Material.GRASS -> Sound.STEP_GRASS
            Material.SAND -> Sound.STEP_SAND
            Material.GRAVEL -> Sound.STEP_GRAVEL
            Material.GOLD_BLOCK -> Sound.STEP_STONE
            Material.IRON_BLOCK -> Sound.STEP_STONE
            Material.BRICK -> Sound.STEP_STONE
            Material.MOSSY_COBBLESTONE -> Sound.STEP_STONE
            Material.OBSIDIAN -> Sound.STEP_STONE
            Material.DIAMOND_BLOCK -> Sound.STEP_STONE
            Material.SNOW_BLOCK -> Sound.STEP_SNOW
            Material.CLAY -> Sound.STEP_GRAVEL
            Material.SOUL_SAND -> Sound.STEP_SAND
            Material.NETHERRACK -> Sound.STEP_STONE
            Material.ENDER_STONE -> Sound.STEP_STONE
            Material.SANDSTONE -> Sound.STEP_STONE
            Material.EMERALD_BLOCK -> Sound.STEP_STONE
            Material.SPRUCE_WOOD_STAIRS -> Sound.STEP_WOOD
            Material.BIRCH_WOOD_STAIRS -> Sound.STEP_WOOD
            Material.JUNGLE_WOOD_STAIRS -> Sound.STEP_WOOD
            Material.COBBLESTONE_STAIRS -> Sound.STEP_STONE
            Material.BRICK_STAIRS -> Sound.STEP_STONE
            Material.SMOOTH_STAIRS -> Sound.STEP_STONE
            Material.NETHER_BRICK_STAIRS -> Sound.STEP_STONE
            Material.QUARTZ_STAIRS -> Sound.STEP_STONE
            Material.ACACIA_STAIRS -> Sound.STEP_WOOD
            Material.DARK_OAK_STAIRS -> Sound.STEP_WOOD
            Material.SLIME_BLOCK -> Sound.STEP_STONE
            Material.PRISMARINE -> Sound.STEP_STONE
            Material.SEA_LANTERN -> Sound.STEP_STONE
            Material.RED_SANDSTONE -> Sound.STEP_STONE
            Material.RED_SANDSTONE_STAIRS -> Sound.STEP_STONE

            Material.WOOD_STEP -> Sound.STEP_WOOD
            Material.STEP -> Sound.STEP_STONE
            Material.SNOW -> Sound.STEP_SNOW
            Material.CACTUS -> Sound.STEP_STONE
            Material.JUKEBOX -> Sound.STEP_STONE
            Material.FENCE -> Sound.STEP_WOOD
            Material.FENCE_GATE -> Sound.STEP_WOOD
            Material.NETHER_FENCE -> Sound.STEP_WOOD
            Material.SPRUCE_FENCE_GATE -> Sound.STEP_WOOD
            Material.BIRCH_FENCE_GATE -> Sound.STEP_WOOD
            Material.JUNGLE_FENCE_GATE -> Sound.STEP_WOOD
            Material.DARK_OAK_FENCE_GATE -> Sound.STEP_WOOD
            Material.ACACIA_FENCE_GATE -> Sound.STEP_WOOD
            Material.SPRUCE_FENCE -> Sound.STEP_WOOD
            Material.BIRCH_FENCE -> Sound.STEP_WOOD
            Material.JUNGLE_FENCE -> Sound.STEP_WOOD
            Material.DARK_OAK_FENCE -> Sound.STEP_WOOD
            Material.ACACIA_FENCE -> Sound.STEP_WOOD

            Material.WOOD_DOUBLE_STEP -> Sound.STEP_WOOD
            Material.DOUBLE_STEP -> Sound.STEP_STONE
            Material.STONE_SLAB2 -> Sound.STEP_STONE

            Material.WOOD_BUTTON -> Sound.STEP_WOOD

            Material.WOOD_PLATE -> Sound.STEP_WOOD

            Material.STONE_PLATE -> Sound.STEP_STONE


            Material.WOOD_DOOR -> Sound.STEP_WOOD

            Material.WOODEN_DOOR -> Sound.STEP_WOOD
            Material.IRON_DOOR -> Sound.STEP_STONE

            Material.IRON_DOOR_BLOCK -> Sound.STEP_STONE

            Material.TRAP_DOOR -> Sound.STEP_WOOD


            Material.IRON_TRAPDOOR -> Sound.STEP_STONE


            // Add more mappings as needed
            else -> Sound.STEP_STONE // Default sound
        }
    }
}

package cn.cyanbukkit.speed.game.build

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.ArenaIslandData
import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.IslandFace
import cn.cyanbukkit.speed.game.GameVMData
import cn.cyanbukkit.speed.game.GameVMData.needBuild
import cn.cyanbukkit.speed.game.GameVMData.playerBindIsLand
import cn.cyanbukkit.speed.game.GameVMData.templateList
import org.bukkit.*
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.lang.reflect.Method

object Template : Listener {

    val settingTemplate = mutableMapOf<Player, Block>()

    /**
     * @note 生成平台
     */
    fun Player.buildPlatform() {
        // 玩家脚下放绿色羊毛
        val mid = this.location.add(0.0, -1.0, 0.0).block
        settingTemplate[this] = mid
        // 然后循环放
        //  -5 到 5 循环
        for (a in -3..3) {
            for (b in -3..3) {
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
     * @note 列出模板列表
     */
    fun Player.templateList() {
        this.sendMessage("§b[SpeedBuild]§6模板列表:")
        // 读取模板列表
        GameVMData.templateList.keys.forEach {
            this.sendMessage("  =>  $it")
        }
    }


    /**
     * @note 保存模板
     */
    fun Player.createTemplate(name: String) {
        val middleBlock = settingTemplate[this]!!
        for (y in 1..7) { // 从上面的方块开始到 第七个方块
            for (x in -3..3) {
                for (z in -3..3) {
                    val coordinate = "$x,$y,$z"
                    val block = middleBlock.getRelative(x, y, z)
                    if (block.type == Material.AIR) {
                        continue
                    }
                    when (block.type) {
                        Material.STANDING_BANNER, Material.WALL_BANNER -> {
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.type",
                                block.type.toString()
                            )
                            val banner = block.state as org.bukkit.block.Banner
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.data",
                                "${15 - banner.baseColor.ordinal}:${banner.rawData.toInt()}"
                            )
                        }

                        Material.BED_BLOCK -> {
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.type",
                                block.type.toString()
                            )
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.data",
                                block.data.toInt()
                            )
                        }

                        Material.SKULL -> {
                            val skull = block.state as Skull
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.type",
                                skull.type.toString()
                            )
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.data",
                                skull.skullType.ordinal.toString() + ":" + (skull.rawData.toInt()) + ":" + skull.rotation.toString() + ":" + skull.owner
                            )
                        }

                        else -> {
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.type",
                                block.type.toString()
                            )
                            SpeedBuildReloaded.instance.blockTemplate.set(
                                "Lists.$name.Blocks.$coordinate.data",
                                block.state.rawData
                            )
                        }
                    }
                }
            }
        }
        SpeedBuildReloaded.instance.blockTemplate.save(SpeedBuildReloaded.instance.blockTemplateFile)
        sendMessage("§b[SpeedBuild]§6已保存模板 $name")
        settingTemplate.remove(this)
    }


    fun clearItem(arena: ArenaSettingData) {
        // 清理地上凋落物
        val world = Bukkit.getWorld(arena.worldName)
        world!!.entities.forEach {
            if (it is Item) {
                it.remove()
            }
        }
    }


    fun Block.putBlock(tpb: TemplateBlockData) {
        val da = tpb.data.split(":")
        when (tpb.type) {
            "STANDING_BANNER", "WALL_BANNER" -> {
                type = Material.getMaterial(tpb.type)
                if (this@putBlock is Banner) {
                    state.rawData = da[0].toByte()
                    when (da[1].toInt()) {
                        0 -> baseColor = DyeColor.WHITE
                        1 -> baseColor = DyeColor.ORANGE
                        2 -> baseColor = DyeColor.MAGENTA
                        3 -> baseColor = DyeColor.LIGHT_BLUE
                        4 -> baseColor = DyeColor.YELLOW
                        5 -> baseColor = DyeColor.LIME
                        6 -> baseColor = DyeColor.PINK
                        7 -> baseColor = DyeColor.GRAY
                        9 -> baseColor = DyeColor.CYAN
                        10 -> baseColor = DyeColor.PURPLE
                        11 -> baseColor = DyeColor.BLUE
                        12 -> baseColor = DyeColor.BROWN
                        13 -> baseColor = DyeColor.GREEN
                        14 -> baseColor = DyeColor.RED
                        15 -> baseColor = DyeColor.BLACK
                    }
                    state.update(true)
                    this.update(true)
                    println("放置旗帜 $this")
                }
            }


            "SKULL" -> {
                type = Material.getMaterial(tpb.type)
                // 先设置方块类型，确保它是头颅方块
                state.type = Material.SKULL
                // 重新获取更新后的 BlockState
                if (state is Skull) {
                    val skull = state as Skull
                    data = da[1].toByte()
                    when (da[0].toInt()) {
                        0 -> skull.skullType = SkullType.SKELETON
                        1 -> skull.skullType = SkullType.WITHER
                        2 -> skull.skullType = SkullType.ZOMBIE
                        3 -> skull.skullType = SkullType.PLAYER
                        4 -> skull.skullType = SkullType.CREEPER
                    }
                    skull.rotation = org.bukkit.block.BlockFace.valueOf(da[2])
                    try {
                        skull.owner = da[3]
                    } catch (_: Exception) {
                    }
                    skull.update(true)
                    state.update(true)
                    println("放置头颅 $this")
                }
            }

            else -> {
                type = Material.getMaterial(tpb.type)
                data = da[0].toByte()
                state.update(true)
                println("放置方块 $this")
            }
        }
        world.playEffect(location, Effect.STEP_SOUND, Material.getMaterial(tpb.type))
    }

    /**
     * 想Hyp那样像打字机一样打印出来
     */
    fun showTemplate(island: MutableList<ArenaIslandData>, templateData: String) {
        val t = templateList[templateData]!! //获取默认建筑
        var nowYBuild = 1
        val templateList = mutableMapOf<ArenaIslandData, MutableList<TemplateBlockData>>()
        object : BukkitRunnable() {
            override fun run() {
                if (nowYBuild > 7) {
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

                        Bukkit.getScheduler().runTask(SpeedBuildReloaded.instance, {
                            val block = il.middleBlock.getRelative(rotatedBlock.x, rotatedBlock.y, rotatedBlock.z)
                            block.putBlock(rotatedBlock)
                        })
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
        }.runTaskTimerAsynchronously(SpeedBuildReloaded.instance, 0L, 20L)
    }


    fun cleanShowTemplate(list: MutableList<ArenaIslandData>) {
        object : BukkitRunnable() {
            var y = 7
            override fun run() {
                list.forEach { ill ->
                    val block = ill.get(y)
                    block.forEach { b ->
                        val item = b.location.block.toItemStack()
                        // 获取岛内玩家
                        val players = playerBindIsLand.filter { it.value == ill }.keys
                        players.forEach { p -> // 但仅限于岛内的玩家 不然会别的岛的玩家也能拿到这个物品
                            p.inventory.addItem(item)
                        }
                        b.world.playEffect(
                            b.location,
                            Effect.STEP_SOUND,
                            b.type
                        )
                        b.type = Material.AIR
                    }
                }
                y--
                if (y < 0) {
                    cancel()
                }
            }
        }.runTaskTimer(SpeedBuildReloaded.instance, 0L, 20L)
    }


    fun fastCleanRegion(list: MutableList<ArenaIslandData>) {
        list.forEach {
            it.list.forEach { block ->
                block.world.playEffect(
                    block.location,
                    Effect.STEP_SOUND,
                    block.type
                )
                block.type = Material.AIR
            }
        }
    }


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
        val item = net.minecraft.server.v1_8_R3.Item.getItemOf(nmsBlock)
        val quantity = nmsBlock.getDropCount(0, worlds.handle.random)
        val nmsItemStack = net.minecraft.server.v1_8_R3.ItemStack(
            item,
            quantity,
            nmsBlock.getDropData(nmsBlock.fromLegacyData(block.data.toInt()))
        )
        return CraftItemStack.asBukkitCopy(nmsItemStack).apply {
            if (nmsItemStack.count < 1) {
                this.amount = 1
            } else {
                this.amount = nmsItemStack.count
            }
        }
    }


}
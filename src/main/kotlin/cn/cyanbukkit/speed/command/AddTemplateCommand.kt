package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.data.*
import cn.cyanbukkit.speed.game.GameRegionManager.buildRegionOrMakeTemplate
import cn.cyanbukkit.speed.game.GameVMData
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.build.Template.buildPlatform
import cn.cyanbukkit.speed.game.build.Template.createTemplate
import cn.cyanbukkit.speed.game.build.Template.templateList
import cn.cyanbukkit.speed.game.build.Template.templatingBind
import cn.cyanbukkit.speed.game.build.Template.templatingDate
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


/**
 *  直接控制配置文件 和保存
 */
class AddTemplateCommand : Command(
    "addtemplate", "SpeedBuildReloaded addtemplate", "/addtemplate", listOf("add")
) {


    init {
        permission = "speedbuildreloaded.admin.addtemplate"
    }


    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        if (!p0.hasPermission(permission)) {
            p0.sendMessage(configSettings!!.mess.noPermission)
            return true
        }
        // start <name>放置一个以玩家为中心的平台
        if (p0 !is Player) {
            p0.sendMessage("§b[SpeedBuild]§6该指令只能由玩家执行")
        }
        val p = p0 as Player
        if (p2.isEmpty()) {
            p.sendMessage("§b[SpeedBuild]§6/addtemplate start <name> 放置一个以玩家为中心的平台")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate create 站在平台中间方块保存模板")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate return <name> 返回模板")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate list 查看模板列表")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate setblock <x> <y> <z> 设置方块")
            return true
        }
        when (p2[0]) {

            "start" -> {
                // 给与工具
                when (p2.size) {
                    2 -> {
                        p.sendMessage("§b[SpeedBuild]§6没有为你建造默认平台")
                    }

                    3 -> {
                        p.buildPlatform()
                    }

                    else -> {
                        p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate start <name> <true = 默认建造平台>")
                        return true
                    }
                }

                templatingBind[p] = p2[1]
                p.inventory.addItem(ItemStack(Material.BLAZE_ROD).apply {
                    itemMeta = itemMeta.apply {
                        displayName = "§b[SpeedBuild]§6模板工具"
                    }
                    this.itemMeta = itemMeta
                })
                p.sendMessage("§b[SpeedBuild]§6请用模板工具点击两个点")
            }

            "create" -> {
                // 保存第二个点
                if (templatingDate.contains(p)) {
                    val pos12 = templatingDate[p]!!
                    val pos1 = if (pos12.pos1 == null) {
                        p.sendMessage("§b[SpeedBuild]§6请先用模板工具点击两个点")
                        return true
                    } else {
                        pos12.pos1
                    }
                    val pos2 = if (pos12.pos2 == null) {
                        p.sendMessage("§b[SpeedBuild]§6请先用模板工具点击两个点")
                        return true
                    } else {
                        pos12.pos2
                    }
                    val reg = p.buildRegionOrMakeTemplate(Region(pos1.location, pos2.location))
                    if (reg == null) {
                        p.sendMessage("§b[SpeedBuild]§6请先用模板工具点击两个点")
                        return true
                    } else {
                        createTemplate(p.location.add(0.0, -1.0, 0.0).block, reg, templatingBind[p]!!)
                        p.sendMessage("§b[SpeedBuild]§6已保存模板 ${templatingBind[p]}")
                    }
                    templatingBind.remove(p)
                    templatingDate.remove(p)
                } else {
                    p.sendMessage("§b[SpeedBuild]§6请先/addtemplate start <name> 然后用模板工具点击两个点")
                }
            }

            "return" -> {
                // 返回模板
                if (p2.size != 2) {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate return <name>")
                    return true
                }
                val middle = p.location.add(0.0, -1.0, 0.0).block
                showTemplate(
                    mutableListOf(
                    ArenaIslandData(
                        p.location,
                        middle.location,
                        Region(p.location, p.location),
                        Region(p.location, p.location),
                        IslandFace.NORTH
                    )
                ), GameVMData.templateList.keys.first { it.name == p2[1] })
                p.sendMessage("§b[SpeedBuild]§6已重现模板 ${p2[1]}")
            }

            "setblock" -> {
                // 设置方块
                if (p2.size != 6) {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate setblock METRIAL DATA")
                    return true
                }
                val loc = p.location.block.getRelative(p2[3].toInt(), p2[4].toInt(), p2[5].toInt())
                loc.putBlock(TemplateBlockData(0, 0, 0, p2[1], p2[2]))
                p.sendMessage("§b[SpeedBuild]§6已放置方块试试看！")
            }

            "look" -> {
                // 查看方块
                val loc = p.getTargetBlock(null as Set<Material>?, 10).location
                val block = loc.block
                p.sendMessage("§b[SpeedBuild]§6${block.type} ${block.data}")
            }

            "list" -> {
                // 返回模板
                p.templateList()
            }

        }
        return true
    }
}
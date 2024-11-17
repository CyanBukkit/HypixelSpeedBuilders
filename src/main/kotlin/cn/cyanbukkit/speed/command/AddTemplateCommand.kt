package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.build.Template.buildPlatform
import cn.cyanbukkit.speed.build.Template.createTemplate
import cn.cyanbukkit.speed.build.Template.returnTemplate
import cn.cyanbukkit.speed.build.Template.templateList
import cn.cyanbukkit.speed.build.Template.templatingBind
import cn.cyanbukkit.speed.build.Template.templatingDate
import cn.cyanbukkit.speed.data.Region
import cn.cyanbukkit.speed.game.GameRegionManager.buildRegionOrMakeTemplate
import cn.cyanbukkit.speed.game.LoaderData
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 *  直接控制配置文件 和保存
 */
class AddTemplateCommand : Command("addtemplate",
    "SpeedBuildReloaded addtemplate", "/addtemplate", listOf("add")) {


    init {
        permission = "speedbuildreloaded.admin.addtemplate"
    }


    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        if (!p0.hasPermission(permission)) {
            p0.sendMessage(LoaderData.configSettings!!.mess.noPermission)
            return true
        }
        // start <name>放置一个以玩家为中心的平台
        if (p0 !is Player ) {
            p0.sendMessage("§b[SpeedBuild]§6该指令只能由玩家执行")
        }
        val p = p0 as Player
        if (p2.isEmpty()) {
            p.sendMessage("§b[SpeedBuild]§6/addtemplate start <name> 放置一个以玩家为中心的平台")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate create 站在平台中间方块保存模板")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate return <name> 返回模板")
            p.sendMessage("§b[SpeedBuild]§6/addtemplate list 查看模板列表")
            return true
        }
        when (p2[0]) {
            "start" -> {
                // 给与工具
                if (p2.size == 2) {
                    p.sendMessage("§b[SpeedBuild]§6没有为你建造默认平台")
                } else if (p2.size == 3) {
                    p.buildPlatform()
                } else {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate start <name> <true = 默认建造平台>")
                    return true
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
                        return true
                    } else {
                        createTemplate(p.location.add(0.0,-1.0,0.0).block, reg, templatingBind[p]!!)
                        p.sendMessage("§b[SpeedBuild]§6已保存模板 ${templatingBind[p]}")
                    }
                    templatingBind.remove(p)
                    templatingDate.remove(p)
                } else {
                    p.sendMessage("§b[SpeedBuild]§6请先/addtemplate start <name> 然后用模板工具点击两个点")
                }
            }

            "return"  -> {
                // 返回模板
                if (p2.size != 2) {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate return <name>")
                    return true
                }
                p.returnTemplate(p2[1])
                p.sendMessage("§b[SpeedBuild]§6已重现模板 ${p2[1]}")
            }

            "list" -> {
                // 返回模板
                p.templateList()
            }

        }
        return true
    }
}
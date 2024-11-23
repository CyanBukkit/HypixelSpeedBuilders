package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.ArenaIslandData
import cn.cyanbukkit.speed.data.IslandFace
import cn.cyanbukkit.speed.game.GameVMData
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.LoaderData.loadTemplate
import cn.cyanbukkit.speed.game.build.Template.buildPlatform
import cn.cyanbukkit.speed.game.build.Template.createTemplate
import cn.cyanbukkit.speed.game.build.Template.settingTemplate
import cn.cyanbukkit.speed.game.build.Template.showTemplate
import cn.cyanbukkit.speed.game.build.Template.templateList
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


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
            p.sendMessage("""
                §6/addtemplate start        开始建造（会保存你脚下的方块作为中心方块）
                §6/addtemplate save <模板名> 保存模板
                §6/addtemplate return       重现模板
                §6/addtemplate list         查看模板列表
                §6/addtemplate look         查看方块
                §6/addtemplate reload       重载配置
            """.trimIndent())
            return true
        }
        when (p2[0]) {
            "start" -> { // 给与工具
                p.buildPlatform()
                p.sendMessage("§b[SpeedBuild]§6 现在开始建造吧然后使用/addtemplate save <模板名>保存")
            }

            "save" -> {
                // 自动获取 7x7x7大小的方块
                if (p2.size != 2) {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate save <name>")
                    return true
                }
                if (!settingTemplate.containsKey(p)) {
                    p.sendMessage("§b[SpeedBuild]§6请先使用 /addtemplate start")
                    return true
                }
                p.createTemplate(p2[1])
            }

            "return" -> {
                // 返回模板
                if (p2.size != 2) {
                    p.sendMessage("§b[SpeedBuild]§6参数错误 /addtemplate return <name>")
                    return true
                }
                if (!GameVMData.templateList.keys.any { it == p2[1] }) {
                    p.sendMessage("§b[SpeedBuild]§6模板不存在")
                    return true
                }
                val middle = p.location.add(0.0, -1.0, 0.0).block
                showTemplate(
                    mutableListOf(
                    ArenaIslandData(p.location, middle.location.block, IslandFace.NORTH)
                ), GameVMData.templateList.keys.first { it == p2[1] })
                p.sendMessage("§b[SpeedBuild]§6已重现模板 ${p2[1]}")
            }

            "list" -> {
                // 返回模板
                p.templateList()
            }

            "look" -> {
                // 查看方块
                val loc = p.getTargetBlock(null as Set<Material>?, 10).location
                val block = loc.block
                p.sendMessage("§b[SpeedBuild]§6${block.type} ${block.data}")
            }

            "reload" -> {
                SpeedBuildReloaded.instance.blockTemplate.load(SpeedBuildReloaded.instance.blockTemplateFile)
                loadTemplate()
                p.sendMessage("§b[SpeedBuild]§6重载配置文件")
            }

        }
        return true
    }
}
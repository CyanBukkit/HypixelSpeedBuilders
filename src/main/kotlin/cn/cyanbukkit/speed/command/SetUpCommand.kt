package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.data.IslandFace
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.utils.toConfig
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * 0 直接控制配置文件 和保存
 */
class SetUpCommand : Command("speedsetup",
    "SpeedBuildReloaded的配置指令",
    "/speedsetup <参数>",
    listOf("ssu")) {


    init {
        permission = "speedbuildreloaded.admin.setup"
    }

    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        if (!p0.hasPermission(permission)) {
            p0.sendMessage(configSettings!!.mess.noPermission)
            return true
        }
        if (p0 !is Player) {
            p0.sendMessage("§b[SpeedBuild]§6该指令只能由玩家执行")
        }
        if (p2.isEmpty()) {
            p0.sendMessage("§b[SpeedBuild]§6参数错误请用help")
            return true
        }
        when (p2[0]) {

            "help" -> {
                val message = arrayOf(
                    "§6------------- §b[SpeedBuilder-§eHypixel]§6 -------------",
                    " ",
                    "                      §6 地图配置部分 ",
                    " ",
                    "/ssu create <世界名字> <最小开始人数> <岛上玩家限制>   ----- 创建地图主指令",
                    "/ssu set-wait-lobby <世界名字>                      ----- 设置等待大厅出生点 <站在方块上>",
                    "/ssu set-show-island-middle <世界名字>              ----- 设置站在的位置为中岛展示出生点 <站在方块上>",
                    " ",
                    "                      §6岛屿部分 ",
                    " ",
                    "/ssu create-island <地图> <岛屿序列/名字>             ----- <站在方块上>设置一个新的岛屿并且放置玩家出生点",
                    "/ssu set-island-middle-block  <地图>  <岛屿序列/名字> ----- 设置建造岛中间的方块浙江确认岛屿的岛屿模板和展示建筑的基础方块",
                    "/ssu set-island-face <地图> <岛屿序列/名字> <面向>     ----- 设置岛屿面向",
                    " ",
                    "§6------------- §b[SpeedBuilder-§eHypixel§b]§6 -------------",)
                p0.sendMessage(message.joinToString("\n"))
                return true;
            }
            "create" -> {
                if (p2.size == 4) {
                    val worldName = p2[1]
                    val min = try {
                        p2[2].toInt()
                    } catch (e: Exception) {
                        p0.sendMessage("§b[SpeedBuild]§6最小开始人数必须是数字")
                        return true
                    }
                    val maxIsland = try {
                        p2[3].toInt()
                    } catch (e: Exception) {
                        p0.sendMessage("§b[SpeedBuild]§6岛上玩家限制必须是数字")
                        return true
                    }
                    SpeedBuildReloaded.instance.settings.set("$worldName.WorldName", worldName)
                    SpeedBuildReloaded.instance.settings.set("$worldName.MinimumPlayers", min)
                    SpeedBuildReloaded.instance.settings.set("$worldName.IsLandPlayerLimit", maxIsland)
                    SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                    p0.sendMessage("§b[SpeedBuild]§6地图${worldName}创建完成！")
                    return true
                } else {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
            }
            "set-wait-lobby" -> {
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[1]
                val p = p0 as Player
                SpeedBuildReloaded.instance.settings.set("$worldName.WaitingLobby", p.location.toConfig())
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置等待大厅出生点")
                return true
            }
            "set-show-island-middle"-> {
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[1]
                val p = (p0 as Player).location.block
                SpeedBuildReloaded.instance.settings.set("$worldName.MiddleIsland", p.toConfig())
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置老师出生点")
                return true
            }
            "create-island"-> {
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val p = p0 as Player
                SpeedBuildReloaded.instance.settings.set("${p2[1]}.IsLand.${p2[2]}.PlayerSpawn", p.location.toConfig())
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置玩家出生点")
                return true
            }
            "set-island-middle-block"->{
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val p = (p0 as Player).location.block
                SpeedBuildReloaded.instance.settings.set("${p2[1]}.IsLand.${p2[2]}.MiddleBlock", p.toConfig())
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置建造岛中间的方块")
                return true
            }
            "set-island-face"->{
                if (p2.size != 4) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                SpeedBuildReloaded.instance.settings.set("${p2[1]}.IsLand.${p2[2]}.IsLandFace", p2[3])
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置岛屿区域")
                return true
            }
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        // 根据输入的内容进行补全
        val list = mutableListOf<String>()
        if (args.size == 1) {
            list.add("create")
            list.add("help")
            list.add("set-wait-lobby")
            list.add("set-show-island-middle")
            list.add("create-island")
            list.add("set-island-middle-block")
            list.add("set-island-face")
            // 根据已输入的文字部分进行重新排列
            return list.filter { it.startsWith(args[0]) }.toMutableList()
        } else if (args.size == 2) {
            when (args[0]) {
                "create" -> {
                    return mutableListOf("世界名字", "最小开始人数", "岛上玩家限制")
                }
                "set-wait-lobby" -> {
                    return mutableListOf("世界名字")
                }
                "set-show-island-middle" -> {
                    return mutableListOf("世界名字")
                }
                "create-island" -> {
                    return mutableListOf("地图","岛屿序列/名字" )
                }
                "set-island-middle-block" -> {
                    return mutableListOf("地图","岛屿序列/名字" )
                }
                "set-island-face" -> {
                    return mutableListOf("地图","岛屿序列/名字", "面向")
                }
            }
        } else if (args.size == 3) {
            when (args[0]) {
                "create" -> {
                    return mutableListOf("最小开始人数", "岛上玩家限制")
                }
                "create-island" -> {
                    return mutableListOf("岛屿序列/名字")
                }
                "set-island-middle-block" -> {
                    return mutableListOf("岛屿序列/名字")
                }
            }
        } else if (args.size == 4) {
            when (args[0]) {
                "set-island-face" -> {
                    return IslandFace.entries.map { it.name }.toMutableList()
                }
            }
        }
        return mutableListOf()
    }


}
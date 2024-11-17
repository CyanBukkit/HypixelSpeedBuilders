package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.command.setup.SetUpArena
import cn.cyanbukkit.speed.command.setup.SetUpArena.left
import cn.cyanbukkit.speed.command.setup.SetUpArena.right
import cn.cyanbukkit.speed.data.Region
import cn.cyanbukkit.speed.game.GameRegionManager.serialize
import cn.cyanbukkit.speed.game.LoaderData
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
            p0.sendMessage(LoaderData.configSettings!!.mess.noPermission)
            return true
        }
        if (p0 !is Player) {
//            p0.nextStep()
//        } else {
            p0.sendMessage("§b[SpeedBuild]§6该指令只能由玩家执行")
        }
        if (p2.isEmpty()) {
            p0.sendMessage("§b[SpeedBuild]§6参数错误")
            return true
        }
        when (p2[0]) {
            "givetool" -> {
                val im= ItemStack(Material.BLAZE_ROD)
                val imMeta = im.itemMeta
                imMeta.displayName = "§b绑图工具"
                im.itemMeta = imMeta
                val p = p0 as Player
                p.inventory.addItem(im)
                p0.sendMessage("§b[SpeedBuild]§6已给予你绑图工具")
                SetUpArena.register()
                return true
            }

            "help" -> {
                p0.sendMessage("§6------------- §b[LMCSpeedBuild]§6 -------------")
                p0.sendMessage(" ")
                p0.sendMessage("                      §6 地图配置部分 ")
                p0.sendMessage(" ")
                p0.sendMessage("/ssu addgame <世界名字> <最小开始人数> <岛上玩家限制> <启动守卫者> ----- 创建地图主指令");
                p0.sendMessage("/ssu setarenaregions <世界名字> ----- 设置竞技场总区域");
                p0.sendMessage("/ssu setwaitingregion <世界名字> ----- 设置等待大厅区域");
                p0.sendMessage("/ssu setwaitinglobbyspawn <世界名字> ----- 设置等待大厅出生点 <站在方块上>");
                p0.sendMessage("/ssu setmiddleisland <世界名字> ----- 设置守卫者出生点 <站在方块上>");
                p0.sendMessage("/ssu givetool ----- 给予玩家绑图工具");
                p0.sendMessage(" ");
                p0.sendMessage("                      §6岛屿部分 ");
                p0.sendMessage(" ")
                p0.sendMessage("/ssu setplayerspawn <岛屿序列/名字> <地图>  ----- 设置玩家出生点 <站在方块上>");
                p0.sendMessage("/ssu setmiddleblock <岛屿序列/名字> <地图>  ----- 设置建造岛中间的方块 <站在方块上>");
                p0.sendMessage("/ssu setislandregions <岛屿序列/名字> <地图>   ----- 设置岛屿区域 ");
                p0.sendMessage("/ssu setbuildregions <岛屿序列/名字> <地图>    ----- 设置建造区域 <站需要规范xzy坐标差需要保持一致>");
                p0.sendMessage(" ")
                p0.sendMessage("§6------------- §b[LMCSpeedBuild]§6 -------------")
                return true;
            }

            "addgame" -> {
                if (p2.size == 5) {
                    val worldName = p2[1]
                    val min = try {
                        p2[2].toInt()
                    } catch (e: Exception) {
                        p0.sendMessage("§b[SpeedBuild]§6最小开始人数必须是数字")
                        return true
                    }
                    val smallxy = try {
                        p2[3].toInt()
                    } catch (e: Exception) {
                        p0.sendMessage("§b[SpeedBuild]§6岛上玩家限制必须是数字")
                        return true
                    }
                    val EnableElderGuardian = try {
                        p2[4].toBoolean()
                    } catch (e: Exception) {
                        p0.sendMessage("§b[SpeedBuild]§6启动守卫者必须是true/false")
                        return true
                    }
                    SpeedBuildReloaded.instance.settings.set("$worldName.WorldName", worldName)
                    SpeedBuildReloaded.instance.settings.set("$worldName.MinimumPlayers", min)
                    SpeedBuildReloaded.instance.settings.set("$worldName.IsLandPlayerLimit", smallxy)
                    SpeedBuildReloaded.instance.settings.set("$worldName.EnableElderGuardian", EnableElderGuardian)
                    SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                    p0.sendMessage("§b[SpeedBuild]§6地图${worldName}以创建完成！")
                    return true
                } else {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
            }

            "setarenaregions" -> {
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                if (SetUpArena.isLateInit()) {
                    val worldName = p2[1]
                    val reg = Region(left.location, right.location).serialize()
                    SpeedBuildReloaded.instance.settings.set("$worldName.ArenaRegions", reg)
                    SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                    p0.sendMessage("§b[SpeedBuild]§6已设置竞技场总区域")
                }
            }

            "setwaitingregion" -> {
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                if (SetUpArena.isLateInit()) {
                    val worldName = p2[1]
                    val reg = Region(left.location, right.location).serialize()
                    SpeedBuildReloaded.instance.settings.set("$worldName.WaitingRegion", reg)
                    SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                    p0.sendMessage("§b[SpeedBuild]§6已设置等待大厅区域")
                }
            }

            "setwaitinglobbyspawn"->{
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[1]
                val p = p0 as Player
                SpeedBuildReloaded.instance.settings.set("$worldName.WaitingLobby", "${p.location.x},${p.location.y},${p.location.z},${p.location.yaw},${p.location.pitch}")
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置等待大厅出生点")
                return true
            }

            "setmiddleisland"-> {
                if (p2.size != 2) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[1]
                val p = p0 as Player
                SpeedBuildReloaded.instance.settings.set("$worldName.MiddleIsland", "${p.location.x},${p.location.y},${p.location.z},${p.location.yaw},${p.location.pitch}")
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置守卫者出生点")
                return true
            }

            "setplayerspawn"-> {
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[2]
                val p = p0 as Player
                SpeedBuildReloaded.instance.settings.set("$worldName.IsLand.${p2[1]}.PlayerSpawn", "${p.location.x},${p.location.y},${p.location.z},${p.location.yaw},${p.location.pitch}")
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置玩家出生点")
                return true
            }

            "setmiddleblock"->{
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[2]
                val p = p0 as Player


                SpeedBuildReloaded.instance.settings.set("$worldName.IsLand.${p2[1]}.MiddleBlock", "${p.location.blockX},${p.location.blockY - 1},${p.location.blockZ}")
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置建造岛中间的方块")
                return true
            }

            "setislandregions"->{
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[2]
                val reg = Region(left.location, right.location).serialize()
                SpeedBuildReloaded.instance.settings.set("$worldName.IsLand.${p2[1]}.IsLandRegions", reg)
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置岛屿区域")
                return true
            }

            "setbuildregions"->{
                if (p2.size != 3) {
                    p0.sendMessage("§b[SpeedBuild]§6参数错误")
                    return true
                }
                val worldName = p2[2]
                val reg = Region(left.location, right.location).serialize()
                SpeedBuildReloaded.instance.settings.set("$worldName.IsLand.${p2[1]}.BuildRegions", reg)
                SpeedBuildReloaded.instance.settings.save(SpeedBuildReloaded.instance.settingsFile)
                p0.sendMessage("§b[SpeedBuild]§6已设置建造区域")
                return true
            }

        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        // 根据输入的内容进行补全
        val list = mutableListOf<String>()
        if (args.size == 1) {
            list.add("givetool")
            list.add("help")
            list.add("addgame")
            list.add("setarenaregions")
            list.add("setwaitingregion")
            list.add("setWaitinglobbyspawn")
            list.add("setmiddleisland")
            list.add("setplayerspawn")
            list.add("setmiddleblock")
            list.add("setislandregions")
            list.add("setbuildregions")
            // 根据已输入的文字部分进行重新排列
            return list.filter { it.startsWith(args[0]) }.toMutableList()
        } else if (args.size == 2) {
            when (args[0]) {
                "addgame" -> {
                    return mutableListOf("世界名字", "最小开始人数", "岛上玩家限制", "启动守卫者")
                }
                "setarenaregions" -> {
                    return mutableListOf("世界名字")
                }
                "setwaitingregion" -> {
                    return mutableListOf("世界名字")
                }
                "setWaitinglobbyspawn" -> {
                    return mutableListOf("世界名字")
                }
                "setmiddleisland" -> {
                    return mutableListOf("世界名字")
                }
                "setplayerspawn" -> {
                    return mutableListOf("岛屿序列/名字", "地图")
                }
                "setmiddleBlock" -> {
                    return mutableListOf("岛屿序列/名字", "地图")
                }
                "setislandregions" -> {
                    return mutableListOf("岛屿序列/名字", "地图")
                }
                "setbuildregions" -> {
                    return mutableListOf("岛屿序列/名字", "地图")
                }
            }
        } else if (args.size == 3) {
            when (args[0]) {
                "addgame" -> {
                    return mutableListOf("最小开始人数", "岛上玩家限制", "启动守卫者")
                }
                "setplayerspawn" -> {
                    return mutableListOf("地图")
                }
                "setmiddleBlock" -> {
                    return mutableListOf("地图")
                }
                "setislandregions" -> {
                    return mutableListOf("地图")
                }
                "setbuildregions" -> {
                    return mutableListOf("地图")
                }
            }
        }
        return mutableListOf()
    }


}
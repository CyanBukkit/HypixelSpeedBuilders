package cn.cyanbukkit.speed.game

import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.Region
import cn.cyanbukkit.speed.game.GameVMData.templateList
import cn.cyanbukkit.speed.game.build.TemplateBlockData
import cn.cyanbukkit.speed.game.build.TemplateData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import kotlin.math.abs

object GameRegionManager {

    /**
     * 根据模板判断是不是成功
     */
    fun isSuccessPlace(temp: List<TemplateBlockData>, middle: Block): Boolean {
        temp.forEach {
            val block = middle.getRelative(it.x, it.y, it.z)
            if (block.type != Material.getMaterial(it.type) || block.data != it.data.toByte()) {
                return false
            }
        }
        return true
    }


    /**
     * 计算Region的内的直径
     */
    private fun Region.getRegionDiameter(): Int {
        val chang = abs(pos1.blockX - pos2.blockX) + 1
        val gao = abs(pos1.blockY - pos2.blockY) + 1
        val kuan = abs(pos1.blockZ - pos2.blockZ) + 1
        return maxOf(chang, gao, kuan)
    }


    /**
     * 序列化用于保存SetupArena 用的
     */
    fun Region.serialize(): String {
        val config = YamlConfiguration()
        config.set("pos1", "${pos1.blockX},${pos1.blockY},${pos1.blockZ}")
        config.set("pos2", "${pos2.blockX},${pos2.blockY},${pos2.blockZ}")
        return config.saveToString()
    }


    /**
     * 反序列化
     */
    fun String.deserialize(worldName: String): Region {
        val config = YamlConfiguration()
        config.loadFromString(this)
        val pos1 = config.getString("pos1")
        val pos2 = config.getString("pos2")
        return Region(
            Location(
                Bukkit.getWorld(worldName),
                pos1.split(",")[0].toDouble(),
                pos1.split(",")[1].toDouble(),
                pos1.split(",")[2].toDouble()
            ),
            Location(
                Bukkit.getWorld(worldName),
                pos2.split(",")[0].toDouble(),
                pos2.split(",")[1].toDouble(),
                pos2.split(",")[2].toDouble()
            )
        )
    }


    /**
     * 根据Region的Size大小找合适的Tempelate
     */
    fun Region.fineTemplate(): TemplateData? {
        val chang = this.pos1.blockX - this.pos2.blockX + 1
        val gao = this.pos1.blockY - this.pos2.blockY + 1
        val kuan = this.pos1.blockZ - this.pos2.blockZ + 1
        val dataList = mutableListOf<TemplateData>()
        templateList.forEach {
            if (it.key.size.chang == chang && it.key.size.gao == gao && it.key.size.kuan == kuan) {
                dataList.add(it.key)
            }
        }
        return try {
            dataList.random()
        } catch (e: Exception) {
            null
        }
    }


    /**
     * 查区域是不是奇数用的
     */
    fun Player.buildRegionOrMakeTemplate(aReg: Region): Region? {
        // 测量x 与z 相差必须是奇数 比如9x9 11x11格
        val x = abs(aReg.pos1.blockX - aReg.pos2.blockX) + 1
        val y = abs(aReg.pos1.blockY - aReg.pos2.blockY) + 1
        val z = abs(aReg.pos1.blockZ - aReg.pos2.blockZ) + 1
        this.sendMessage("§c使用圈地圈出区域${x}x${y}x${z}位置 \n ${aReg.pos1}  ${aReg.pos2}")
        // 如果是偶数return null
        if (x % 2 == 0 || y % 2 == 0 || z % 2 == 0) {
            this.sendMessage("§c请使用奇数格圈地")
            return null
        }
        // 保存区域
        return aReg
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


}
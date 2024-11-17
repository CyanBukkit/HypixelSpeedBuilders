package cn.cyanbukkit.speed.game

import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.TemplateBlockData
import cn.cyanbukkit.speed.task.GameInitTask
import cn.cyanbukkit.speed.utils.EntityTypes
import cn.cyanbukkit.speed.utils.Teacher
import org.bukkit.Location
import org.bukkit.block.Block
import kotlin.math.atan2


object GameHandle {


    /**
     * 对比的方法并屏分 满分100分
     */
    fun List<TemplateBlockData>.compare(middle: Block): Int {
        val simpleScore = 100.0 / this.size
        var score = 100.0
        this.forEach {
            val block = middle.getRelative(
                it.x, it.y, it.z
            )
            if (block.type.name != it.type || block.data != it.data.toByte()) {
                score -= simpleScore
            }
        }
        return score.toInt()
    }




    /**
     * 生成老师
     */
    fun Location.createEntity() : Teacher {
        val cWorld = this.world as org.bukkit.craftbukkit.v1_8_R3.CraftWorld
        val nmsWorld = cWorld.handle
        val w = Teacher(nmsWorld)
        w.isElder = true
        EntityTypes.GUARDIAN.spawnEntity(w,this)
        // 设置无重力
        w.noclip = true
        GameInitTask.clearWatch(w)
        return w
    }


    /**
     * 根据中心点计算 另一个位置位于中心点的那个yaw
     */
    fun Location.getYaw(data: ArenaSettingData): Float {
        val middle = data.middleIsland.toLocation(data.worldName)
        val x = this.x - middle.x
        val z = this.z - middle.z
        val yaw = Math.toDegrees(atan2(x, z)).toFloat()
        return if (yaw < 0) yaw + 360 else yaw
    }




}
package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.utils.Title
import org.bukkit.Sound
import org.bukkit.entity.Player

object SendSign {
    fun Player.send(
        mess : String,
        time : Int,
        title : Boolean = false,
        subTitle: Boolean = false,
        actionBar : Boolean = false,
        enabledSound: Boolean = true,
        sound : Sound = Sound.NOTE_PLING,
        volume : Float = 1f,
        pitch : Float = 1f
    ) {
        val message = mess.replace("%time%", time.toString())
        this.sendMessage(message)
        if (title) {
            Title.title(this, message, "")
        }
        if (subTitle) {
            Title.title(this, "", message)
        }
        if (actionBar) {
            Title.actionbar(this, message)
        }

        this.playSound(this.location, sound, volume, pitch)
        this.level = time
    }
}
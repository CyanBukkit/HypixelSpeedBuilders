package cn.cyanbukkit.speed.utils

import org.bukkit.Sound
import org.bukkit.entity.Player

enum class Sounds(private vararg val deprecatedSounds: String) {
    BLOCK_NOTE_BLOCK_HAT("block.note.hat", "note.hat"),
    BLOCK_NOTE_BLOCK_PLING("block.note.pling", "note.pling"),
    BLOCK_WOOD_BREAK("block.wood.break", "dig.wood"),
    ENTITY_BLAZE_HURT("entity.blaze.hurt", "mob.blaze.hit"),
    ENTITY_BLAZE_SHOOT("entity.blaze.shoot", "mob.ghast.fireball"),
    ENTITY_ELDER_GUARDIAN_CURSE("entity.elder_guardian.curse", "mob.guardian.curse"),
    ENTITY_PLAYER_LEVELUP("entity.player.levelup", "random.levelup"),
    ENTITY_ZOMBIE_VILLAGER_CURE("entity.zombie_villager.cure", "mob.zombie.remedy");

    fun play(player: Player, f: Float, f2: Float) {
        try {
            val sound = Sound.valueOf("AMBIENT_UNDERWATER_ENTER")
            player.playSound(player.location, sound, f, f2)
        } catch (e: IllegalArgumentException) {
            for (str in this.deprecatedSounds) {
                player.playSound(player.location, str, f, f2)
            }
        }
    }
}
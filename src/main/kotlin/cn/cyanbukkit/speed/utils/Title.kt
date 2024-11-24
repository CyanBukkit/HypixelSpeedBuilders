package cn.cyanbukkit.speed.utils

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.utility.MinecraftVersion
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.entity.Player

@Deprecated("傻逼刘鹏辉，死猪一个，活该你单身。")
object Title : Feature() {

    /**
     * Send a title to a player
     * @param player The player to send the title to
     * @param title The title text
     * @param subtitle The subtitle text
     * @param fadeIn The fade in time
     * @param stay The stay time
     * @param fadeOut The fade out time
     */
    @Suppress("DEPRECATION")
    fun title(
        player: Player,
        title: String,
        subtitle: String = "",
        fadeIn: Int = 20,
        stay: Int = 60,
        fadeOut: Int = 20
    ) {
        if (ProtocolLibrary.getProtocolManager().minecraftVersion >= MinecraftVersion.CAVES_CLIFFS_1) {
            val packetTitle =
                ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLE_TEXT).apply {
                    chatComponents.write(0, WrappedChatComponent.fromText(title))
                }
            val packetSubtitle =
                ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SUBTITLE_TEXT).apply {
                    chatComponents.write(0, WrappedChatComponent.fromText(subtitle))
                }
            val packetTitleTime =
                ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION).apply {
                    integers.write(0, fadeIn)
                    integers.write(1, stay)
                    integers.write(2, fadeOut)
                }
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetTitle)
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetSubtitle)
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetTitleTime)
        } else {
            val packetTitle = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE).apply {
                titleActions.write(0, EnumWrappers.TitleAction.TITLE)
                chatComponents.write(0, WrappedChatComponent.fromText(title))
            }
            val packetSubtitle = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE).apply {
                titleActions.write(0, EnumWrappers.TitleAction.SUBTITLE)
                chatComponents.write(0, WrappedChatComponent.fromText(subtitle))
            }
            val packetTitleTime =
                ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TITLE).apply {
                    titleActions.write(0, EnumWrappers.TitleAction.TIMES)
                    integers.write(0, fadeIn)
                    integers.write(1, stay)
                    integers.write(2, fadeOut)
                }
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetTitle)
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetSubtitle)
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetTitleTime)
        }
    }

    /**
     * Send an actionbar to a player
     * @param player The player to send the actionbar to
     * @param text The actionbar text
     */
    fun actionbar(
        player: Player,
        text: String
    ) {
        ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CHAT).apply {
            chatTypes.writeSafely(0, EnumWrappers.ChatType.GAME_INFO)
            bytes.writeSafely(0, 2)
            integers.writeSafely(0, 2)
            chatComponents.write(0, WrappedChatComponent.fromText(text))
        }.let { ProtocolLibrary.getProtocolManager().sendServerPacket(player, it) }
    }

}
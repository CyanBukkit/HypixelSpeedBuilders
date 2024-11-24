package cn.cyanbukkit.speed.api.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEliminateEvent  extends Event {

    public Player player;
    public Block mineBlock;

    public PlayerEliminateEvent(Player player, Block mineBlock) {
        this.player = player;
        this.mineBlock = mineBlock;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

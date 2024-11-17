package cn.cyanbukkit.speed.api.event;


import cn.cyanbukkit.speed.data.ArenaSettingData;
import cn.cyanbukkit.speed.game.GameStatus;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class GameChangeEvent extends Event {

    public GameChangeEvent(ArenaSettingData mapName, GameStatus status) {
        this.mapName = mapName;
        this.status = status;
    }

    public GameChangeEvent(boolean isAsync, ArenaSettingData mapName, GameStatus status) {
        super(isAsync);
        this.mapName = mapName;
        this.status = status;
    }

    public ArenaSettingData mapName;

    public GameStatus status;

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}

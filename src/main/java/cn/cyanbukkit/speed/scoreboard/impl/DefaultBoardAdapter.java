package cn.cyanbukkit.speed.scoreboard.impl;


import cn.cyanbukkit.speed.data.ArenaSettingData;
import cn.cyanbukkit.speed.game.GameStatus;
import cn.cyanbukkit.speed.scoreboard.BoardAdapter;
import cn.cyanbukkit.speed.game.GameVMData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;


public class DefaultBoardAdapter implements BoardAdapter {

    private final ArenaSettingData arenaIslandData;

    public DefaultBoardAdapter(ArenaSettingData arenaIslandData) {
        this.arenaIslandData = arenaIslandData;
    }

    @Override
    public String getTitle() {
        if (GameVMData.INSTANCE.getGameStatus() == GameStatus.WAITING) {
            return Objects.requireNonNull(GameVMData.INSTANCE.getConfigSettings()).getScoreBroad().get("Wait").getTitle();
        }else {
            return Objects.requireNonNull(GameVMData.INSTANCE.getConfigSettings()).getScoreBroad().get("Gaming").getTitle();
        }
    }

    @Override
    public List<String> getStrings(Player p) {
        return GameVMData.INSTANCE.getHotScoreBroadLine();
    }


}

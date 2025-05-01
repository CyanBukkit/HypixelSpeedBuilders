package cn.cyanbukkit.murdermystery.scoreboard;

import org.bukkit.entity.Player;

import java.util.List;

public interface BoardAdapter {

    String getTitle();
    List<String> getStrings(Player p);
}

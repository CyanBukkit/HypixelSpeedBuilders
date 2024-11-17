package cn.cyanbukkit.speed.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BoardRunnable extends BukkitRunnable {

    BoardManager manager;

    public BoardRunnable(BoardManager manager){
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.getBoards().forEach((uuid, board) -> {
            Player player = Bukkit.getPlayer(uuid);
            board.update(manager.getAdapter().getTitle(),manager.getAdapter().getStrings(player));
        });
    }
}

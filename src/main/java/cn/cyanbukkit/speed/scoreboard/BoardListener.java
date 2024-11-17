package cn.cyanbukkit.speed.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BoardListener implements Listener {
    private BoardManager manager;

    public BoardListener(BoardManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        IBoard board = Board.VersionType.V1_13.isHigherOrEqual() ? new Board(player):new OldBoard(player);
        manager.getBoards().putIfAbsent(player.getUniqueId(),board);
//        board.updateLines(manager.getAdapter().getStrings(player));
        board.update(manager.getAdapter().getTitle(),manager.getAdapter().getStrings(player));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (manager.getBoards().containsKey(player.getUniqueId())) {
            manager.getBoards().get(player.getUniqueId()).delete();
            manager.getBoards().remove(player.getUniqueId());
        }
    }
}

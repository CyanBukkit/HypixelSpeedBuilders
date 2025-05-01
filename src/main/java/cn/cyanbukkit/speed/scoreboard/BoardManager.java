package cn.cyanbukkit.speed.scoreboard;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

@Getter @Setter
public class BoardManager {
    private HashMap<UUID, IBoard> boards;
    private Plugin                                              plugin;
    private BoardAdapter adapter;
    private BoardListener                                       listener;
    private BukkitRunnable runnable;

    public BoardManager(Plugin plugin, BoardAdapter adapter){
        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") == null){
            plugin.getLogger().warning("发包依赖无法找到! 停止加载...");
            return;
        }
        this.plugin = plugin;
        this.adapter = adapter;
        init();
    }

    public void init(){
        boards = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            IBoard board = Board.VersionType.V1_13.isHigherOrEqual() ? new Board(player) : new OldBoard(player);
            boards.putIfAbsent(player.getUniqueId(), board);
            board.update(adapter.getTitle(),adapter.getStrings(player));
//            board.(adapter.getTitle());
//            board.updateLines(adapter.getStrings(player));
        }
        (runnable = new BoardRunnable(this)).runTaskTimerAsynchronously(plugin,0L,10L);
        Bukkit.getPluginManager().registerEvents(listener = new BoardListener(this),plugin);
    }

    public void dispose(){
        HandlerList.unregisterAll(listener);
        boards.values().forEach(IBoard::delete);
        boards.clear();
        if (runnable != null) runnable.cancel();
    }
}

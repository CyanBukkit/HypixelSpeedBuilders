package cn.cyanbukkit.speed.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameLoopTask extends BukkitRunnable {
    private final GameInitTask gameInitTask;
    private final JavaPlugin plugin;

    public GameLoopTask(JavaPlugin plugin,GameInitTask gameInitTask){
        this.plugin = plugin;
        this.gameInitTask = gameInitTask;
    }
    @Override
    public void run(){
        gameInitTask.run();
    }
}

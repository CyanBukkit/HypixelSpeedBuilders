package cn.cyanbukkit.speed.bukkit;

import cn.cyanbukkit.speed.data.GameStatus;
import cn.cyanbukkit.speed.SpeedBuildReloaded;
import cn.cyanbukkit.speed.data.Region;
import cn.cyanbukkit.speed.game.LoadData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
/**
 * @implNote  主线程接入 主线程
 */
public class GameInitTask implements Runnable{
    private List<Player> players;
    private GameStatus gameStatus;
    private int countdown;
    private YamlConfiguration config;
    private int maxPlayerCount;

    //玩家 游戏状态初始化
    public GameInitTask() {
        this.players = new ArrayList<>();
        this.gameStatus = GameStatus.WAITING;
        this.config = LoadData.INSTANCE.getConfigSettings().
    }

    @Override
    public void run() {
        GameLoopTask gameLoopTask = new GameLoopTask(this, gameInitTask);
        gameLoopTask.runTaskTimer(this, 0L, 20L);

        // 这段对 但是 GameTask runTaskLater  (也就是说我调用的 GameInitTask 是用于初始化的
        // 因为考虑到要调用接口中的模式ServerMode （一端一图是客户要的模式 而这个插件 后期还会放在我的服务器 我也就弄了个一端多图 方便多事件调用
        // 而且还有个不创建循环Task的 Lobby 模式 后续写MySQl方便 客户调用做大厅服 ）)还写MYSQL是吧
        // 你需另外  自己创建一个 Game循环Task
        switch (gameStatus){
            //等待区域
            case GameStatus.WAITING:
                //???加入玩家逻辑
                if(players.size() >= maxPlayerCount){
                    //将超出最大玩家数量的玩家从玩家列表中移除
                    if(players.size() > maxPlayerCount)players.subList(maxPlayerCount,players.size()).clear();
                    //更改游戏状态为开始
                    gameStatus = GameStatus.STARTING;
                }
                break;
            case GameStatus.STARTING:
                //???若玩家没有参加过游戏则初始化排行榜
                //保存配置文件
                SpeedBuildReloaded.instance.saveResource("playerData.yml",false);
                //???文字输出
                gameStatus = GameStatus.OBSERVING;
                break;
            case GameStatus.OBSERVING:
                //???区域生成建筑以观察
                //获取时间
                int observeTime = 0;
                countdown = observeTime;
                Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(countdown>0){
                            countdown--;
                            //???文字输出
                        }else {
                            gameStatus = GameStatus.END_OBSERVE;
                            this.cancel();
                        }
                    }
                },0L,20L);
                //???文字输出
                break;
            case GameStatus.END_OBSERVE:
                //???区域删除建筑
                //???文字输出
                gameStatus = GameStatus.BUILDING;
            case GameStatus.BUILDING:
                //???区域建筑+生成屏障
                //???文字输出
                //获取时间
                int buildingTime = 0;
                countdown = buildingTime;
                Bukkit.getScheduler().runTaskTimer(SpeedBuildReloaded.instance, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(countdown>0){
                            countdown--;
                            //???文字输出
                        }else {
                            gameStatus = GameStatus.SCORE;
                            this.cancel();
                        }
                    }
                },0L,20L);
                //???文字输出
                break;
            case GameStatus.SCORE:
                //???区域建筑比对+去掉屏障+玩家变成旁观
                //???文字输出
                if(players.size()>1){
                    gameStatus = GameStatus.STARTING;
                }else{
                    gameStatus = GameStatus.END;
                }
                break;
            case GameStatus.END:
                gameStatus = GameStatus.WAITING;
                players.clear();
                //结算
        }
    }
}
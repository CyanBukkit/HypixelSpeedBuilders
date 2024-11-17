package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.game.GameStatus
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.game.LoaderData.backLobby
import cn.cyanbukkit.speed.game.LoaderData.gameStatus
import cn.cyanbukkit.speed.utils.Title
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.event.server.ServerListPingEvent

class PlayerListener : Listener {


    @EventHandler
    fun onPing(e: ServerListPingEvent) {
        if (LoaderData.nowMap.isEmpty() || gameStatus == GameStatus.NULL) {
            e.motd = "NoMap :("
            return
        }
        val map = LoaderData.nowMap[SpeedBuildReloaded.instance]
        if (gameStatus == GameStatus.WAITING) {
            e.motd = "Waiting..."
        }else if (gameStatus != GameStatus.STARTING
            || gameStatus != GameStatus.OBSERVING
            || gameStatus != GameStatus.BUILDING
            || gameStatus != GameStatus.SCORE
            || gameStatus != GameStatus.END){
            e.motd = "NoMap :("
        }else{
            e.motd = "Starting"
        }
    }


    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        LoaderData.storage.onDefault(e.player)
        val map = LoaderData.nowMap[SpeedBuildReloaded.instance]!!
        if (gameStatus == GameStatus.WAITING) {
            LoaderData.playerStatus[e.player] = PlayerStatus.WAITING

            e.player.inventory.clear()
            e.player.inventory.addItem(backLobby)
            e.player.health = e.player.maxHealth
            e.player.foodLevel = 20
            e.player.gameMode = GameMode.ADVENTURE
            e.player.teleport(map.waitingLobby.toLocation())

            val max = map.islandData.size * map.isLandPlayerLimit
            val now = LoaderData.playerStatus.size
            if (now > max) { // 封頂人數
                e.player.kickPlayer("已满")
                return
            }
            val join = LoaderData.configSettings!!.mess.join
                .replace("%player%", e.player.name)
                .replace("%now%", now.toString())
                .replace("%max%", max.toString())
            e.joinMessage = join

        } else {
            if (LoaderData.playerStatus[e.player] == PlayerStatus.LEAVE) {
                // 进入设置活的
                LoaderData.playerStatus[e.player] = PlayerStatus.LIFE
                return
            }

            e.player.kickPlayer("§b游戏已经开始了")
        }
    }

    @EventHandler
    fun noFoodLevel(e: FoodLevelChangeEvent) {
        e.isCancelled = true
    }


    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val map = LoaderData.nowMap[SpeedBuildReloaded.instance]!!
        if (gameStatus == GameStatus.WAITING) {
            LoaderData.playerStatus.remove(e.player)
            val join = LoaderData.configSettings!!.mess.quit
                .replace("%player%", e.player.name)
                .replace("%now%", LoaderData.playerStatus.size.toString())
                .replace("%max%", (map.islandData.size * map.isLandPlayerLimit).toString())
            e.quitMessage = join
        } else {
            LoaderData.playerStatus[e.player] = PlayerStatus.OUT
        }
    }


    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onDamageByEntity(e: EntityDamageByEntityEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onDamageByBlock(e: EntityDamageByBlockEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        // 防止玩家擅自离开岛的区域就会传送 并且处于 玩家不在淘汰状态下
        val map = LoaderData.nowMap[SpeedBuildReloaded.instance]!!
        if (LoaderData.gameStatus[map] == GameStatus.GAME_STARTING) {
            val island = GameVMData.playerBindIsLand[e.player]!!
            e.player.teleport(island.playerSpawn.toLocation(e.player.world.name))
            return
        }
        if (LoaderData.playerStatus[e.player] == PlayerStatus.LIFE) {
            val island = GameVMData.playerBindIsLand[e.player]!!
            if (!island.islandRegions.isInIsland(e.to)) {
                e.player.teleport(island.playerSpawn.toLocation(e.player.world.name))
                Title.title(e.player, "", LoaderData.configSettings!!.mess.noLeaveRegion)
            }
        }
    }

    @EventHandler
    // 禁止丢系物品
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    // 禁止合成 熔炉 铁砧
//    @EventHandler
//    fun onCraft(e: PrepareItemCraftEvent) {
//
//    }


    @EventHandler
    fun setFight(e: PlayerToggleFlightEvent) {
        if (LoaderData.playerStatus[e.player] == PlayerStatus.LIFE && e.isFlying) {
            e.isCancelled = true
            e.player.velocity = Vector(0, 1, 0).multiply(1.05)
            Sounds.ENTITY_BLAZE_SHOOT.play(e.player, 1.0f, 1.0f)
        }
    }
}
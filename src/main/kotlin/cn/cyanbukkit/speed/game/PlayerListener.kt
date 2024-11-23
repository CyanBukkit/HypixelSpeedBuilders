package cn.cyanbukkit.speed.game

import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.GameVMData.backLobby
import cn.cyanbukkit.speed.game.GameVMData.configSettings
import cn.cyanbukkit.speed.game.GameVMData.gameStatus
import cn.cyanbukkit.speed.game.GameVMData.isInitNowMap
import cn.cyanbukkit.speed.game.GameVMData.nowMap
import cn.cyanbukkit.speed.game.GameVMData.playerStatus
import cn.cyanbukkit.speed.game.GameVMData.spectator
import cn.cyanbukkit.speed.game.GameVMData.storage
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
        if (isInitNowMap() || gameStatus == GameStatus.NULL) {
            e.motd = "NoMap :("
            return
        }
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
        storage.onDefault(e.player)
        if (gameStatus == GameStatus.WAITING) {
            playerStatus[e.player] = PlayerStatus.WAITING
            e.player.inventory.clear()
            e.player.inventory.setItem(0,backLobby)
            e.player.health = e.player.maxHealth
            e.player.foodLevel = 20
            e.player.gameMode = GameMode.ADVENTURE
            e.player.teleport(nowMap.waitingLobby)
            val max = nowMap.islandData.size * nowMap.isLandPlayerLimit
            val now = playerStatus.size
            if (now > max) { // 封頂人數
                e.player.kickPlayer("已满")
                return
            }
            val join = configSettings!!.mess.join
                .replace("%player%", e.player.name)
                .replace("%now%", now.toString())
                .replace("%max%", max.toString())
            e.joinMessage = join

        } else {
            if (playerStatus[e.player] == PlayerStatus.LEAVE) {
                // 进入设置活的
                playerStatus[e.player] = PlayerStatus.LIFE
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
        if (gameStatus == GameStatus.WAITING) {
            playerStatus.remove(e.player)
            val join = configSettings!!.mess.quit
                .replace("%player%", e.player.name)
                .replace("%now%", playerStatus.size.toString())
                .replace("%max%", (nowMap.islandData.size * nowMap.isLandPlayerLimit).toString())
            e.quitMessage = join
        } else {
            if (spectator.contains(e.player)) {
                spectator.remove(e.player)
            }
            playerStatus[e.player] = PlayerStatus.OUT
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
        if (playerStatus[e.player] == PlayerStatus.LIFE) {
            val island = GameVMData.playerBindIsLand[e.player]!!
            if (e.to.y <=0) {
                e.player.teleport(island.playerSpawn)
                Title.title(e.player, "", configSettings!!.mess.noLeaveRegion)
            }
        }else if (spectator.contains(e.player)) {
            if (e.to.y <=0) {
                e.player.teleport(nowMap.middleIsland)
            }
        }
    }

    @EventHandler
    // 禁止丢系物品
    fun onDrop(e: PlayerDropItemEvent) {
        e.isCancelled = true
    }

    @EventHandler
    private fun onInventoryClick(e: InventoryClickEvent) {
        val p = e.whoClicked as Player
        if (spectator.contains(p)) {
            e.isCancelled = true
            return
        }
    }

    //实现旁观者附身
    @EventHandler
    private fun onClickPlayer(e: PlayerInteractAtEntityEvent) {
        if (e.rightClicked is Player && spectator.contains(e.player)) {
            val p = e.rightClicked as Player
            //阻止玩家右键进行与旁观者交互
            if (spectator.contains(p)) return
            Title.title(e.player,"§a你旁观了${p.name}","",10,5,10)
            e.player.gameMode = GameMode.SPECTATOR
            e.player.spectatorTarget = e.rightClicked
        }
        e.isCancelled = true
    }

    //实现旁观者离开玩家身体
    @EventHandler
    private fun onLeavePlayer(e: PlayerToggleSprintEvent) {
        if (spectator.contains(e.player) && e.player.gameMode == GameMode.SPECTATOR) {
            Title.title(e.player,"&a你离开了观战模式","",10,5,10)
            e.player.gameMode = GameMode.SURVIVAL
            e.player.allowFlight = true
            e.player.isFlying = true
        }
    }

    // 禁止合成 熔炉 铁砧
//    @EventHandler
//    fun onCraft(e: PrepareItemCraftEvent) {
//
//    }


//    @EventHandler
//    fun setFight(e: PlayerToggleFlightEvent) {
//        if (playerStatus[e.player] == PlayerStatus.LIFE && e.isFlying) {
//            e.isCancelled = true
//            e.player.velocity = Vector(0, 1, 0).multiply(1.05)
//            Sounds.ENTITY_BLAZE_SHOOT.play(e.player, 1.0f, 1.0f)
//        }
//    }
}
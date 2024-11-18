package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.build.toItemStack
import cn.cyanbukkit.speed.data.BuildStatus
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.GameStatus
import cn.cyanbukkit.speed.task.GameVMData.backLobby
import cn.cyanbukkit.speed.task.GameVMData.buildSign
import cn.cyanbukkit.speed.task.GameVMData.configSettings
import cn.cyanbukkit.speed.task.GameVMData.gameStatus
import cn.cyanbukkit.speed.task.GameVMData.nowMap
import cn.cyanbukkit.speed.task.GameVMData.playerBuildStatus
import cn.cyanbukkit.speed.task.GameVMData.playerStatus
import cn.cyanbukkit.speed.task.GameVMData.spectator
import cn.cyanbukkit.speed.utils.connectTo
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockListener : Listener {



    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (buildSign.contains(e.player)) {
            return
        }
        if (gameStatus != GameStatus.BUILDING) {
            e.isCancelled = true
            return
        }
        val isLand = GameVMData.playerBindIsLand[e.player]!!
        val block = e.block
        if (!isLand.buildRegions.inBuild(block)) {
            e.isCancelled = true
            return
        }
        if (playerBuildStatus[e.player] == BuildStatus.CANTBUILD) {
            e.player.sendMessage("§a很完美，不用继续建造了")
            return
        }
    }


    @EventHandler
    fun chat(e:  AsyncPlayerChatEvent) {
        // 设置样式 p: xxx
        val p = e.player
        val group = when (playerStatus[p]) {
            PlayerStatus.WAITING -> "§a[等待]"
            PlayerStatus.LIFE -> "§e[玩家]"
            PlayerStatus.LEAVE -> "§e[离开]"
            PlayerStatus.OUT -> "§e[淘汰]"
            else -> "§f"
        }
        e.format = "$group§a${e.player.name}§f: ${e.message}"
    }


    @EventHandler
    fun entitySpawn(e: EntitySpawnEvent) {
        // 如果是是凋零就取消
        if (e.entityType == org.bukkit.entity.EntityType.WITHER) {
            e.isCancelled = true
        }
    }


    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (buildSign.contains(e.player)) {
            return
        }
        if (gameStatus != GameStatus.BUILDING) {
            e.isCancelled = true
            return
        }
        if (playerBuildStatus[e.player] == BuildStatus.CANTBUILD) {
            e.player.sendMessage("§a很完美，不用继续建造了")
            return
        }
        val isLand = GameVMData.playerBindIsLand[e.player]!!
        val block = e.block
        if (!isLand.buildRegions.inBuild(block)) {
            e.isCancelled = true
            return
        }

    }


    private val waitGoToLobby = mutableMapOf<Player, Int>()


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPPB(e: PlayerInteractEvent) {
        val mapData = nowMap[SpeedBuildReloaded.instance]!!
        if (gameStatus == GameStatus.WAITING) {
            if (e.hasItem() && e.item == backLobby) {
                if (waitGoToLobby.contains(e.player)) {
                    Bukkit.getScheduler().cancelTask(waitGoToLobby[e.player]!!)
                    waitGoToLobby.remove(e.player)
                    e.player.sendMessage("§a取消回到大厅")
                    return
                }
                waitGoToLobby[e.player] = Bukkit.getScheduler().runTaskLater(SpeedBuildReloaded.instance, {
                    e.player.connectTo(configSettings!!.endReturnToTheLobby, SpeedBuildReloaded.instance)
                }, 60L).taskId
                e.player.sendMessage("§a你将在3秒后回到大厅，如果不想回去请再次点击")
            }
            return
        }
        if (LoaderData.gameStatus[mapData] != GameStatus.BUILDING) {
            return
        }
        val isLand = GameVMData.playerBindIsLand[e.player]?: return
        val block = e.clickedBlock?: return
        if (playerBuildStatus[e.player] == BuildStatus.CANTBUILD) {
            e.player.sendMessage("§a很完美，不用继续建造了")
            e.isCancelled = true
            return
        }
        if (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.LEFT_CLICK_AIR) {
            if (!isLand.buildRegions.inBuild(block)) return
            // 给物品
            e.player.inventory.addItem(block.toItemStack())
            block.type = Material.AIR
            block.data = 0
            // 放置特效效果
            block.world.playEffect(block.location, Effect.SMOKE, 0)
            // 给挖方块的音效
            e.player.playSound(e.player.location, Sound.DIG_STONE, 1f, 1f)
        }
    }







}
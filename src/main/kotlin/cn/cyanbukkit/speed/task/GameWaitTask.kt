package cn.cyanbukkit.speed.task

import cn.cyanbukkit.speed.data.ArenaSettingData
import cn.cyanbukkit.speed.data.PlayerStatus
import cn.cyanbukkit.speed.game.LoaderData
import cn.cyanbukkit.speed.game.LoaderData.hotScoreBroadLine
import cn.cyanbukkit.speed.task.GameInitTask.startGame
import cn.cyanbukkit.speed.utils.Title
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

class GameWaitTask(private val arena: ArenaSettingData) : Runnable {
    private var time = LoaderData.configSettings!!.time.wait

    override fun run() {
        val playerList = mutableListOf<Player>()

        LoaderData.playerStatus.forEach { (player, playerStatus) ->
            run {
                if (playerStatus == PlayerStatus.WAITING) {
                    playerList.add(player)
                }
            }
        }


        val newList = mutableListOf<String>()
        val now = playerList.size
        val max = (arena.islandData.size * arena.isLandPlayerLimit)


        if (now >= arena.minimumPlayers) {
            if (now >= max / 2 &&
                time > LoaderData.configSettings!!.time.forwardWaiting) {
                time = LoaderData.configSettings!!.time.forwardWaiting
                playerList.forEach {
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                }
            }

            time--
        } else {
            time = LoaderData.configSettings!!.time.wait
        }



        when (time) {
            60 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            30 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            10 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            5 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            4 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            3 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            2 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }
            1 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                    Title.title(it,"§c${time}","§e游戏即将开始！",10,20,10)
                    Title.actionbar(it, LoaderData.configSettings!!.mess.countdown.replace("%time%", time.toString()))
                }
            }

            0 -> {
                playerList.forEach { // actionbar
                    it.playSound(it.location, Sound.NOTE_BASS, 1f, 1f)
                }
                startGame(playerList, arena)
                return
            }
        }


        val countdown = time
        val need = max - now
        val time = SimpleDateFormat("yy/MM/dd").format(Date())

        LoaderData.configSettings!!.scoreBroad["Wait"]!!.line.forEach {
            newList.add(
                it
                    .replace("%now%", now.toString())
                    .replace("%mapName%", arena.worldName)
                    .replace("%max%", max.toString())
                    .replace("%countdown%", countdown.toString())
                    .replace("%remainPlayer%", need.toString())
                    .replace("%time%", time)
            )
        }



        hotScoreBroadLine = newList


    }
}
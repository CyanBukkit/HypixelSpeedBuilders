package cn.cyanbukkit.speed.utils

import cn.cyanbukkit.speed.SpeedBuildReloaded
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.event.Listener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object RestoreMap : Listener {

    fun init() {
        //如果 插件文件夹下的有个叫map的文件夹则将文件夹内的文件复制在spigot的个根目录下
        //如果没有则创建一个
        val mapFolder = File(SpeedBuildReloaded.instance.dataFolder,"map")
        Bukkit.getConsoleSender().sendMessage("§a开始检查地图文件夹")
        if (!mapFolder.exists()) {
            mapFolder.mkdir()
        }
        val mapList = mapFolder.listFiles()
        if (mapList.isNullOrEmpty()) {
            return
        }
        mapList.forEach { file ->
            run {
                restoreMap(file.name.split(".")[0], true)
            }
        }
        // 检查Bukkit的spawn-protection是否为0
        if (Bukkit.getServer().spawnRadius != 0) {
            Bukkit.getServer().spawnRadius = 0
            Bukkit.getServer().reload()
        }
    }



    private fun restoreMap(mapName: String, loading: Boolean) {
        val bukkitMap = File("")
        val mapNameFolder = File(bukkitMap.absolutePath, mapName)
        mapNameFolder.delete()
        val mapFolder = File(SpeedBuildReloaded.instance.dataFolder,"map")
        val map = File(mapFolder, mapName)
        val zip = File(mapFolder, "$mapName.zip")
        // 检查Bukkit是否加载这个世界
        if (zip.exists()) {
            // 解压
            Bukkit.getConsoleSender().sendMessage("§a开始解压文件 ${zip.name} 到 ${mapNameFolder.absolutePath}")
            unZipFiles(zip, mapNameFolder)
            if (loading){
                Bukkit.getConsoleSender().sendMessage("§a开始加载世界 $mapName")
                Bukkit.createWorld(WorldCreator(mapName))
            }
            return
        }
        if (map.exists()) {
            // 复制
            Bukkit.getConsoleSender().sendMessage("§a开始复制文件 ${map.name} 到 ${mapFolder.absolutePath}")
            Files.copy(map.toPath(), mapNameFolder.toPath())
            if (loading){
                Bukkit.getConsoleSender().sendMessage("§a开始加载世界 $mapName")
                Bukkit.createWorld(WorldCreator(mapName))
            }
            return
        }
    }



    // 解压文件
    private fun unZipFiles(zip: File, output : File) {
        if (!output.exists()) {
            output.mkdirs()
        }
        val zipFile = ZipInputStream(FileInputStream(zip))
        var zipEntry: ZipEntry?
        while (zipFile.nextEntry.also { zipEntry = it } != null) {
            val fileName = zipEntry!!.name
            val outFile = File(output, fileName)
            if (zipEntry!!.isDirectory) {
                outFile.mkdirs()
            } else {
                outFile.parentFile.mkdirs()
                outFile.createNewFile()
                val out = FileOutputStream(outFile)
                val buf1 = ByteArray(1024)
                var len: Int
                while (zipFile.read(buf1).also { len = it } > 0) {
                    out.write(buf1, 0, len)
                }
                out.close()
            }
        }
        zipFile.closeEntry()
        zipFile.close()
        Bukkit.getConsoleSender().sendMessage("§a解压完成")
    }

}
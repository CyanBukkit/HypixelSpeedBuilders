package cn.cyanbukkit.speed.utils

import cn.cyanbukkit.speed.SpeedBuildReloaded
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.generator.ChunkGenerator
import java.io.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object RestoreMap {
    fun init() {
        val mapFolder = File(SpeedBuildReloaded.instance.dataFolder, "map")
        Bukkit.getConsoleSender().sendMessage("§a开始检查地图文件夹")
        if (!mapFolder.exists()) {
            mapFolder.mkdir()
        }
        val mapList = mapFolder.listFiles()
        if (mapList.isNullOrEmpty()) {
            return
        }
        mapList.forEach { file ->
            try {
                restoreMap(file.name.split(".")[0])
            } catch (e: Exception) {
                Bukkit.getConsoleSender().sendMessage("§c还原地图 ${file.name} 时发生错误: ${e.message}")
                e.printStackTrace()
            }
        }

        if (Bukkit.getServer().spawnRadius != 0) {
            Bukkit.getServer().spawnRadius = 0
            Bukkit.getServer().reload()
        }
    }

    private fun restoreMap(mapName: String) {
        val mapNameFolder = File(Bukkit.getWorldContainer(), mapName)
        val mapFolder = File(SpeedBuildReloaded.instance.dataFolder, "map")
        val zip = File(mapFolder, "$mapName.zip")
        val map = File(mapFolder, mapName)

        // 卸载世界并移除玩家
        Bukkit.getWorld(mapName)?.let { world ->
            world.players.forEach { player ->
                player.teleport(Bukkit.getWorlds()[0].spawnLocation)
            }
            Bukkit.unloadWorld(world, false)
        }

        // 强制删除目标文件夹
        forceDelete(mapNameFolder)

        // 确保目标文件夹不存在
        if (mapNameFolder.exists()) {
            throw IOException("无法完全删除目标文件夹: ${mapNameFolder.absolutePath}")
        }

        when {
            zip.exists() -> {
                Bukkit.getConsoleSender().sendMessage("§a开始解压文件 ${zip.name} 到 ${mapNameFolder.absolutePath}")
                unZipFiles(zip, mapNameFolder)
            }
            map.exists() -> {
                Bukkit.getConsoleSender().sendMessage("§a开始复制文件 ${map.name} 到 ${mapNameFolder.absolutePath}")
                forceCopy(map, mapNameFolder)
            }
            else -> {
                throw FileNotFoundException("未找到地图文件: $mapName")
            }
        }

        // 验证文件夹创建成功
        if (!mapNameFolder.exists()) {
            throw IOException("地图文件夹创建失败: ${mapNameFolder.absolutePath}")
        }

        Bukkit.getConsoleSender().sendMessage("§a开始加载世界 $mapName")
        val creator = WorldCreator(mapName).apply {
            generateStructures(false)
            generator(null as ChunkGenerator?)
        }
        Bukkit.createWorld(creator) ?: throw IllegalStateException("无法创建世界: $mapName")
    }

    private fun forceDelete(file: File) {
        if (!file.exists()) return

        try {
            // 先尝试使用Files.walkFileTree删除
            Files.walkFileTree(file.toPath(), object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.deleteIfExists(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    Files.deleteIfExists(dir)
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: Exception) {
            // 如果上述方法失败，使用递归强制删除
            if (file.exists()) {
                file.listFiles()?.forEach { child ->
                    if (child.isDirectory) {
                        forceDelete(child)
                    } else {
                        child.delete()
                        if (child.exists()) {
                            child.deleteOnExit()
                        }
                    }
                }
                file.delete()
                if (file.exists()) {
                    file.deleteOnExit()
                }
            }
        }

        // 等待确保删除完成
        var attempts = 0
        while (file.exists() && attempts < 10) {
            Thread.sleep(100)
            attempts++
        }
    }

    private fun forceCopy(source: File, destination: File) {
        if (source.isDirectory) {
            if (!destination.exists() && !destination.mkdirs()) {
                throw IOException("无法创建目标目录: ${destination.absolutePath}")
            }

            source.listFiles()?.forEach { file ->
                forceCopy(file, File(destination, file.name))
            }
        } else {
            destination.parentFile?.mkdirs()
            Files.copy(
                source.toPath(),
                destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
            )
        }
    }

    private fun unZipFiles(zip: File, output: File) {
        // 确保输出目录存在
        output.mkdirs()

        ZipInputStream(BufferedInputStream(FileInputStream(zip))).use { zipIn ->
            var entry: ZipEntry?
            val buffer = ByteArray(8192)

            while (zipIn.nextEntry.also { entry = it } != null) {
                val currentEntry = entry ?: continue
                val filePath = File(output, currentEntry.name)

                // 确保解压目标在输出目录内
                if (!filePath.canonicalPath.startsWith(output.canonicalPath)) {
                    throw SecurityException("发现非法ZIP条目: ${currentEntry.name}")
                }

                if (currentEntry.isDirectory) {
                    filePath.mkdirs()
                    continue
                }

                // 确保父目录存在
                filePath.parentFile?.mkdirs()

                // 强制删除已存在的文件
                if (filePath.exists()) {
                    forceDelete(filePath)
                }

                // 创建新文件
                BufferedOutputStream(FileOutputStream(filePath)).use { bos ->
                    var len: Int
                    while (zipIn.read(buffer).also { len = it } > 0) {
                        bos.write(buffer, 0, len)
                    }
                }

                // 设置文件时间戳
                filePath.setLastModified(currentEntry.time)
            }
        }

        Bukkit.getConsoleSender().sendMessage("§a解压完成")
    }
}
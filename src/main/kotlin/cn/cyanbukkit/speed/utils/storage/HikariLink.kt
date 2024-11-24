package cn.cyanbukkit.speed.utils.storage

import cn.cyanbukkit.speed.game.GameVMData.configSettings
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.sql.Connection
import java.sql.SQLTransientConnectionException

class HikariLink : Storage {

    private lateinit var msPool: HikariDataSource

    fun getConnection(): Connection {
        return try {
            getConnection()
        } catch (e: SQLTransientConnectionException) {
            link()
            getConnection()
        }
    }

    override fun link() {
        val config = HikariConfig().apply {
            this.username = configSettings!!.configMySQLData.user
            this.password = configSettings!!.configMySQLData.password
            this.jdbcUrl = configSettings!!.configMySQLData.url
            this.addDataSourceProperty("transaction_isolation", "TRANSACTION_REPEATABLE_READ")
            this.connectionInitSql = """
                CREATE TABLE IF NOT EXISTS sb_user_data(
                    uuid varchar(36) PRIMARY KEY,
                    wins int NULL,
                    eliminate int NULL,
                    restore_build int NULL,
                    fastest_build_time LONGTEXT NULL
                );
         """.trimIndent()
            this.addDataSourceProperty("useLocalSessionState", true)
            maximumPoolSize = 10
            minimumIdle = 10
            idleTimeout = 30000 // 30 seconds
            connectionTestQuery = "SELECT 1"
        }
        msPool = HikariDataSource(config)
        // 创个新表叫sb_island 列 分一个选择的岛 UUID 和一个解锁的所有岛
        val sql = """
            CREATE TABLE IF NOT EXISTS sb_island(
                uuid varchar(36) PRIMARY KEY,
                use_island varchar(36) NULL,
                unlock_island LONGTEXT NULL
            );
        """.trimIndent()
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.execute()
            }
        }
    }

    override fun reload() {
        msPool.close()
        link()
    }

    override fun addWins(p: Player) { // 增加玩家的胜利次数
        val sql = "UPDATE sb_user_data SET wins = wins + 1 WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                it.execute()
            }
        }
    }

    override fun getWins(p: Player): Int { // 获取玩家的胜利次数
        val sql = "SELECT wins FROM sb_user_data WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var wins = 0
                if (resultSet.next()) {
                    wins = resultSet.getInt("wins")
                }
                wins
            }
        }
    }

    override fun addEliminate(p: Player) { // 增加玩家的淘汰次数
        val sql = "UPDATE sb_user_data SET eliminate = eliminate + 1 WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                it.execute()
            }
        }
    }

    override fun getEliminate(p: Player): Int { // 获取玩家的淘汰次数
        val sql = "SELECT eliminate FROM sb_user_data WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var eliminate = 0
                if (resultSet.next()) {
                    eliminate = resultSet.getInt("eliminate")
                }
                eliminate
            }
        }
    }

    override fun addRestoreBuild(p: Player) { // 增加玩家的修复建筑次数
        val sql = "UPDATE sb_user_data SET restore_build = restore_build + 1 WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                it.execute()
            }
        }
    }

    override fun getRestoreBuild(p: Player): Int { // 获取玩家的修复建筑次数
        val sql = "SELECT restore_build FROM sb_user_data WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var restoreBuild = 0
                if (resultSet.next()) {
                    restoreBuild = resultSet.getInt("restore_build")
                }
                restoreBuild
            }
        }
    }


    override fun setFastestBuildTime(p: Player, newValue: Double, mapString: String) { // 设置玩家的最快建造时间
        // 找出fastest_build_time你的String值
        var sql = "SELECT fastest_build_time FROM sb_user_data WHERE uuid = ?"
        getConnection().use { connection ->
            var statement = connection.prepareStatement(sql)
            statement.setString(1, p.uniqueId.toString())
            val resultSet = statement.executeQuery()
            var fastestBuildTime = ""
            if (resultSet.next()) {
                fastestBuildTime = resultSet.getString("fastest_build_time")
            }
            resultSet.close()
            statement.close()
            // 获取现在的
            val value = fastestBuildTime
            val s = YamlConfiguration()
            s.loadFromString(value)
            val old = s.getInt(mapString)
            if (old > newValue) {
                s.set(mapString, newValue)
                p.sendMessage("§a你打破了${mapString}建造自己的纪录！")
                sql = "UPDATE sb_user_data SET fastest_build_time = ? WHERE uuid = ?"
                statement = connection.prepareStatement(sql)
                statement.setString(1, s.saveToString())
                statement.setString(2, p.uniqueId.toString())
                statement.execute()
                statement.close()
            } else if (old == 0) {
                s.set(mapString, newValue)
                p.sendMessage("§a你打破了${mapString}建造自己的纪录！")
                sql = "UPDATE sb_user_data SET fastest_build_time = ? WHERE uuid = ?"
                statement = connection.prepareStatement(sql)
                statement.setString(1, s.saveToString())
                statement.setString(2, p.uniqueId.toString())
                statement.execute()
                statement.close()
            }
        }
    }

    override fun getFastestBuildTime(p: Player, mapString: String): Double {
        // 如果收不到为0就是-1
        val sql = "SELECT fastest_build_time FROM sb_user_data WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var fastestBuildTime = ""
                if (resultSet.next()) {
                    fastestBuildTime = resultSet.getString("fastest_build_time")
                }
                resultSet.close()
                val s = YamlConfiguration()
                s.loadFromString(fastestBuildTime)
                s.getDouble(mapString)
            }
        }
    }


    override fun onDefault(p: Player) { // 如果数据库不存在改玩家就添加
        val stateSQL = "SELECT * FROM sb_user_data WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(stateSQL).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                if (!resultSet.next()) {
                    val sql =
                        "INSERT INTO sb_user_data (uuid, wins, eliminate, restore_build, fastest_build_time) VALUES (?, ?, ?, ?, ?)"
                    conn.prepareStatement(sql).use { statement ->
                        statement.setString(1, p.uniqueId.toString())
                        statement.setInt(2, 0)
                        statement.setInt(3, 0)
                        statement.setInt(4, 0)
                        val s = YamlConfiguration()
                        s.set("xx", 9999)
                        val txt = s.saveToString()
                        statement.setString(5, txt)
                        statement.execute()
                    }
                }
            }
        }
    }

    override fun getNowIslandTemplate(p: Player): String {
        val sql = "SELECT use_island FROM sb_island WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var useIsland = "default"
                if (resultSet.next()) {
                    useIsland = resultSet.getString("use_island") ?: "default"
                }
                useIsland
            }
        }
    }

    override fun setIslandTemplate(p: Player, useName: String) {
        val sql = "UPDATE sb_island SET use_island = ? WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, useName)
                it.setString(2, p.uniqueId.toString())
                it.execute()
            }
        }
    }

    override fun unlockIslandTemplateList(p: Player): MutableList<String> {
        val sql = "SELECT unlock_island FROM sb_island WHERE uuid = ?"
        return getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var unlockIsland = ""
                if (resultSet.next()) {
                    unlockIsland = resultSet.getString("unlock_island")
                }
                val s = YamlConfiguration()
                s.loadFromString(unlockIsland)
                val list = s.getStringList("list")
                list.toMutableList()
            }
        }
    }

    override fun unlockIslandTemplateList(p: Player, name: String) {
        val sql = "SELECT unlock_island FROM sb_island WHERE uuid = ?"
        getConnection().use { conn ->
            conn.prepareStatement(sql).use {
                it.setString(1, p.uniqueId.toString())
                val resultSet = it.executeQuery()
                var unlockIsland = ""
                if (resultSet.next()) {
                    unlockIsland = resultSet.getString("unlock_island")
                }
                val s = YamlConfiguration()
                s.loadFromString(unlockIsland)
                val list = s.getStringList("list")
                list.add(name)
                s.set("list", list)
                val txt = s.saveToString()
                val sql2 = "UPDATE sb_island SET unlock_island = ? WHERE uuid = ?"
                conn.prepareStatement(sql2).use { statement2 ->
                    statement2.setString(1, txt)
                    statement2.setString(2, p.uniqueId.toString())
                    statement2.execute()
                }
            }
        }
    }


}
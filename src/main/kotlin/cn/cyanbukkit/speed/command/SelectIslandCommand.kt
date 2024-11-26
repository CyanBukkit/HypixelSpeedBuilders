package cn.cyanbukkit.speed.command

import cn.cyanbukkit.speed.SpeedBuildReloaded
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.cc
import cn.cyanbukkit.speed.SpeedBuildReloaded.Companion.register
import cn.cyanbukkit.speed.command.CurrencyType.*
import cn.cyanbukkit.speed.game.GameVMData
import net.milkbowl.vault.economy.Economy
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.RegisteredServiceProvider
import java.io.File

lateinit var menuFile: File
lateinit var menu: YamlConfiguration

lateinit var currencyType: CurrencyType

enum class CurrencyType {
    VAULT,
    `PLAYER-POINTS`,
    NULL
}

object SelectIslandCommand : Command(
    "selectisland", "SpeedBuildReloaded selectisland",
    "/selectisland", listOf("select")
) {

    init {
        // 加载islandMenu配置文件
        menuFile = SpeedBuildReloaded.instance.dataFolder.resolve("islandMenu.yml")
        if (!menuFile.exists()) {
            SpeedBuildReloaded.instance.saveResource("islandMenu.yml", false)
        }
        menu = YamlConfiguration.loadConfiguration(menuFile)
        // 挂钩货币
        when (menu.getString("currency")) {
            "wco" -> {
                val rsp: RegisteredServiceProvider<Economy>? =
                    Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)
                if (rsp?.provider != null) {
                    currencyType = VAULT
                } else {
                    SpeedBuildReloaded.instance.logger.warning("Vault未找到")
                    currencyType = NULL
                }
            }

            "points" -> {
                val points = Bukkit.getPluginManager().getPlugin("PlayerPoints")
                if (points != null) {
                    currencyType = `PLAYER-POINTS`
                } else {
                    SpeedBuildReloaded.instance.logger.warning("PlayerPoints未找到")
                    currencyType = NULL
                }
            }

            else -> {
                SpeedBuildReloaded.instance.logger.warning("未知的货币类型")
                currencyType = NULL
            }
        }
        // 录入价格与按钮相关的东西
        val c = menu.getConfigurationSection("menu").getKeys(false)
        for (i in c) {
            val name = menu.getString("menu.$i.name")
            val template = menu.getString("menu.$i.template")
            val price = menu.getInt("menu.$i.price")
            val material = Material.getMaterial(menu.getString("menu.$i.material")!!)
            list.add(IslandMenuValue(name, template, price, material))
        }
        SelectIslandListener.register()
    }

    override fun execute(p0: CommandSender, p1: String?, p2: Array<out String>?): Boolean {
        if (p0 is Player) {
            p0.openSelect()
        }
        return true
    }
}

/**
 * 点击事件
 */
object SelectIslandListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "§a岛屿选择") {
            event.isCancelled = true
            val player = event.whoClicked as Player
            val slot = event.slot

            if (slot == 45) {
                // Previous page
                val currentPage = getCurrentPage(event.inventory)
                player.openSelect(currentPage - 1)
            } else if (slot == 53) {
                // Next page
                val currentPage = getCurrentPage(event.inventory)
                player.openSelect(currentPage + 1)
            } else if (slotItem.containsKey(slot)) {
                val template = slotItem[slot]!!
                val unlockedTemplates = GameVMData.storage.unlockIslandTemplateList(player)
                if (unlockedTemplates.contains(template.template)) {
                    // Already unlocked, set as current template
                    GameVMData.storage.setIslandTemplate(player, template.template)
                    player.sendMessage("§a已选择岛屿模板: ${template.template}")
                } else {
                    // Not unlocked, attempt to unlock
                    val canBuy = player.getCanBuy(template.price)
                    if (canBuy != null) {
                        if (canBuy) {
                            player.takeMoney(template.price)
                            GameVMData.storage.unlockIslandTemplate(player, template.template)
                            player.sendMessage("§a已解锁并选择岛屿模板: ${template.template}")
                        } else {
                            player.sendMessage("§c余额不足")
                        }
                    } else {
                        player.sendMessage("§c服务器没找着货币请联系管理员")
                    }
                }
            }
        }
    }
}

fun getCurrentPage(inventory: Inventory): Int {
    val title = inventory.title
    val page = title.substringAfter("§a岛屿选择 - 第")
        .substringBefore("页").toIntOrNull()
    return page?.minus(1) ?: 0
}

/**
 * 按钮
 */
data class IslandTemplate(val template: String, val price: Int)

val slotItem = mutableMapOf<Int, IslandTemplate>() // 槽位对应的template的名字与价格

data class IslandMenuValue(val name: String, val template: String, val price: Int, val material: Material)

val list = mutableListOf<IslandMenuValue>()

fun Player.openSelect(page: Int = 0) {
    val inv = Bukkit.createInventory(null, 6 * 9, "§a岛屿选择")
    // Calculate the start and end index for the current page
    val startIndex = page * canPutSlot.size
    val endIndex = (startIndex + canPutSlot.size).coerceAtMost(list.size)

    // Add items to the inventory
    for (i in startIndex until endIndex) {
        val slot = canPutSlot[i - startIndex]
        val item = ItemStack(list[i].material)
        val meta: ItemMeta = item.itemMeta!!
        meta.displayName = list[i].name.cc(this)
        item.itemMeta = meta
        inv.setItem(slot, item)
        slotItem[slot] = IslandTemplate(list[i].template, list[i].price)
    }

    // Add navigation items
    if (page > 0) {
        val prevPageItem = ItemStack(Material.ARROW)
        val prevMeta: ItemMeta = prevPageItem.itemMeta!!
        prevMeta.displayName = "§a上一页"
        prevPageItem.itemMeta = prevMeta
        inv.setItem(45, prevPageItem)
    }

    if (endIndex < list.size) {
        val nextPageItem = ItemStack(Material.ARROW)
        val nextMeta: ItemMeta = nextPageItem.itemMeta!!
        nextMeta.displayName = "§a下一页"
        nextPageItem.itemMeta = nextMeta
        inv.setItem(53, nextPageItem)
    }
    this.openInventory(inv)
}

val canPutSlot = mutableListOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)

fun Player.getCanBuy(price: Int): Boolean? {
    return when (currencyType) {
        VAULT -> {
            val rsp: RegisteredServiceProvider<Economy> =
                Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)
            rsp.provider.has(this, price.toDouble())
        }

        `PLAYER-POINTS` -> {
            val api = PlayerPoints.getInstance().api
            api.look(this.uniqueId) >= price
        }

        else -> {
            null
        }
    }
}

fun Player.takeMoney(price: Int) {
    when (currencyType) {
        VAULT -> {
            // 检测vault
            val rsp: RegisteredServiceProvider<Economy>? =
                Bukkit.getServer().servicesManager.getRegistration(Economy::class.java)
            if (rsp?.provider != null) {
                val economy = rsp.provider
                if (economy.has(this, price.toDouble())) {
                    economy.withdrawPlayer(this, price.toDouble())
                }
            }
        }

        `PLAYER-POINTS` -> {
            // 检测economy
            val points = Bukkit.getPluginManager().getPlugin("PlayerPoints")
            if (points != null) {
                val api = PlayerPoints.getInstance().api
                if (api.look(this.uniqueId) >= price) {
                    api.take(this.uniqueId, price)
                }
            }
        }

        NULL -> {
            this.sendMessage("§c服务器没找着货币请联系管理员")
        }
    }
}
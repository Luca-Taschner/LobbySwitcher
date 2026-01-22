package gg.ninjagaming.lobbyswitcher.listener

import de.cyne.lobbyswitcher.LobbySwitcher
import gg.ninjagaming.lobbyswitcher.misc.ItemBuilder
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.getPlayer()

        val hotBarItemMaterialString = LobbySwitcher.cfg.getString("hotbarItem.material")
        val horBarItemDisplayName = LobbySwitcher.cfg.getString("hotbarItem.displayname")

        if (hotBarItemMaterialString == null || horBarItemDisplayName == null)
            return

        val hotBarItemMaterial = Material.getMaterial(hotBarItemMaterialString) ?:
            return

        val item = ItemBuilder(
            hotBarItemMaterial,
            LobbySwitcher.cfg.getInt("hotbarItem.subid").toShort().toInt())
            .setDisplayName(
                ChatColor.translateAlternateColorCodes('&', horBarItemDisplayName))
            .setLore(LobbySwitcher.cfg.getStringList("hotbarItem.lore"))

        p.inventory.setItem(LobbySwitcher.cfg.getInt("hotbarItem.slot"), item)

        if (LobbySwitcher.currentServer == null) LobbySwitcher.getInstance().getServer(p)

        if (LobbySwitcher.updateAvailable && p.hasPermission("lobbyswitcher.admin")) {
            val message = TextComponent("§8┃ §bLobbySwitcher §8┃ §7Download now §8▶ ")
            val extra = TextComponent("§8*§aclick§8*")

            extra.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder("§8» §7Redirect to §bhttps://spigotmc.org/").create()
            )
            extra.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://spigotmc.org/resources/65769/")

            message.addExtra(extra)


            p.sendMessage("")
            p.sendMessage("§8┃ §bLobbySwitcher §8┃ §7A §anew update §7for §bLobbySwitcher §7was found§8.")
            p.spigot().sendMessage(message)
            p.sendMessage("")
        }
    }
}

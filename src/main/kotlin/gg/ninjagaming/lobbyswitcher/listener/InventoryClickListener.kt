package gg.ninjagaming.lobbyswitcher.listener

import gg.ninjagaming.lobbyswitcher.LobbySwitcher
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.serverProvider
import gg.ninjagaming.lobbyswitcher.misc.PluginMessageHelper

class InventoryClickListener : Listener {
    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val p = e.whoClicked as Player
        if (e.currentItem == null) return

        if (e.view.title != ChatColor.translateAlternateColorCodes('&', LobbySwitcher.cfg.getString("inventory.title")!!))
            return

        e.isCancelled = true

        if (e.currentItem!!.type != Material.getMaterial(LobbySwitcher.cfg.getString("layouts.online.material")!!))
            return

        val serverInfoMap = serverProvider.getServers()
        for (servers in serverInfoMap.values) {
            if (e.slot != servers.slot)
                continue

            if (!servers.isOnline) {
                p.closeInventory()
                p.sendMessage(
                    LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_offline").replace("%server%", servers.displayName!!))
                continue
            }


            if (servers.serverName == LobbySwitcher.currentServer) {
                p.closeInventory()
                p.sendMessage(
                    LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_already_connected")
                        .replace("%server%", servers.displayName!!))
                continue
            }

            p.closeInventory()
            p.sendMessage(
                LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_connect")
                    .replace("%server%", servers.displayName!!))

            PluginMessageHelper.sendPlayerToServer(p, servers.serverName)


        }
    }
}

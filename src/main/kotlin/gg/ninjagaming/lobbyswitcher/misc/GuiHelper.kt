package gg.ninjagaming.lobbyswitcher.misc

import gg.ninjagaming.lobbyswitcher.LobbySwitcher
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.cfg
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.currentServer
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.serverProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object GuiHelper {
    fun openGui(player: Player) {
        val inventory: Inventory = Bukkit.createInventory(
            null, cfg.getInt("inventory.rows") * 9,
            ChatColor.translateAlternateColorCodes(
                '&',
                cfg.getString("inventory.title")!!
            )
        )

        if (currentServer == null) LobbySwitcher.getServer(player)

        val serverInfoMap = serverProvider.getServers()
        for (servers in serverInfoMap.values) {
            if (!servers.isOnline) {
                //Offline Server
                val offlineItem = ServerItemBuilder.buildOfflineServerItem(servers)

                inventory.setItem(servers.slot, offlineItem)
                continue

            }
            // Current Server
            if (servers.serverName == currentServer) {
                val current = ServerItemBuilder.buildCurrentServerItem(servers)
                inventory.setItem(servers.slot, current)
                continue
            }

            //Online Server
            val online = ServerItemBuilder.buildOnlineServerItem(servers)
            inventory.setItem(servers.slot, online)
            // < ONLINE

        }

        player.openInventory(inventory)
    }
}
package gg.ninjagaming.lobbyswitcher.runnables

import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.cfg
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.currentServer
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.reloading
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.servers
import gg.ninjagaming.lobbyswitcher.misc.ServerItemBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor

object ServerRefreshRunnable {
    fun getRunnable(): Runnable{
        return Runnable {
            if (reloading)
                return@Runnable
            for (servers in servers.values) {
                val ping = servers.serverPing ?: continue

                val response = ping.fetchData()

                servers.isOnline = response.version != null
                servers.MOTD = response.description
                servers.playerCount = response.players
                servers.maxPlayers = response.maxPlayers
            }

            for (players in Bukkit.getOnlinePlayers()) {
                if (players.openInventory.title != ChatColor.translateAlternateColorCodes('&',
                        cfg.getString("inventory.title")!!))
                    continue

                players.openInventory.topInventory.clear()
                for (servers in servers.values) {
                    if (!servers.isOnline){
                        val offlineItem = ServerItemBuilder.buildOfflineServerItem(servers)
                        players.openInventory.topInventory.setItem(servers.slot, offlineItem)
                        continue
                    }

                    if (servers.serverName == currentServer) {
                        val current = ServerItemBuilder.buildCurrentServerItem(servers)
                        players.openInventory.topInventory.setItem(servers.slot, current)
                    }

                    val online = ServerItemBuilder.buildOnlineServerItem(servers)
                    players.openInventory.topInventory.setItem(servers.slot, online)
                }
            }
            return@Runnable
        }
    }
}
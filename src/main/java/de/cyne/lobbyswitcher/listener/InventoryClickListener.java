package de.cyne.lobbyswitcher.listener;

import de.cyne.lobbyswitcher.LobbySwitcher;
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;

        if (e.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', LobbySwitcher.cfg.getString("inventory.title")))) {
            e.setCancelled(true);
            if (e.getCurrentItem().getType().equals(Material.getMaterial(LobbySwitcher.cfg.getString("layouts.online.material")))) {
                for (ServerInfo servers : LobbySwitcher.servers.values()) {
                    if (e.getSlot() == servers.getSlot()) {
                        if (servers.isOnline()) {
                            if (servers.getServerName().equals(LobbySwitcher.currentServer)) {
                                p.closeInventory();
                                p.sendMessage(LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_already_connected").replace("%server%", servers.getDisplayName()));
                            } else {
                                p.closeInventory();
                                p.sendMessage(LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_connect").replace("%server%", servers.getDisplayName()));
                                LobbySwitcher.getInstance().sendToServer(p, servers.getServerName());
                            }
                        } else {
                            p.closeInventory();
                            p.sendMessage(LobbySwitcher.getString("messages.prefix") + LobbySwitcher.getString("messages.server_offline").replace("%server%", servers.getDisplayName()));
                        }

                    }
                }
            }
        }

    }
}

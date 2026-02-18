package gg.ninjagaming.lobbyswitcher.misc

import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.cfg
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.getString
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo
import org.bukkit.ChatColor
import org.bukkit.Material

object ServerItemBuilder {
    private val invalidEntryItemBuilder = ItemBuilder(Material.BARRIER).setDisplayName("Invalid Item Config")
        .setLore(mutableListOf("Please report this issue to an server Admin","If you are an server Admin please check the config file for errors"))

    fun buildOnlineServerItem(servers: ServerInfo): ItemBuilder {
        val displayNameString = cfg.getString("layouts.online.displayname") ?: return invalidEntryItemBuilder

        val displayName = getString("layouts.online.displayname")
            .replace("%server%", displayNameString)

        val lore = ArrayList<String>()
        for (string in cfg.getStringList("layouts.online.lore")) {
            lore.add(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    string.replace("%players%", servers.playerCount.toString())
                        .replace("%max_players%", servers.maxPlayers.toString())
                        .replace("%motd%", servers.MOTD)
                )
            )
        }

        val materialString = cfg.getString("layouts.online.material") ?: return invalidEntryItemBuilder

        val material = Material.getMaterial(materialString) ?: return invalidEntryItemBuilder

        val online: ItemBuilder = ItemBuilder(
            material
        )
            .setDisplayName(displayName).setLore(lore)
        if (cfg.getBoolean("layouts.online.glow")) online.addGlowEffect()

        return online
    }

    fun buildOfflineServerItem(servers: ServerInfo):ItemBuilder {
        val displayNameString = cfg.getString("layouts.offline.displayname") ?: return invalidEntryItemBuilder

        val displayName = getString("layouts.offline.displayname").replace("%server%", displayNameString)

        val materialString = cfg.getString("layouts.offline.material") ?: return invalidEntryItemBuilder
        val material = Material.getMaterial(materialString) ?: return invalidEntryItemBuilder

        val offline: ItemBuilder = ItemBuilder(
            material
        )
            .setDisplayName(displayName)
            .setLore(cfg.getStringList("layouts.offline.lore"))
        if (cfg.getBoolean("layouts.offline.glow")) offline.addGlowEffect()
        return offline
    }

    fun buildCurrentServerItem(servers: ServerInfo): ItemBuilder {
        val displayNameString = cfg.getString("layouts.current.displayname") ?: return ItemBuilder(Material.BARRIER)
        val displayName = getString("layouts.current.displayname")
            .replace("%server%", displayNameString)

        val lore = ArrayList<String>()
        for (string in cfg.getStringList("layouts.current.lore")) {
            lore.add(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    string.replace("%players%", servers.playerCount.toString())
                        .replace("%max_players%", servers.maxPlayers.toString())
                        .replace("%motd%", servers.MOTD)
                )
            )
        }

        val materialString = cfg.getString("layouts.current.material") ?: return ItemBuilder(Material.BARRIER)
        val material = Material.getMaterial(materialString) ?: return ItemBuilder(Material.BARRIER)

        val current: ItemBuilder = ItemBuilder(
            material
        )
            .setDisplayName(displayName).setLore(lore)
        if (cfg.getBoolean("layouts.current.glow")) current.addGlowEffect()

        return current
    }
}
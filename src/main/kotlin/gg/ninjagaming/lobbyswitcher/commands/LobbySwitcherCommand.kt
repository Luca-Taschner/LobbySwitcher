package gg.ninjagaming.lobbyswitcher.commands

import gg.ninjagaming.lobbyswitcher.LobbySwitcher
import gg.ninjagaming.lobbyswitcher.misc.GuiHelper
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.entity.Player
import java.io.IOException

class LobbySwitcherCommand : CommandExecutor {
    private val prefix = "§8┃ §bLobbySwitcher §8┃ "

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String?>): Boolean {
        if (!sender.hasPermission("lobbyswitcher.admin")) {
            sender.sendMessage(
                this.prefix + LobbySwitcher.getString("messages.no_permission"))
            return true
        }

        if (args.size == 1) {
            when {
                args[0].equals("gui", ignoreCase = true) -> {
                    if (sender !is Player) {
                        sender.sendMessage("§cThis command is available for players only.")
                        return true
                    }
                    GuiHelper.openGui(sender)
                    return true
                }

                args[0].equals("addserver", ignoreCase = true) -> {
                    sender.sendMessage(
                        this.prefix
                                + "§cUsage§8: /§clobbyswitcher addserver §8<§chost§8> <§cport§8> <§cbungeecord servername§8> <§cslot§8> §8<§cdisplayname§8>")
                    return true
                }
                args[0].equals("reload", ignoreCase = true) or args[0].equals("rl", ignoreCase = true) -> {
                    val start = System.currentTimeMillis()
                    if (LobbySwitcher.reloading) {
                        sender.sendMessage(this.prefix + "§cLobbySwitcher is already reloading.")
                        return true
                    }

                    LobbySwitcher.reloading = true
                    sender.sendMessage("")
                    sender.sendMessage(this.prefix + "§cReloading§8..")

                    try {
                        LobbySwitcher.cfg.load(LobbySwitcher.configFile)

                        LobbySwitcher.servers.clear()
                        for (server in LobbySwitcher.cfg.getConfigurationSection("servers")!!.getKeys(false)) {
                            val host = LobbySwitcher.cfg.getString("servers.$server.host")
                            val port = LobbySwitcher.cfg.getInt("servers.$server.port")
                            val displayName = LobbySwitcher.cfg.getString("servers.$server.displayname")
                            val slot = LobbySwitcher.cfg.getInt("servers.$server.slot")

                            LobbySwitcher.servers[server] = ServerInfo(server, host!!, port, displayName, slot)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: InvalidConfigurationException) {
                        e.printStackTrace()
                    }

                    if (sender is Player) {
                        if (LobbySwitcher.currentServer == null) LobbySwitcher.getServer(sender)
                        //sender.sendMessage("ServerInfo: §e" + LobbySwitcher.currentServer);
                    }

                    LobbySwitcher.reloading = false
                    val duration = System.currentTimeMillis() - start
                    sender.sendMessage(
                        (this.prefix + "§aReload finished, took §e" + duration + "ms§8."))
                    sender.sendMessage("")

                    return true
                }
            }
        }
        if (args.size >= 6) {
            if (!(LobbySwitcher.isInteger(args[2]) or LobbySwitcher.isInteger(args[4]))) {
                if (!LobbySwitcher.isInteger(args[2])) sender.sendMessage(
                    this.prefix + "§cYou must enter a number at 'port'."
                )
                if (!LobbySwitcher.isInteger(args[4])) sender.sendMessage(
                    this.prefix + "§cYou must enter a number at 'slot'."
                )
                return true
            }

            val host = args[1]
            val port = args[2]!!.toInt()
            val bungeeServerName = args[3]
            val slot = args[4]!!.toInt()
            var displayName = args[5]

            if (args.size > 6) {
                for (i in 6..<args.size) {
                    displayName = displayName + " " + args[i]
                }
            }

            LobbySwitcher.cfg.set("servers.$bungeeServerName.displayname", displayName)
            LobbySwitcher.cfg.set("servers.$bungeeServerName.host", host)
            LobbySwitcher.cfg.set("servers.$bungeeServerName.port", port)
            LobbySwitcher.cfg.set("servers.$bungeeServerName.slot", slot)

            try {
                LobbySwitcher.cfg.save(LobbySwitcher.configFile)
                LobbySwitcher.cfg.load(LobbySwitcher.configFile)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InvalidConfigurationException) {
                e.printStackTrace()
            }

            sender.sendMessage(
                this.prefix + "§7The server was added §asuccessfully§8."
            )
            return true
        }
        if (args.size > 1 && args[0].equals("addserver", ignoreCase = true)) {
            sender.sendMessage(
                this.prefix
                        + "§cUsage§8: /§clobbyswitcher addserver §8<§chost§8> <§cport§8> <§cbungeecord servername§8> <§cslot§8> §8<§cdisplayname§8>"
            )
            return true
        }
        sender.sendMessage("")
        sender.sendMessage(
            ("§8┃ §b● §8┃ §bLobbySwitcher §8× §av" + LobbySwitcher.VERSION + " §7by cyne"))
        sender.sendMessage("§8┃ §b● §8┃ ")
        sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher gui §8- §7Open the LobbySwitcher-Inventory")
        sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher reload §8- §7Reload the configuration files")
        sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher addserver §8- §7Add a new server to the LobbySwitcher")
        sender.sendMessage("")
        return true
    }
}

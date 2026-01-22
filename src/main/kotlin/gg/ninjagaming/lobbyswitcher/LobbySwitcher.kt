package gg.ninjagaming.lobbyswitcher

import com.google.common.io.ByteStreams
import gg.ninjagaming.advancedlobby.misc.Updater
import gg.ninjagaming.lobbyswitcher.commands.LobbySwitcherCommand
import gg.ninjagaming.lobbyswitcher.listener.InventoryClickListener
import gg.ninjagaming.lobbyswitcher.listener.PlayerInteractListener
import gg.ninjagaming.lobbyswitcher.listener.PlayerJoinListener
import gg.ninjagaming.lobbyswitcher.misc.PluginMessageHelper.pluginMessageReceived
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo
import gg.ninjagaming.lobbyswitcher.runnables.ServerRefreshRunnable
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.File
import java.io.IOException
import java.util.logging.Logger

class LobbySwitcher : JavaPlugin(), PluginMessageListener {
    override fun onEnable() {
        instance = this

        VERSION = this.description.version

        saveDefaultConfig()
        try {

            cfg.load(configFile)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }

        this.registerCommands()
        this.registerListener()

        updater = Updater(VERSION)
        Bukkit.getScheduler()
            .scheduleSyncRepeatingTask(instance!!, { updater!!.run() }, 0L, (20 * 60 * 60 * 24).toLong())

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord")

        for (server in cfg.getConfigurationSection("servers")!!.getKeys(false)) {
            val host: String? = cfg.getString("servers.$server.host")
            val port: Int = cfg.getInt("servers.$server.port")
            val displayName: String? = cfg.getString("servers.$server.displayname")
            val slot: Int = cfg.getInt("servers.$server.slot")
            servers[server] = ServerInfo(server, host!!, port, displayName, slot)
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(instance!!, ServerRefreshRunnable.getRunnable(), 20, 20)

        Bukkit.getConsoleSender().sendMessage("     §b_    §3____")
        Bukkit.getConsoleSender().sendMessage(
            "     §b|    §3[__    §b" + instance!!.description.name + " §fv" + instance!!.description.version
        )
        Bukkit.getConsoleSender().sendMessage("     §b|___ §3___]   §7The plugin has been §aenabled§8.")
        Bukkit.getConsoleSender().sendMessage("")
    }

    private fun registerCommands() {
        this.getCommand("lobbyswitcher")?.setExecutor(LobbySwitcherCommand())
    }

    private fun registerListener() {
        Bukkit.getPluginManager().registerEvents(InventoryClickListener(), this)
        Bukkit.getPluginManager().registerEvents(PlayerInteractListener(), this)
        Bukkit.getPluginManager().registerEvents(PlayerJoinListener(), this)
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        pluginMessageReceived(channel, player, message)
    }

    companion object {
        var configFile: File = File("plugins/LobbySwitcher", "config.yml")
        var cfg: FileConfiguration = YamlConfiguration.loadConfiguration(configFile)

        var servers: HashMap<String?, ServerInfo> = HashMap()
        var currentServer: String? = null

        var updateAvailable: Boolean = false

        var reloading: Boolean = false

        lateinit var  VERSION: String

        var updater: Updater? = null
        var instance: LobbySwitcher? = null
            private set

        fun isInteger(string: String?): Boolean {
            try {
                if (string == null)
                    return false
                string.toInt()
            } catch (_: NumberFormatException) {
                return false
            }
            return true
        }

        fun getString(path: String): String {
            return ChatColor.translateAlternateColorCodes('&', cfg.getString(path)!!)
        }

        fun getServer(player: Player) {
            val out = ByteStreams.newDataOutput()
            out.writeUTF("GetServer")

            player.sendPluginMessage(LobbySwitcher(), "BungeeCord", out.toByteArray())
        }

        fun sendToServer(player: Player, serverName: String) {
            val out = ByteStreams.newDataOutput()
            out.writeUTF("Connect")
            out.writeUTF(serverName)
            player.sendPluginMessage(LobbySwitcher(), "BungeeCord", out.toByteArray())
        }

        fun getLogger(): Logger {
            return Logger.getLogger("LobbySwitcher")
        }
    }
}

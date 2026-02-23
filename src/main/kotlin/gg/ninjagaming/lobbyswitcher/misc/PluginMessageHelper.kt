package gg.ninjagaming.lobbyswitcher.misc

import com.google.common.io.ByteStreams
import gg.ninjagaming.lobbyswitcher.LobbySwitcher
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.currentServer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

object PluginMessageHelper {
    fun pluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        when {
            channel == "BungeeCord" -> {
                val `in` = ByteStreams.newDataInput(message)
                val subchannel = `in`.readUTF()
                if (subchannel == "GetServer") {
                    currentServer = `in`.readUTF()
                }
            }
            else -> {
                return
            }
        }
    }

    fun sendPlayerToServer(player: Player, serverName: String) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(serverName)

        val plugin: Plugin = LobbySwitcher.instance?: return
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }

    fun getServer(player: Player) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF("GetServer")

        val plugin: Plugin = LobbySwitcher.instance?: return
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray())
    }
}
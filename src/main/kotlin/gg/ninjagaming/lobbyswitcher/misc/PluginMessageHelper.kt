package gg.ninjagaming.lobbyswitcher.misc

import com.google.common.io.ByteStreams
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.currentServer
import org.bukkit.entity.Player

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
}
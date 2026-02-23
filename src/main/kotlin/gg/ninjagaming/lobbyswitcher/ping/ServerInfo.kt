package gg.ninjagaming.lobbyswitcher.ping

import java.net.InetSocketAddress

class ServerInfo(var serverName: String, var host: String, var port: Int, var displayName: String?, var slot: Int) {
    var serverPing: ServerPing?

    var isOnline: Boolean = false
    var motd: String = ""
    var playerCount: Int = 0
    var maxPlayers: Int = 0

    init {

        val serverPing = ServerPing()
        serverPing.setAddress(InetSocketAddress(host, port))
        this.serverPing = serverPing
    }
}

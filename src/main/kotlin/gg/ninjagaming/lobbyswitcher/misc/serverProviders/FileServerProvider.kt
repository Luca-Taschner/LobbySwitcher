package gg.ninjagaming.lobbyswitcher.misc.serverProviders

import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.cfg
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo
import kotlin.collections.set

object FileServerProvider: IServerProvider {
    private var serverMap: HashMap<String, ServerInfo> = HashMap()

    override fun initialize() {
        for (server in cfg.getConfigurationSection("servers")!!.getKeys(false)) {
            val host: String? = cfg.getString("servers.$server.host")
            val port: Int = cfg.getInt("servers.$server.port")
            val displayName: String? = cfg.getString("servers.$server.displayname")
            val slot: Int = cfg.getInt("servers.$server.slot")
            serverMap[server] = ServerInfo(server, host!!, port, displayName, slot)
        }
    }

    override fun getServers(): HashMap<String, ServerInfo> {
        return serverMap
    }

    override fun addServer(server: ServerInfo) {
        serverMap[server.serverName] = server
    }

    override fun clearServers() {
        serverMap.clear()
    }
}
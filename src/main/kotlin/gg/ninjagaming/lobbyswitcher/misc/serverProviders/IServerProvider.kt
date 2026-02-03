package gg.ninjagaming.lobbyswitcher.misc.serverProviders

import gg.ninjagaming.lobbyswitcher.ping.ServerInfo

interface IServerProvider {
    fun initialize()
    fun getServers(): HashMap<String, ServerInfo>
    fun addServer(server: ServerInfo)
    fun clearServers()
}
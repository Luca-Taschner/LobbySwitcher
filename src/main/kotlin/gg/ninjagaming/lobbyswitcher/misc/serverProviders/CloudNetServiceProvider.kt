package gg.ninjagaming.lobbyswitcher.misc.serverProviders

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.CloudServiceProvider
import eu.cloudnetservice.driver.provider.ServiceTaskProvider
import eu.cloudnetservice.driver.service.ServiceTask
import gg.ninjagaming.lobbyswitcher.LobbySwitcher
import gg.ninjagaming.lobbyswitcher.LobbySwitcher.Companion.cfg
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo


object CloudNetServiceProvider: IServerProvider {
    lateinit var taskInfoProvider: ServiceTaskProvider
    lateinit var serviceInfoProvider: CloudServiceProvider
    lateinit var LobbyTask: ServiceTask

    override fun initialize() {
        taskInfoProvider = InjectionLayer.boot().instance(ServiceTaskProvider::class.java)
        serviceInfoProvider = InjectionLayer.boot().instance(CloudServiceProvider::class.java)

        val tasks = taskInfoProvider.serviceTasks()

        val taskString = cfg.getString("server-provider.task") ?: {
            LobbySwitcher.getLogger().severe("No task name specified in config.yml, falling back to 'Lobby'.")
            "Lobby"
        }

        tasks.forEach { task ->
            if (task.name() != taskString)
                return@forEach

            LobbyTask = task
        }

        if (!this::LobbyTask.isInitialized) {
            LobbySwitcher.getLogger().severe("Could not find task with name '$taskString'.")
            return
        }

        LobbySwitcher.getLogger().info("Successfully initialized CloudNet server provider.")
    }

    override fun getServers(): HashMap<String, ServerInfo> {

        val servers: HashMap<String, ServerInfo> = HashMap()
        var slot = 1;
        val services = serviceInfoProvider.services()
        services.forEach { service ->
            if (service.serviceId().taskName() != LobbyTask.name())
                return@forEach

            servers[service.serviceId().name()] = ServerInfo(
                service.serviceId().name(),
                service.address().host,
                service.address().port,
                service.serviceId().name(),
                slot)
            slot++
        }

        return servers
    }

    override fun addServer(server: ServerInfo) {}

    override fun clearServers() {}
}
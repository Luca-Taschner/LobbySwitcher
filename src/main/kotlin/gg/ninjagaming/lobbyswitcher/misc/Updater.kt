package gg.ninjagaming.lobbyswitcher.misc

import de.cyne.lobbyswitcher.LobbySwitcher
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Updater(private val resourceId: Long) {
    private var latestVersion: String? = null
    val currentVersion: String = LobbySwitcher.getInstance().description.version
    private var updateResult: UpdateResult? = null

    enum class UpdateResult {
        UPDATE_AVAILABLE, NO_UPDATE, CONNECTION_ERROR
    }

    fun checkLatestVersion() {
        try {
            val httpConnection = URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                .openConnection() as HttpURLConnection
            this.latestVersion = BufferedReader(InputStreamReader(httpConnection.getInputStream())).readLine()
        } catch (e: IOException) {
            this.setUpdateResult(UpdateResult.CONNECTION_ERROR)
        }
    }

    fun compareVersions() {
        val currentVersionCompact = currentVersion.replace(".", "").toLong()
        val latestVersionCompact = latestVersion!!.replace(".", "").toLong()

        if (currentVersionCompact == latestVersionCompact) {
            this.setUpdateResult(UpdateResult.NO_UPDATE)
            return
        }
        this.setUpdateResult(UpdateResult.UPDATE_AVAILABLE)
        return
    }

    fun run() {
        LobbySwitcher.getInstance().getLogger().info("Searching for an update on 'spigotmc.org'..")
        checkLatestVersion()
        compareVersions()
        when (this.updateResult) {
            UpdateResult.UPDATE_AVAILABLE -> {
                LobbySwitcher.getInstance().getLogger()
                    .info("There was a new version found. It is recommended to update. (Visit spigotmc.org)")
                LobbySwitcher.updateAvailable = true
            }

            UpdateResult.NO_UPDATE -> {
                LobbySwitcher.getInstance().getLogger().info("The plugin is up to date.")
                LobbySwitcher.updateAvailable = false
            }

            UpdateResult.CONNECTION_ERROR -> {
                LobbySwitcher.getInstance().getLogger().warning("Could not connect to spigotmc.org. Retrying soon.")
                LobbySwitcher.updateAvailable = false
            }

            else -> {
                LobbySwitcher.getInstance().getLogger().warning("Could not connect to spigotmc.org. Retrying soon.")
                LobbySwitcher.updateAvailable = false
            }
        }
    }

    fun getLatestVersion(): String {
        return this.latestVersion!!
    }

    fun setUpdateResult(updateResult: UpdateResult) {
        this.updateResult = updateResult
    }
}

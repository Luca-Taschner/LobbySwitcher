package gg.ninjagaming.lobbyswitcher.listener

import de.cyne.lobbyswitcher.LobbySwitcher
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class PlayerInteractListener : Listener {
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val p = e.getPlayer()
        val item = p.itemInHand

        if (e.getAction() !in setOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK))
            return

        val hotBarItemMaterialString = LobbySwitcher.cfg.getString("hotbarItem.material") ?:
            return

        if (item.type != Material.getMaterial(hotBarItemMaterialString) ||
            item.itemMeta!!.displayName != LobbySwitcher.getString("hotbarItem.displayname"))
            return

        e.isCancelled = true
        LobbySwitcher.getInstance().openGUI(p)
        return
    }
}

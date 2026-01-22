package de.cyne.lobbyswitcher.listener;

import de.cyne.lobbyswitcher.LobbySwitcher;
import gg.ninjagaming.lobbyswitcher.misc.ItemBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        ItemBuilder item = new ItemBuilder(Material.getMaterial(LobbySwitcher.cfg.getString("hotbarItem.material")),
                (short) LobbySwitcher.cfg.getInt("hotbarItem.subid"))
                .setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        LobbySwitcher.cfg.getString("hotbarItem.displayname")))
                .setLore(LobbySwitcher.cfg.getStringList("hotbarItem.lore"));

        p.getInventory().setItem(LobbySwitcher.cfg.getInt("hotbarItem.slot"), item);

        if (LobbySwitcher.currentServer == null)
            LobbySwitcher.getInstance().getServer(p);

        //p.sendMessage("ServerInfo: §e" + LobbySwitcher.currentServer);

        if (LobbySwitcher.updateAvailable && p.hasPermission("lobbyswitcher.admin")) {
            TextComponent message = new TextComponent("§8┃ §bLobbySwitcher §8┃ §7Download now §8▶ ");
            TextComponent extra = new TextComponent("§8*§aclick§8*");

            extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8» §7Redirect to §bhttps://spigotmc.org/").create()));
            extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://spigotmc.org/resources/65769/"));

            message.addExtra(extra);


            p.sendMessage("");
            p.sendMessage("§8┃ §bLobbySwitcher §8┃ §7A §anew update §7for §bLobbySwitcher §7was found§8.");
            p.spigot().sendMessage(message);
            p.sendMessage("");
        }
    }

}

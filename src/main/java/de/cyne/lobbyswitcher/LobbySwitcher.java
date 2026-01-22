package de.cyne.lobbyswitcher;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.cyne.lobbyswitcher.commands.LobbySwitcherCommand;
import de.cyne.lobbyswitcher.listener.InventoryClickListener;
import de.cyne.lobbyswitcher.listener.PlayerInteractListener;
import de.cyne.lobbyswitcher.listener.PlayerJoinListener;
import de.cyne.lobbyswitcher.misc.ItemBuilder;
import de.cyne.lobbyswitcher.ping.ServerPing;
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo;
import de.cyne.lobbyswitcher.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class LobbySwitcher extends JavaPlugin implements PluginMessageListener {

    public static File file = new File("plugins/LobbySwitcher", "config.yml");
    public static FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

    public static HashMap<String, ServerInfo> servers = new HashMap<>();
    public static String currentServer;

    public static boolean updateAvailable = false;

    public static boolean reloading = false;

    public static Updater updater;
    private static LobbySwitcher instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        try {
            LobbySwitcher.cfg.load(LobbySwitcher.file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        this.registerCommands();
        this.registerListener();

        updater = new Updater(65769);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(LobbySwitcher.getInstance(), () -> updater.run(), 0L, 20 * 60 * 60 * 24);

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        for (String server : cfg.getConfigurationSection("servers").getKeys(false)) {
            String host = cfg.getString("servers." + server + ".host");
            int port = cfg.getInt("servers." + server + ".port");
            String displayName = cfg.getString("servers." + server + ".displayname");
            int slot = cfg.getInt("servers." + server + ".slot");
            LobbySwitcher.servers.put(server, new ServerInfo(server, host, port, displayName, slot));
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(LobbySwitcher.getInstance(), () -> {
            if (!LobbySwitcher.reloading) {
                for (ServerInfo servers : LobbySwitcher.servers.values()) {
                    ServerPing ping = servers.getServerPing();
                    ServerPing.DefaultResponse response;
                    try {
                        response = ping.fetchData();
                        servers.setOnline(true);
                        servers.setMOTD(response.description);
                        servers.setPlayerCount(response.getPlayers());
                        servers.setMaxPlayers(response.getMaxPlayers());
                    } catch (IOException ex) {
                        servers.setOnline(false);
                    }
                }

                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (players.getOpenInventory().getTitle().equals(
                            ChatColor.translateAlternateColorCodes('&', LobbySwitcher.cfg.getString("inventory.title")))) {
                        players.getOpenInventory().getTopInventory().clear();
                        for (ServerInfo servers : LobbySwitcher.servers.values()) {
                            if (servers.isOnline()) {
                                if (servers.getServerName().equals(LobbySwitcher.currentServer)) {
                                    String displayName = LobbySwitcher.cfg.getString("layouts.current.displayname")
                                            .replace("%server%", servers.getDisplayName());

                                    ArrayList<String> lore = new ArrayList<>();
                                    for (String string : LobbySwitcher.cfg.getStringList("layouts.current.lore")) {
                                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                                string.replace("%players%", String.valueOf(servers.getPlayerCount()))
                                                        .replace("%max_players%", String.valueOf(servers.getMaxPlayers()))
                                                        .replace("%motd%", servers.getMOTD())));
                                    }
                                    ItemBuilder current = new ItemBuilder(
                                            Material.getMaterial(LobbySwitcher.cfg.getString("layouts.current.material")),
                                            1, (byte) LobbySwitcher.cfg.getInt("layouts.current.subid"))
                                            .setDisplayName(
                                                    ChatColor.translateAlternateColorCodes('&', displayName))
                                            .setLore(lore);
                                    if (LobbySwitcher.cfg.getBoolean("layouts.current.glow"))
                                        current.addGlowEffect();
                                    players.getOpenInventory().getTopInventory().setItem(servers.getSlot(), current);
                                } else {
                                    String displayName = LobbySwitcher.cfg.getString("layouts.online.displayname")
                                            .replace("%server%", servers.getDisplayName());

                                    ArrayList<String> lore = new ArrayList<>();
                                    for (String string : LobbySwitcher.cfg.getStringList("layouts.online.lore")) {
                                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                                string.replace("%players%", String.valueOf(servers.getPlayerCount()))
                                                        .replace("%max_players%", String.valueOf(servers.getMaxPlayers()))
                                                        .replace("%motd%", servers.getMOTD())));
                                    }
                                    ItemBuilder online = new ItemBuilder(
                                            Material.getMaterial(LobbySwitcher.cfg.getString("layouts.online.material")), 1,
                                            (byte) LobbySwitcher.cfg.getInt("layouts.online.subid"))
                                            .setDisplayName(
                                                    ChatColor.translateAlternateColorCodes('&', displayName))
                                            .setLore(lore);
                                    if (LobbySwitcher.cfg.getBoolean("layouts.online.glow"))
                                        online.addGlowEffect();
                                    players.getOpenInventory().getTopInventory().setItem(servers.getSlot(), online);
                                }
                            } else {
                                String displayName = LobbySwitcher.cfg.getString("layouts.offline.displayname")
                                        .replace("%server%", servers.getDisplayName());
                                ItemBuilder offline = new ItemBuilder(
                                        Material.getMaterial(LobbySwitcher.cfg.getString("layouts.offline.material")), 1,
                                        (byte) LobbySwitcher.cfg.getInt("layouts.offline.subid"))
                                        .setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName))
                                        .setLore(LobbySwitcher.cfg.getStringList("layouts.offline.lore"));
                                if (LobbySwitcher.cfg.getBoolean("layouts.offline.glow"))
                                    offline.addGlowEffect();
                                players.getOpenInventory().getTopInventory().setItem(servers.getSlot(), offline);
                            }
                        }
                    }
                }
            }
        }, 20, 20);

        Bukkit.getConsoleSender().sendMessage("     §b_    §3____");
        Bukkit.getConsoleSender().sendMessage("     §b|    §3[__    §b" + getInstance().getDescription().getName() + " §fv" + getInstance().getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("     §b|___ §3___]   §7The plugin has been §aenabled§8.");
        Bukkit.getConsoleSender().sendMessage("");
    }

    private void registerCommands() {
        LobbySwitcher.getInstance().getCommand("lobbyswitcher").setExecutor(new LobbySwitcherCommand());
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), LobbySwitcher.getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), LobbySwitcher.getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), LobbySwitcher.getInstance());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServer")) {
            LobbySwitcher.currentServer = in.readUTF();
        }
    }

    public void openGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, LobbySwitcher.cfg.getInt("inventory.rows") * 9,
                ChatColor.translateAlternateColorCodes('&',
                        LobbySwitcher.cfg.getString("inventory.title")));

        if (LobbySwitcher.currentServer == null)
            LobbySwitcher.getInstance().getServer(player);

        for (ServerInfo servers : LobbySwitcher.servers.values()) {

            if (servers.isOnline()) {
                // CURRENT >
                if (servers.getServerName().equals(LobbySwitcher.currentServer)) {
                    String displayName = LobbySwitcher.getString("layouts.current.displayname")
                            .replace("%server%", servers.getDisplayName());

                    ArrayList<String> lore = new ArrayList<>();
                    for (String string : LobbySwitcher.cfg.getStringList("layouts.current.lore")) {
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                string.replace("%players%", String.valueOf(servers.getPlayerCount()))
                                        .replace("%max_players%", String.valueOf(servers.getMaxPlayers()))
                                        .replace("%motd%", servers.getMOTD())));
                    }
                    ItemBuilder current = new ItemBuilder(
                            Material.getMaterial(LobbySwitcher.cfg.getString("layouts.current.material")),
                            1, (byte) LobbySwitcher.cfg.getInt("layouts.current.subid"))
                            .setDisplayName(displayName).setLore(lore);
                    if (LobbySwitcher.cfg.getBoolean("layouts.current.glow"))
                        current.addGlowEffect();
                    inventory.setItem(servers.getSlot(), current);
                    // < CURRENT
                } else {
                    // ONLINE >
                    String displayName = LobbySwitcher.getString("layouts.online.displayname")
                            .replace("%server%", servers.getDisplayName());

                    ArrayList<String> lore = new ArrayList<>();
                    for (String string : LobbySwitcher.cfg.getStringList("layouts.online.lore")) {
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                string.replace("%players%", String.valueOf(servers.getPlayerCount()))
                                        .replace("%max_players%", String.valueOf(servers.getMaxPlayers()))
                                        .replace("%motd%", servers.getMOTD())));
                    }
                    ItemBuilder online = new ItemBuilder(
                            Material.getMaterial(LobbySwitcher.cfg.getString("layouts.online.material")), 1,
                            (byte) LobbySwitcher.cfg.getInt("layouts.online.subid"))
                            .setDisplayName(displayName).setLore(lore);
                    if (LobbySwitcher.cfg.getBoolean("layouts.online.glow"))
                        online.addGlowEffect();
                    inventory.setItem(servers.getSlot(), online);
                    // < ONLINE
                }
            } else {
                // OFFLINE >
                String displayName = LobbySwitcher.getString("layouts.offline.displayname")
                        .replace("%server%", servers.getDisplayName());
                ItemBuilder offline = new ItemBuilder(
                        Material.getMaterial(LobbySwitcher.cfg.getString("layouts.offline.material")), 1,
                        (byte) LobbySwitcher.cfg.getInt("layouts.offline.subid"))
                        .setDisplayName(displayName)
                        .setLore(LobbySwitcher.cfg.getStringList("layouts.offline.lore"));
                if (LobbySwitcher.cfg.getBoolean("layouts.offline.glow"))
                    offline.addGlowEffect();
                inventory.setItem(servers.getSlot(), offline);
                // < OFFLINE
            }

        }
        player.openInventory(inventory);
    }

    public void sendToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public void getServer(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    public static String getString(String path) {
        return ChatColor.translateAlternateColorCodes('&', LobbySwitcher.cfg.getString(path));
    }

    public static LobbySwitcher getInstance() {
        return instance;
    }

}

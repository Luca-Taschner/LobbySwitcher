package de.cyne.lobbyswitcher.commands;

import de.cyne.lobbyswitcher.LobbySwitcher;
import gg.ninjagaming.lobbyswitcher.ping.ServerInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;

public class LobbySwitcherCommand implements CommandExecutor {

    private String prefix = "§8┃ §bLobbySwitcher §8┃ ";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("lobbyswitcher.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("gui")) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage("§cThis command is available for players only.");
                        return true;
                    }
                    Player player = (Player) sender;
                    LobbySwitcher.getInstance().openGUI(player);
                    return true;
                }
                if (args[0].equalsIgnoreCase("addserver")) {
                    sender.sendMessage(this.prefix
                            + "§cUsage§8: /§clobbyswitcher addserver §8<§chost§8> <§cport§8> <§cbungeecord servername§8> <§cslot§8> §8<§cdisplayname§8>");
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload") | args[0].equalsIgnoreCase("rl")) {
                    long start = System.currentTimeMillis();
                    if (LobbySwitcher.reloading) {
                        sender.sendMessage(this.prefix + "§cLobbySwitcher is already reloading.");
                        return true;
                    }
                    LobbySwitcher.reloading = true;
                    sender.sendMessage("");
                    sender.sendMessage(this.prefix + "§cReloading§8..");

                    try {
                        LobbySwitcher.cfg.load(LobbySwitcher.file);

                        LobbySwitcher.servers.clear();
                        for (String server : LobbySwitcher.cfg.getConfigurationSection("servers").getKeys(false)) {
                            String host = LobbySwitcher.cfg.getString("servers." + server + ".host");
                            int port = LobbySwitcher.cfg.getInt("servers." + server + ".port");
                            String displayName = LobbySwitcher.cfg.getString("servers." + server + ".displayname");
                            int slot = LobbySwitcher.cfg.getInt("servers." + server + ".slot");

                            LobbySwitcher.servers.put(server, new ServerInfo(server, host, port, displayName, slot));
                        }

                    } catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }

                    if (sender instanceof Player) {
                        if (LobbySwitcher.currentServer == null)
                            LobbySwitcher.getInstance().getServer((Player) sender);
                        //sender.sendMessage("ServerInfo: §e" + LobbySwitcher.currentServer);
                    }

                    LobbySwitcher.reloading = false;
                    long duration = System.currentTimeMillis() - start;
                    sender.sendMessage(this.prefix + "§aReload finished, took §e"
                            + duration + "ms§8.");
                    sender.sendMessage("");
                    ;

                    return true;
                }
            }
            if (args.length >= 6) {
                if (!(LobbySwitcher.isInteger(args[2]) | LobbySwitcher.isInteger(args[4]))) {
                    if (!LobbySwitcher.isInteger(args[2]))
                        sender.sendMessage(
                                this.prefix + "§cYou must enter a number at 'port'.");
                    if (!LobbySwitcher.isInteger(args[4]))
                        sender.sendMessage(
                                this.prefix + "§cYou must enter a number at 'slot'.");
                    return true;
                }

                String host = args[1];
                int port = Integer.valueOf(args[2]);
                String bungeeServerName = args[3];
                int slot = Integer.valueOf(args[4]);
                String displayName = args[5];

                if (args.length > 6) {
                    for (int i = 6; i < args.length; i++) {
                        displayName = displayName + " " + args[i];
                    }
                }

                LobbySwitcher.cfg.set("servers." + bungeeServerName + ".displayname", displayName);
                LobbySwitcher.cfg.set("servers." + bungeeServerName + ".host", host);
                LobbySwitcher.cfg.set("servers." + bungeeServerName + ".port", port);
                LobbySwitcher.cfg.set("servers." + bungeeServerName + ".slot", slot);

                try {
                    LobbySwitcher.cfg.save(LobbySwitcher.file);
                    LobbySwitcher.cfg.load(LobbySwitcher.file);
                } catch (IOException | InvalidConfigurationException e) {
                    e.printStackTrace();
                }

                sender.sendMessage(
                        this.prefix + "§7The server was added §asuccessfully§8.");
                return true;
            }
            if (args.length > 1 && args[0].equalsIgnoreCase("addserver")) {
                sender.sendMessage(this.prefix
                        + "§cUsage§8: /§clobbyswitcher addserver §8<§chost§8> <§cport§8> <§cbungeecord servername§8> <§cslot§8> §8<§cdisplayname§8>");
                return true;
            }
            sender.sendMessage("");
            sender.sendMessage("§8┃ §b● §8┃ §bLobbySwitcher §8× §av"
                    + LobbySwitcher.getInstance().getDescription().getVersion() + " §7by cyne");
            sender.sendMessage("§8┃ §b● §8┃ ");
            sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher gui §8- §7Open the LobbySwitcher-Inventory");
            sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher reload §8- §7Reload the configuration files");
            sender.sendMessage("§8┃ §b● §8┃ §8/§flobbyswitcher addserver §8- §7Add a new server to the LobbySwitcher");
            sender.sendMessage("");
            return true;
        }
        sender.sendMessage(
                this.prefix + LobbySwitcher.getString("messages.no_permission"));
        return true;
    }

}

package me.jackz.simplebungee.lib;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.List;
import java.util.Map;

public class ServerShortcut {
    public static void setupShortcuts(SimpleBungee plugin, Configuration servers) {
        Map<String,ServerInfo> SERVERS = plugin.getProxy().getServers();
        if(servers != null) {
            for (String key : servers.getKeys()) {
                if(key.equals("examplebungeeserver")) continue; //ignore 'example'
                boolean use_perms = servers.getBoolean(Util.getSectioned(key,"permissions"),false);
                List<String> aliases_list = servers.getStringList(Util.getSectioned(key,"aliases"));

                if(aliases_list.size() > 0) {
                    ServerInfo server = null;
                    for (ServerInfo value : SERVERS.values()) {
                        if(value.getName().equalsIgnoreCase(key)) {
                            server = value;
                            break;
                        }
                    }
                    if(server == null) {
                        plugin.getLogger().warning("Server in config section 'server_shortcuts' does not exist: " + key);
                        continue;
                    }
                    String first = aliases_list.get(0);
                    String[] aliases = (aliases_list.size() >= 2) ? aliases_list.subList(1,aliases_list.size()).toArray(new String[0]) : new String[0];
                    String permissions = (use_perms) ? Util.getSectioned("simplebungee","server",key) : null;

                    //plugin.getLogger().info("Registering aliases[" + aliases_list.size() + "] for '" + key + "'. first: " + first + " | rest: " + String.join(",",aliases));
                    ServerInfo finalServer = server;
                    plugin.getProxy().getPluginManager().registerCommand(plugin, new Command(first,permissions,aliases)  {
                        @Override
                        public void execute(CommandSender sender, String[] args) {
                            if(sender instanceof ProxiedPlayer) {
                                ProxiedPlayer player = (ProxiedPlayer) sender;
                                player.connect(finalServer);
                            }else{
                                sender.sendMessage(new TextComponent("§cYou must be a player to use this"));
                            }
                        }
                    });
                }else{
                    plugin.getLogger().warning("Server shortcut '" + key + "' has no aliases defined");
                }

            }
        }
    }
}

package me.jackz.simplebungee.lib;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.List;

public class ServerShortcut {
    public static void setupShortcuts(SimpleBungee plugin, Configuration servers) {
        if(servers != null) {
            for (String key : servers.getKeys()) {
                if(key.equals("example")) continue; //ignore 'example'
                boolean use_perms = servers.getBoolean(Util.getSectioned("server_shortcuts",key,"permissions"),false);
                List<String> aliases_list = servers.getStringList(Util.getSectioned("server_shortcuts",key,"aliases"));
                if(aliases_list.size() > 0) {
                    ServerInfo server = plugin.getProxy().getServerInfo(key);
                    if(server == null) {
                        plugin.getLogger().warning("Server in config section 'server_shortcuts' does not exist: " + key);
                        continue;
                    }
                    String first = aliases_list.get(0);
                    String[] aliases = aliases_list.subList(1,aliases_list.size()-1).toArray(new String[0]);

                    String permissions = (use_perms) ? Util.getSectioned("simplebungee","server",key) : null;

                    plugin.getProxy().getPluginManager().registerCommand(plugin, new Command(key,permissions,aliases)  {
                        @Override
                        public void execute(CommandSender sender, String[] args) {
                            if(sender instanceof ProxiedPlayer) {
                                ProxiedPlayer player = (ProxiedPlayer) sender;
                                player.connect(server);
                            }else{
                                sender.sendMessage(new TextComponent("§cYou must be a player to use this"));
                            }
                        }
                    });
                }

            }
        }
    }
}

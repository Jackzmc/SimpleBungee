package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.DataStore;
import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.OfflinePlayerStore;
import me.jackz.simplebungee.lib.PlayerLoader;
import me.jackz.simplebungee.lib.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;

public class Lookup extends Command {
    private SimpleBungee plugin;
    private PlayerLoader playerLoader;
    public Lookup(SimpleBungee plugin) {
        super("lookup");
        this.plugin = plugin;
        this.playerLoader =  plugin.getPlayerLoader();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length > 0) {
            boolean show_advanced = sender.hasPermission("simplebungee.lookup.advanced");
            Collection<ProxiedPlayer> players = plugin.getProxy().matchPlayer(args[0]);
            if(players.size() > 0) {
                ProxiedPlayer player = players.iterator().next();

                if(DataStore.CURRENT_PLAYTIME_STORE.containsKey(player.getUniqueId())) {
                    long playtime = DataStore.CURRENT_PLAYTIME_STORE.get(player.getUniqueId());
                    long now = System.currentTimeMillis() / 1000L;
                    TextComponent base = new TextComponent("Lookup for " + player.getName() );

                    base.setColor(ChatColor.GOLD);
                    base.addExtra("\n§7UUID: §e" + player.getUniqueId());
                    base.addExtra("\n§7Server: §e" + player.getServer().getInfo().getName());
                    base.addExtra("\n§7Online for §e" + Util.getTimeBetween(playtime,now));
                    base.addExtra("\n§7Ping: §e" + player.getPing() + " ms");
                    if(show_advanced) {
                        base.addExtra("\n§7IP: §e" + player.getAddress().getHostString());
                    }
                    sender.sendMessage(base);
                }else{
                    sender.sendMessage(new TextComponent("§cCould not get information about player"));
                }
            }else{
                OfflinePlayerStore player = playerLoader.getOfflinePlayer(args[0]);
                if(player == null) {
                    sender.sendMessage(new TextComponent("§cCould not find a player with that name."));
                }else{
                    TextComponent base = new TextComponent("Lookup for " + player.getLastUsername());
                    base.setColor(ChatColor.GOLD);
                    base.addExtra("\n§7UUID: §e" + player.getUUID());
                    base.addExtra("\n§7Last Server: §e" + player.getLastServer());
                    base.addExtra("\n§7Last Online: §e" + player.getLastOnline());
                    if(show_advanced) {
                        base.addExtra("\n§7IP: §e" + player.getIP() );
                    }
                    sender.sendMessage(base);
                }
            }
        }else{
            sender.sendMessage(new TextComponent("§cPlease enter a user to lookup (Usage: /lookup <username>)"));
        }
    }
}

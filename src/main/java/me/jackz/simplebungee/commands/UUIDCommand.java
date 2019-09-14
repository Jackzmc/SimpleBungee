package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;

public class UUIDCommand extends Command {
    private SimpleBungee plugin;
    public UUIDCommand() {
        super("uuid");
    }
    public UUIDCommand(SimpleBungee plugin)  {
        super("uuid");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length > 0) {
            Collection<ProxiedPlayer> players = plugin.getProxy().matchPlayer(args[0]);
            if(players.size() > 0) {
                ProxiedPlayer player = players.iterator().next();
                sender.sendMessage(new TextComponent("§7" + player.getName() + "'s UUID: §e" + player.getUniqueId()));
            }else{
                sender.sendMessage(new TextComponent("§cCould not find a player online with that name."));
            }
        }else{
            if(sender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) sender;
                sender.sendMessage(new TextComponent("§7Your UUID: §e" + player.getUniqueId()));
            }else{
                sender.sendMessage(new TextComponent("§cPlease enter a user to get the UUID of. Usage: /uuid <player>"));
            }
        }
    }
}

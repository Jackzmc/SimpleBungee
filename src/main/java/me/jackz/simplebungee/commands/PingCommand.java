package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;

public class PingCommand extends Command {
    private SimpleBungee plugin;

    public PingCommand()  {
        super("ping");
    }
    public PingCommand(SimpleBungee plugin)  {
        super("ping");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
            if(args.length == 0) {
                int ping = proxiedPlayer.getPing();
                proxiedPlayer.sendMessage(new TextComponent("§7Your Ping: §e" + ping + " ms"));
            }else{
                Collection<ProxiedPlayer> players = plugin.getProxy().matchPlayer(args[0]);
                if(players.size() > 0) {
                    ProxiedPlayer player = players.iterator().next();
                    int ping = player.getPing();
                    proxiedPlayer.sendMessage(new TextComponent("§7" + player.getName() + "'s ping: §e" + ping + " ms"));
                }else{
                    proxiedPlayer.sendMessage(new TextComponent("§cCould not find a player online with that name."));
                }
            }
        }else{
            sender.sendMessage(new TextComponent("This command must be run from a player"));
        }
    }
}

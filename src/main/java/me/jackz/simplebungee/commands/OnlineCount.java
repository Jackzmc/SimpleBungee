package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;
import java.util.stream.Collectors;

public class OnlineCount extends Command {
    private SimpleBungee plugin;

    public OnlineCount()  {
        super("online","simplebungee.command.online","simplebungee:online");
    }
    public OnlineCount(SimpleBungee plugin)  {
        super("online");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        Collection<ProxiedPlayer> playerCollection = plugin.getProxy().getPlayers();
        if(playerCollection.size() > 0) {
            String players =  playerCollection.stream().map(ProxiedPlayer::getName).collect(Collectors.joining(","));
            TextComponent msg = new TextComponent("Players Online: " + players);
            sender.sendMessage(msg);
        }else{
            sender.sendMessage(new TextComponent(ChatColor.RED + "There are no players online."));
        }

    }
}

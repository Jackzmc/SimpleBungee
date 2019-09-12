package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.stream.Collectors;

public class Servers extends Command {
    private SimpleBungee plugin;

    public Servers()  {
        super("servers");
    }
    public Servers(SimpleBungee plugin)  {
        super("servers","simplebungee.command.servers");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        TextComponent base = new TextComponent("§6Server Status: ");
        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            String name = server.getName();
            TextComponent comp_server = new TextComponent(String.format("\n%s[%s]",ChatColor.GREEN,name));
            comp_server.setColor(ChatColor.GREEN);
            if(server.isRestricted()) {
                comp_server.addExtra(" [restricted]");
                comp_server.setColor(ChatColor.RED);
            }else {
                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + name);
                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Connect to this server").create());
                //server_name.setHoverEvent(he);
                comp_server.setHoverEvent(he);
                comp_server.setClickEvent(ce);
            }
            String players = server.getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.joining(", "));
            String finalString = String.format(" %s(%d)%s: %s",ChatColor.YELLOW,server.getPlayers().size(),ChatColor.WHITE,players);
            TextComponent comp_count = new TextComponent(finalString);
            base.addExtra(comp_server);
            base.addExtra(comp_count);
        }
        sender.sendMessage(base);
    }
}

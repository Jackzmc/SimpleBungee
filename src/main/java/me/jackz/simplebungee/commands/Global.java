package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Global extends Command implements Listener {
    private Map<UUID,Boolean> GLOBAL_CHAT_TOGGLED = new HashMap<>();
    private String format = "&9GLOBAL %servername%> &e%displayname%:&r";
    private SimpleBungee plugin;

    public Global(SimpleBungee plugin) {
        super("global","simplebungee.command.global","g");
        this.plugin = plugin;
    }

    /* format: '&9GLOBAL %servername%> &e%fullname%: %message%' */


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(args.length > 0) {
                BaseComponent[] tc = new ComponentBuilder(Util.formatPlaceholders(format, player))
                        .append(" " + String.join(" ",args)).create();
                plugin.getProxy().broadcast(tc);
            }else{
                boolean toggled = GLOBAL_CHAT_TOGGLED.getOrDefault(player.getUniqueId(),false);
                if(toggled) {
                    GLOBAL_CHAT_TOGGLED.put(player.getUniqueId(),false);
                    sender.sendMessage(new TextComponent("§aNow talking in local chat"));
                }else{
                    GLOBAL_CHAT_TOGGLED.put(player.getUniqueId(),true);
                    sender.sendMessage(new TextComponent("§aNow talking in global chat"));
                }
            }
        }else{
            sender.sendMessage(new TextComponent("§cYou must be a player to use this."));
        }

    }

    @EventHandler(priority =  EventPriority.HIGH)
    public void onChat(ChatEvent e) {
        if(e.getMessage().startsWith("/")) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();
        boolean is_global = isGlobalEnabled(player.getUniqueId());
        if(is_global && !e.isCancelled())    {
            BaseComponent[] tc = new ComponentBuilder(Util.formatPlaceholders(format,player))
                    .append(" " + String.join(" ",message)).create();
            plugin.getProxy().broadcast(tc);
            e.setCancelled(true);
        }
    }

    public boolean isGlobalEnabled(UUID uuid) {
        return GLOBAL_CHAT_TOGGLED.getOrDefault(uuid,false);
    }
}

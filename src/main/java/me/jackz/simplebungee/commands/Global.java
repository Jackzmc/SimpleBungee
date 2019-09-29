package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.LanguageManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
    private final Map<UUID,Boolean> GLOBAL_CHAT_TOGGLED = new HashMap<>();
    private final SimpleBungee plugin;
    private final LanguageManager lm;

    public Global(SimpleBungee plugin) {
        super("global","simplebungee.command.global","g","simplebungee:global");
        this.plugin = plugin;
        lm = SimpleBungee.getLanguageManager();
    }

    /* format: '&9GLOBAL %server_name%> &e%fullname%: %message%' */


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(args.length > 0) {
                BaseComponent[] tc = new ComponentBuilder(lm.getString("formats.GLOBAL",player))
                        .append(" " + String.join(" ",args)).create();
                plugin.getProxy().broadcast(tc);
            }else{
                boolean toggled = GLOBAL_CHAT_TOGGLED.getOrDefault(player.getUniqueId(),false);
                if(toggled) {
                    GLOBAL_CHAT_TOGGLED.put(player.getUniqueId(),false);
                    lm.sendMessage(sender,"global.NOW_LOCALCHAT");
                }else{
                    GLOBAL_CHAT_TOGGLED.put(player.getUniqueId(),true);
                    lm.sendMessage(sender,"global.NOW_GLOBALCHAT");
                }
            }
        }else{
            lm.sendMessage(sender,"core.PLAYER_ONLY");
        }

    }

    @EventHandler(priority =  EventPriority.HIGH)
    public void onChat(ChatEvent e) {
        if(e.getMessage().startsWith("/")) return;
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        String message = e.getMessage();
        boolean is_global = isGlobalEnabled(player.getUniqueId());
        if(is_global && !e.isCancelled())    {
            BaseComponent[] tc = new ComponentBuilder(lm.getString("formats.GLOBAL",player))
                    .append(" " + String.join(" ",message)).create();
            plugin.getProxy().broadcast(tc);
            e.setCancelled(true);
        }
    }

    private boolean isGlobalEnabled(UUID uuid) {
        return GLOBAL_CHAT_TOGGLED.getOrDefault(uuid,false);
    }
}

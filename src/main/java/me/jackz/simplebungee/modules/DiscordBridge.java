package me.jackz.simplebungee.modules;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;

public class DiscordBridge implements Listener {
    private SimpleBungee plugin;
    private String channel;
    private String bot_token;
    private String bot_id;

    private boolean link_to_bungee = false;
    private boolean link_to_discord = false;
    private boolean enabled = false;

    public DiscordBridge(SimpleBungee plugin) {
        this.plugin = plugin;
        try {
            Configuration config = plugin.getConfig();
            channel = config.getString("discord.channelID");
            bot_id = config.getString("discord.bot.id");
            bot_token = config.getString("discord.bot.token");
            link_to_bungee = config.getBoolean("discord.link.discordtobungee",false);
            link_to_discord = config.getBoolean("discord.link.bungeetodiscord",false);
            enabled = true;
        } catch (IOException e) {
            plugin.getLogger().severe("Discord Bridge module could not load the configuration. Disabled.");
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if(enabled && link_to_discord) {
            ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        }
    }

}

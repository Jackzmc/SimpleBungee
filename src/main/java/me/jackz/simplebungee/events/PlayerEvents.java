package me.jackz.simplebungee.events;

import me.jackz.simplebungee.DataStore;
import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.PlayerLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private SimpleBungee plugin;
    private PlayerLoader playerLoader;

    public PlayerEvents(SimpleBungee plugin) {
        this.plugin = plugin;
        this.playerLoader = plugin.getPlayerLoader();
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        long date = System.currentTimeMillis() / 1000L;
        DataStore.CURRENT_PLAYTIME_STORE.put(player.getUniqueId(),date);

        TextComponent join_message = new TextComponent(player.getName() + " has joined the network");
        join_message.setColor(ChatColor.YELLOW);
        plugin.getProxy().broadcast(join_message);
    }
    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        TextComponent leave_message = new TextComponent(player.getName() + " has left the network");
        leave_message.setColor(ChatColor.YELLOW);
        plugin.getProxy().broadcast(leave_message);
        DataStore.CURRENT_PLAYTIME_STORE.remove(player.getUniqueId());

        try {
            playerLoader.save(player);
        } catch (Exception ex) {
            plugin.getLogger().warning("Could not save player information for " + player.getName());
        }
    }
    private Map<UUID,ServerInfo> LAST_SERVER_MAP = new HashMap<>();


    @EventHandler(priority = EventPriority.LOW)
    public void onServerConnect(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if(player.isConnected() && player.getServer() != null && !e.isCancelled()) {
            LAST_SERVER_MAP.put(player.getUniqueId(),e.getPlayer().getServer().getInfo());
        }

    }
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();
        ServerInfo previous = LAST_SERVER_MAP.get(player.getUniqueId());
        ServerInfo current = player.getServer().getInfo();
        if(current != null) {
            TextComponent tc = new TextComponent(player.getName() + " switched servers from " + previous.getName() + " to " + current.getName());
            tc.setColor(ChatColor.YELLOW);
            plugin.getProxy().broadcast(tc);
        }
    }
}

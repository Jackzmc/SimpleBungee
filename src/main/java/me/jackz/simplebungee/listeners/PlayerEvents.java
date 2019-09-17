package me.jackz.simplebungee.listeners;

import me.jackz.simplebungee.DataStore;
import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.FriendsManager;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.managers.PlayerLoader;
import me.jackz.simplebungee.utils.Placeholder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private SimpleBungee plugin;
    private PlayerLoader playerLoader;
    private FriendsManager friendsManager;
    private LanguageManager lm;

    private Map<UUID,ServerInfo> LAST_SERVER_MAP = new HashMap<>();

    private boolean network_messages;
    private boolean switch_server_messages;
    private boolean friends_messages; //move friends methods outside to lib, then get friends list for event player, and do loop to send message

    public PlayerEvents(SimpleBungee plugin) {
        this.plugin = plugin;
        this.playerLoader = plugin.getPlayerLoader();
        this.lm = plugin.getLanguageManager();

        Configuration config = plugin.getConfig();
        network_messages = config.getBoolean("connection-messages.bungee",false);
        switch_server_messages = config.getBoolean("connection-messages.serverswitch",false);
        friends_messages = config.getBoolean("connection-messages.friends",true);

        if(friends_messages) {
            friendsManager = plugin.getFriendsManager();
        }
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        long date = System.currentTimeMillis() / 1000L;
        DataStore.CURRENT_PLAYTIME_STORE.put(player.getUniqueId(),date);

        if(network_messages) {
            TextComponent join_message = lm.getTextComponent("connection-messages.JOIN",player);
            plugin.getProxy().broadcast(join_message);
        }else if(friends_messages && friendsManager != null) {
            List<ProxiedPlayer> friends = friendsManager.getOnlineFriends(player.getUniqueId());
            TextComponent join_message = lm.getTextComponent("connection-messages.friends.JOIN",player);
            for (ProxiedPlayer friend : friends) {
                friend.sendMessage(join_message);
            }
        }

    }
    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if(network_messages) {
            TextComponent leave_message = lm.getTextComponent("connection-messages.QUIT",player);
            plugin.getProxy().broadcast(leave_message);
        }else if(friends_messages && friendsManager != null) {
            List<ProxiedPlayer> friends = friendsManager.getOnlineFriends(player.getUniqueId());
            TextComponent message = lm.getTextComponent("connection-messages.friends.QUIT",player);
            for (ProxiedPlayer friend : friends) {
                friend.sendMessage(message);
            }
        }
        DataStore.CURRENT_PLAYTIME_STORE.remove(player.getUniqueId());
        try {
            playerLoader.save(player);
        } catch (NoClassDefFoundError ex) {
            plugin.getLogger().warning("Could not save player information for " + player.getName() + " [NoClassDefFoundError]");
        } catch(Exception ex) {
            plugin.getLogger().warning("Could not save player information for " + player.getName());
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    public void onServerConnect(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if(player.isConnected() && player.getServer() != null && !e.isCancelled()) {
            if(switch_server_messages) {
                LAST_SERVER_MAP.put(player.getUniqueId(),e.getPlayer().getServer().getInfo());
            }
        }

    }
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();
        ServerInfo previous = LAST_SERVER_MAP.get(player.getUniqueId());
        ServerInfo current = player.getServer().getInfo();
        if(previous != null && current != previous) {
            Placeholder ph_previous = new Placeholder("previous",previous.getName());
            if(switch_server_messages) {
                TextComponent tc = lm.getTextComponent("connection-messages.SERVERSWITCH",player,ph_previous);
                plugin.getProxy().broadcast(tc);
            }else if(friends_messages && friendsManager != null) {
                List<ProxiedPlayer> friends = friendsManager.getOnlineFriends(player.getUniqueId());
                TextComponent message = lm.getTextComponent("connection-messages.friends.SERVERSWITCH",player,ph_previous);
                for (ProxiedPlayer friend : friends) {
                    friend.sendMessage(message);
                }
            }
        }

    }
}

package me.jackz.simplebungee.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

//will be stored in data.yml
public class OfflinePlayerStore {
    //private transient ProxiedPlayer player;
    //things to store
    private final UUID id;
    private final String last_username;
    private final String last_server;
    private final long last_online;
    private final String IP;
    //public static Map<UUID,Long> CURRENT_PLAYTIME_STORE;

    public OfflinePlayerStore(UUID id, String username, String server, long lastonline, String IP) {
        this.id = id;
        this.last_username = username;
        this.last_server = server;
        this.last_online = lastonline;
        this.IP = IP;
        //this.player = player;
        //calculated fields:
        //this.id = player.getUniqueId();
        //this.last_username = player.getName();
        //this.last_server = player.getServer().getInfo().getName();
        //this.last_online = System.currentTimeMillis() / 1000L;
        //this.IP = player.getAddress().getHostString();
    }
    public OfflinePlayerStore(ProxiedPlayer player) {
        this.id = player.getUniqueId();
        this.last_username = player.getName();
        this.last_server = player.getServer().getInfo().getName();
        this.last_online = System.currentTimeMillis() / 1000L;
        this.IP = player.getAddress().getHostString();
    }

    public String getIP() {
        return IP;
    }

    public String getLastOnline() {
        long now = System.currentTimeMillis() / 1000L;
        return Util.getTimeBetween(last_online,now) + " ago";
    }
    public long getLogoutTime() {
        return last_online;
    }

    public String getLastUsername() {
        return last_username;
    }
    public String getLastServer() {
        return last_server;
    }

    public UUID getUUID() {
        return id;
    }

    public boolean isOnline(ProxyServer proxy) {
        return proxy.getPlayer(id) != null;
    }
    public ProxiedPlayer getOnlinePlayer(ProxyServer proxy) {
        return proxy.getPlayer(id);
    }
}

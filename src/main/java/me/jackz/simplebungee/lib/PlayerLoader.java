package me.jackz.simplebungee.lib;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.UUID;

public class PlayerLoader {
    private SimpleBungee plugin;

    public PlayerLoader(SimpleBungee plugin) {
        this.plugin = plugin;
    }
    public void save(ProxiedPlayer player) throws IOException {
        Configuration data = plugin.data;
        if(player == null) return;
        OfflinePlayerStore store = new OfflinePlayerStore(player);
        //data.set("players." + player.getUniqueId(),store);
        String key = "players." + player.getUniqueId() + ".";
        data.set(key + "last_username",store.getLastUsername());
        data.set(key + "last_login",store.getLogoutTime());
        data.set(key + "last_ip",store.getIP());
        data.set(key + "last_server",store.getLastServer());
        plugin.saveData();
    }
    public boolean isPlayerOnline(UUID uuid) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
        return p != null;
    }
    public boolean isPlayerOnline(String username) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(username);
        return p != null;
    }
    /** Get an OfflinePlayerStore object containing information about player's last information
     * @param uuid player's unique id
     * @return OfflinePlayerStore
     */
    public OfflinePlayerStore getPlayer(UUID uuid) {
        Configuration data =  plugin.data;
        String key = "players." + uuid + ".";
        String username = data.getString(key + "last_username");
        long lastonline = data.getLong(key + "last_login");
        String IP = data.getString(key + "last_ip");
        String last_server = data.getString(key+"last_server");
        OfflinePlayerStore store = new OfflinePlayerStore(uuid,username,last_server,lastonline,IP);
        return store;
    }

    /** Gets an OfflinePlayerStore searched from a username
     * @param username Player's username
     * @return OfflinePlayerStore or null
     */
    public OfflinePlayerStore getOfflinePlayer(String username){
        UUID uuid = FindOfflinePlayer(username);
        if(uuid != null) {
            OfflinePlayerStore player = getPlayer(uuid);
            return player;
        }
        return null;
    }

    /** Gets the UUID from an offline player based on username
     * @param username The username to search for
     * @return UUID
     */
    public UUID FindOfflinePlayer(String username) {
        Configuration players = plugin.data.getSection("players");
        for (String uuid : players.getKeys()) {
            String last_username = players.getString(uuid + ".last_username");
            if(last_username.equalsIgnoreCase(username)) return UUID.fromString(uuid);
        }
        return null;
    }
}

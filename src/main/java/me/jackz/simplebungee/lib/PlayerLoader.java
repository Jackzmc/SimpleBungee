package me.jackz.simplebungee.lib;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerLoader {
    private SimpleBungee plugin;

    public PlayerLoader(SimpleBungee plugin) {
        this.plugin = plugin;
    }
    public void save(ProxiedPlayer player) throws IOException {
        File data_file = new File(plugin.getDataFolder(),"data.yml");
        Configuration data = plugin.getData();
        OfflinePlayerStore store = new OfflinePlayerStore(player);
        //data.set("players." + player.getUniqueId(),store);
        String key = "players." + player.getUniqueId() + ".";
        data.set(key + "last_username",store.getLastUsername());
        data.set(key + "last_login",store.getLogoutTime());
        data.set(key + "last_ip",store.getIP());
        data.set(key + "last_server",store.getLastServer());
        plugin.saveConfiguration(data,data_file);
    }
    public OfflinePlayerStore getPlayer(UUID uuid) throws IOException {
        File data_file = new File(plugin.getDataFolder(),"data.yml");
        Configuration data = plugin.getData();
        String key = "players." + uuid + ".";
        String username = data.getString(key + "last_username");
        long lastonline = data.getLong(key + "last_login");
        String IP = data.getString(key + "last_ip");
        String last_server = data.getString(key+"last_server");
        OfflinePlayerStore store = new OfflinePlayerStore(uuid,username,last_server,lastonline,IP);
        return store;
    }

    public UUID FindOfflinePlayer(String username) throws IOException {
        File data_file = new File(plugin.getDataFolder(),"data.yml");
        Configuration players = plugin.getData().getSection("players");
        /*
        players: [var: players]
            <uuid>: [for loop of UUID]
                last_username: <username>
         */
        for (String uuid : players.getKeys()) {
            String last_username = players.getString(uuid + ".last_username");
            if(last_username.equalsIgnoreCase(username)) return UUID.fromString(uuid);
        }
        return null;
    }
}

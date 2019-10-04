package me.jackz.simplebungee.managers;

import javafx.util.Pair;
import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {
    private SimpleBungee plugin;
    private Map<UUID, Pair<Long, TextComponent>> banlist = new HashMap<>();
    //Pair<Duration, Reason>
    public BanManager(SimpleBungee plugin) {
        this.plugin = plugin;
    }
    public boolean isPlayerBanned(ProxiedPlayer player) {
        Pair<Long,TextComponent> status = banlist.get(player.getUniqueId());
        return status != null;
    }
    public void banPlayer(ProxiedPlayer player, TextComponent reason) {
        if(!isPlayerBanned(player)) {
            Pair<Long,TextComponent> newPair = new Pair<>(0L, reason);
            banlist.put(player.getUniqueId(),newPair);

            player.disconnect(reason);
        }
    }
    public void banPlayer(ProxiedPlayer player, TextComponent reason, Long duration) {
        if(!isPlayerBanned(player)) {
            Pair<Long,TextComponent> newPair = new Pair<>(duration, reason);
            banlist.put(player.getUniqueId(),newPair);

            player.disconnect(reason);
        }
    }
    public void unbanPlayer(ProxiedPlayer player) {
        //todo
    }
    public Long getDuration(ProxiedPlayer player) {
        Pair<Long,TextComponent> pair = banlist.get(player.getUniqueId());
        if(pair != null) {
            return pair.getKey();
        }else{
            return null;
        }
    }
    public TextComponent getReasonMessage(ProxiedPlayer player) {
        Pair<Long,TextComponent> pair = banlist.get(player.getUniqueId());
        if(pair != null) {
            return pair.getValue();
        }else{
            return null;
        }
    }

    public void saveList() {
        //todo
    }
    public void loadList() {
        //todo
    }
}

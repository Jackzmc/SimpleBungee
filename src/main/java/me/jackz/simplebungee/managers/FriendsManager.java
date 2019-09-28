package me.jackz.simplebungee.managers;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FriendsManager {
    private final SimpleBungee plugin;
    private final LanguageManager lm;

    private Map<UUID, List<UUID>> FRIENDS_LIST = new HashMap<>();
    private final Map<UUID, List<UUID>> FRIEND_REQUESTS = new HashMap<>();

    public FriendsManager(SimpleBungee plugin) {
        this.plugin = plugin;
        this.lm = SimpleBungee.getLanguageManager();
    }

    public void joinFriend(ProxiedPlayer player, ProxiedPlayer friend) {
        if(friend != null) {
            if(getFriends(player.getUniqueId()).contains(friend.getUniqueId())) {
                player.connect(friend.getServer().getInfo());
                player.sendMessage(lm.getTextComponent("friends.JOIN_FRIEND",friend));
            }else{
                player.sendMessage(lm.getTextComponent("friends.NOT_FRIENDS_WITH",friend));
            }
        }else{
            player.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
        }
    }

    public List<UUID> getFriends(UUID target) {
        List<UUID> list = FRIENDS_LIST.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    public List<ProxiedPlayer> getOnlineFriends(UUID target) {
        List<UUID> main_friend_list = getFriends(target);
        List<ProxiedPlayer> online = new ArrayList<>();
        for (UUID uuid : main_friend_list) {
            ProxiedPlayer p = plugin.getProxy().getPlayer(uuid);
            if(p != null) online.add(p);
        }
        return online;
    }
    public List<UUID> getFriendRequests(UUID target) {
        List<UUID> list = FRIEND_REQUESTS.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    public void addFriend(UUID target, UUID friend) {
        if(target == friend) return; //if trying to add self as friend ,fail
        List<UUID> list = getFriends(target);
        if(list.contains(friend)) return; //silent fail if friend is already friends with target

        list.add(friend);
        FRIENDS_LIST.put(target, list);
    }
    public void removeFriend(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriends(target);
        list.remove(friend);
        FRIENDS_LIST.put(target, list);
    }
    public void addFriendRequest(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriendRequests(target);
        list.add(friend);
        FRIEND_REQUESTS.put(target,list);
    }

    public void saveFriendsList() throws IOException {
        Configuration data = plugin.data;
        for (Map.Entry<UUID, List<UUID>> entry : FRIENDS_LIST.entrySet()) {
            Configuration sub_player = data.getSection("friends." + entry.getKey().toString());

            List<String> string_friends = entry.getValue().stream().map(UUID::toString).collect(Collectors.toList());
            sub_player.set("friends",string_friends);
        }
        plugin.saveData();
    }
    public void loadFriendsList() {
        Configuration data = plugin.data;
        Configuration sub_friends = data.getSection("friends");
        FRIENDS_LIST = new HashMap<>();
        if(sub_friends != null) {
            for (String id : sub_friends.getKeys()) {
                UUID uuid = UUID.fromString(id);
                Configuration sub_player = data.getSection("friends." + id);
                List<String> str_friends = sub_player.getStringList("friends");

                List<UUID> friends = str_friends.stream().map(UUID::fromString).collect(Collectors.toList());
                FRIENDS_LIST.put(uuid, friends);
            }
        }
    }
}

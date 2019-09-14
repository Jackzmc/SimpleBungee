package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.LanguageManager;
import me.jackz.simplebungee.lib.OfflinePlayerStore;
import me.jackz.simplebungee.lib.Placeholder;
import me.jackz.simplebungee.lib.PlayerLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Friends extends Command {
    private SimpleBungee plugin;
    private PlayerLoader playerLoader;
    private Map<UUID, List<UUID>> FRIENDS_LIST = new HashMap<>();
    private Map<UUID, List<UUID>> FRIEND_REQUESTS = new HashMap<>();
    private LanguageManager lm;

    public Friends(SimpleBungee plugin) {
        super("friends","simplebungee.command.friends","friend");
        this.plugin = plugin;
        this.playerLoader = new PlayerLoader(plugin);
        this.lm = plugin.getLanguageManager();
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(lm.getTextComponent("core.PLAYER_ONLY"));
            sender.sendMessage(new TextComponent("You must be a player to use this command"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            //show gui
            sender.sendMessage(lm.getTextComponent("friends.HELP"));
        }else{
            switch(args[0].toLowerCase()) {
                case "add": {
                    if (args.length >= 2) {
                        Collection<ProxiedPlayer> playerMatches = plugin.getProxy().matchPlayer(args[1]);
                        if (playerMatches.size() > 0) {
                            ProxiedPlayer friend = playerMatches.iterator().next();
                            List<UUID> friends_list = getFriends(friend.getUniqueId());
                            List<UUID> friend_requests = getFriendRequests(friend.getUniqueId());
                            if(friends_list.contains(player.getUniqueId())) {
                                sender.sendMessage(lm.getTextComponent("friends.ALREADY_FRIENDS",friend));
                                return;
                            }else if(friend_requests.contains(player.getUniqueId())) {
                                sender.sendMessage(lm.getTextComponent("friends.ALREADY_SENT_REQUEST",friend));
                                return;
                            }else if(friend == player) {
                                sender.sendMessage(lm.getTextComponent("friends.FRIENDS_WITH_SELF"));
                                return;
                            }
                            addFriendRequest(friend.getUniqueId(), player.getUniqueId());
                            sender.sendMessage(lm.getTextComponent("friends.REQUEST_SEND",friend));

                            TextComponent base = lm.getTextComponent("friends.RECEIVE_REQUEST",player);
                            TextComponent approve = new TextComponent("[ACCEPT] ");
                            TextComponent reject = new TextComponent("[REJECT]");
                            approve.setColor(ChatColor.GREEN);
                            approve.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend approve " + player.getUniqueId()));
                            approve.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Approve friend request").create())); //TODO: add localization to this
                            reject.setColor(ChatColor.RED);
                            reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend reject " + player.getUniqueId()));
                            reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rejects friend request").create())); //TODO: add localization to this

                            base.addExtra(approve);
                            base.addExtra(reject);
                            friend.sendMessage(base);
                        } else {
                            sender.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
                        }
                    } else {
                        sender.sendMessage(lm.getTextComponent("friends.ADD_FRIEND_USAGE"));
                    }
                    break;
                }
                case "remove": {
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            removeFriend(friend.getUniqueId(),player.getUniqueId());
                            removeFriend(player.getUniqueId(),friend.getUniqueId());
                            sender.sendMessage(lm.getTextComponent("friends.REMOVE_PLAYER",friend));
                            sender.sendMessage(lm.getTextComponent("friends.FRIENDSHIP_REMOVED",player));
                        }else{
                            sender.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
                        }
                    }else{
                        sender.sendMessage(lm.getTextComponent("friends.DEL_FRIEND_USAGE"));
                    }
                    break;
                }
                case "_join":
                    try {
                        UUID uuid = UUID.fromString(args[1]);
                        ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                        joinFriend(player, friend);
                    }catch(IllegalArgumentException e) {
                        player.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND_ALT"));
                    }
                    break;
                case "join":
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            joinFriend(player, friend);
                        }else{
                            player.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
                        }
                    }else{
                        player.sendMessage(lm.getTextComponent("friends.JOIN_USAGE"));
                    }
                    break;
                case "invite":
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            if(getFriends(player.getUniqueId()).contains(friend.getUniqueId())) {
                                player.sendMessage(lm.getTextComponent("friends.INVITE_SUCCESS",friend));
                                ServerInfo player_server = player.getServer().getInfo();

                                TextComponent tc = lm.getTextComponent("friends.RECIEVE_INVITE",player);
                                TextComponent join = new TextComponent(" [JOIN]");
                                join.setColor(ChatColor.GREEN);
                                join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend _join " + player.getUniqueId() ));
                                join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Join your friend's game").create()));
                                tc.addExtra(join);
                                friend.sendMessage(tc);
                            }else{
                                sender.sendMessage(lm.getTextComponent("friends.NOT_FRIENDS_WITH",friend));
                            }
                        }else{
                            sender.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
                        }
                    }else{
                        sender.sendMessage(lm.getTextComponent("INVITE_USAGE"));
                    }

                    break;
                case "list": {
                    List<UUID> friends = FRIENDS_LIST.get(player.getUniqueId());
                    List<UUID> requests = FRIEND_REQUESTS.get(player.getUniqueId());
                    ServerInfo player_server = player.getServer().getInfo();
                    TextComponent title_friends = lm.getTextComponent("friends.LIST_HEADING");
                    if (friends != null && friends.size() > 0) {
                        for (UUID uuid : friends) {
                            boolean is_online = playerLoader.isPlayerOnline(uuid);
                            TextComponent comp_friend;
                            if(is_online) {
                                ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                                ServerInfo server = friend.getServer().getInfo();
                                comp_friend = new TextComponent(friend.getName());

                                BaseComponent[] hovertext = new ComponentBuilder("§7Server: §e" + server.getName())
                                        .append(" (" + server.getPlayers().size() + " online)")
                                        .append("\n§7Ping: §e" + friend.getPing() + " ms")
                                        .create();
                                comp_friend.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hovertext));
                                if(!server.equals(player_server)) {
                                    TextComponent join = new TextComponent(" [JOIN]");
                                    join.setColor(ChatColor.LIGHT_PURPLE);
                                    join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend _join " + uuid.toString()));
                                    join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Click to join their server").create()));

                                    TextComponent invite = new TextComponent(" [INVITE]");
                                    invite.setColor(ChatColor.BLUE);
                                    invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend invite " + friend.getName()));
                                    invite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("§7Click to invite them to join your server").create()));
                                    comp_friend.addExtra(join);
                                    comp_friend.addExtra(invite);
                                }else{
                                    TextComponent in_server = new TextComponent(" [In Server]");
                                    in_server.setColor(ChatColor.LIGHT_PURPLE);
                                    comp_friend.addExtra(in_server);
                                }
                            }else{
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                                comp_friend = new TextComponent(friend.getLastUsername());

                                BaseComponent[] hovertext = new ComponentBuilder("§7Last Online: §e" + friend.getLastOnline())
                                        .append("\n§7Last Server: §e" + friend.getLastServer())
                                        .create();
                                comp_friend.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hovertext));
                            }
                            comp_friend.setColor(is_online ? ChatColor.GREEN : ChatColor.RED);
                            title_friends.addExtra("\n");
                            title_friends.addExtra(comp_friend);
                        }
                    } else {
                        title_friends.addExtra(lm.getTextComponent("friends.NO_FRIENDS"));
                    }

                    if (requests != null && requests.size() > 0) {
                        TextComponent title_friend_requests = lm.getTextComponent("friends.REQUESTS_HEADING");
                        title_friend_requests.setColor(ChatColor.GOLD);
                        title_friends.addExtra(title_friend_requests);
                        title_friend_requests.addExtra(lm.getTextComponent("friends.FRIEND_REQUEST_SIZE",new Placeholder("requests",requests.size())));
                        //title_friends.addExtra("\n§7You have " + requests.size() + " friend requests");
                        for (UUID request : requests) {
                            OfflinePlayerStore friend = playerLoader.getPlayer(request);
                            TextComponent comp_request = new TextComponent(friend.getLastUsername());
                            comp_request.setColor(ChatColor.YELLOW);

                            TextComponent approve = new TextComponent(" [ACCEPT] ");
                            TextComponent reject = new TextComponent("[REJECT]");
                            approve.setColor(ChatColor.GREEN);
                            approve.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend approve " + player.getUniqueId()));
                            approve.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Approve friend request").create()));
                            reject.setColor(ChatColor.RED);
                            reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend reject " + player.getUniqueId()));
                            reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rejects friend request").create()));

                            comp_request.addExtra(approve);
                            comp_request.addExtra(reject);
                            title_friends.addExtra("\n");
                            title_friends.addExtra(comp_request);
                        }
                    }
                    sender.sendMessage(title_friends);
                    break;
                }
                case "approve":
                case "accept": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = getFriendRequests(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);

                                requests.remove(uuid);
                                addFriend(player.getUniqueId(),uuid);
                                addFriend(uuid,player.getUniqueId());

                                Placeholder username = new Placeholder("player_name",friend.getLastUsername());
                                Placeholder server = new Placeholder("server_name",friend.getLastServer());
                                sender.sendMessage(lm.getTextComponent("friends.ACCEPT_SUCCESS",username,server));

                                ProxiedPlayer online_friend = friend.getOnlinePlayer(plugin.getProxy());
                                //if friend is online
                                if(online_friend != null) {
                                    online_friend.sendMessage(lm.getTextComponent("friends.RECEIVE_ACCEPT_SUCCESS",player));
                                }
                            }else{
                                sender.sendMessage(lm.getTextComponent("friends.NO_PENDING_REQUEST"));
                            }
                        }catch(IllegalArgumentException e) {
                            sender.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND"));
                        }

                    }else{
                        sender.sendMessage(lm.getTextComponent("friends.ACCEPT_USAGE"));
                    }
                    break;
                }
                case "deny":
                case "reject": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = getFriendRequests(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                                requests.remove(uuid);
                                Placeholder username = new Placeholder("player_name",friend.getLastUsername());
                                Placeholder server = new Placeholder("server_name",friend.getLastServer());
                                sender.sendMessage(lm.getTextComponent("friends.REJECT_REQUEST",username,server));
                            }else{
                                sender.sendMessage(lm.getTextComponent("friends.NO_PENDING_REQUEST"));
                            }
                        }catch(IllegalArgumentException e) {
                            sender.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND_ALT"));
                        }

                    }else{
                        sender.sendMessage(lm.getTextComponent("friends.REJECT_USAGE"));
                    }
                    break;
                }
                default:
                    sender.sendMessage(lm.getTextComponent("friends.UNKNOWN_ARGUMENT"));
            }
        }
    }

    private void joinFriend(ProxiedPlayer player, ProxiedPlayer friend) {
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

    private List<UUID> getFriends(UUID target) {
        List<UUID> list = FRIENDS_LIST.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    private List<UUID> getFriendRequests(UUID target) {
        List<UUID> list = FRIEND_REQUESTS.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    private void addFriend(UUID target, UUID friend) {
        if(target == friend) return; //if trying to add self as friend ,fail
        List<UUID> list = getFriends(target);
        if(list.contains(friend)) return; //silent fail if friend is already friends with target

        list.add(friend);
        FRIENDS_LIST.put(target, list);
    }
    private void removeFriend(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriends(target);
        list.remove(friend);
        FRIENDS_LIST.put(target, list);
    }
    private void addFriendRequest(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriendRequests(target);
        list.add(friend);
        FRIEND_REQUESTS.put(target,list);
    }

    public void SaveFriendsList() throws IOException {
        Configuration data = plugin.data;
        for (Map.Entry<UUID, List<UUID>> entry : FRIENDS_LIST.entrySet()) {
            Configuration sub_player = data.getSection("friends." + entry.getKey().toString());

            List<String> string_friends = entry.getValue().stream().map(UUID::toString).collect(Collectors.toList());
            sub_player.set("friends",string_friends);
        }
        plugin.saveData();
    }
    public void LoadFriendsList() throws IOException {
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

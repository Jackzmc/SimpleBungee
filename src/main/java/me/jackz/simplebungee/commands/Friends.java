package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.OfflinePlayerStore;
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

    public Friends(SimpleBungee plugin) {
        super("friends","simplebungee.command.friends","friend");
        this.plugin = plugin;
        this.playerLoader = new PlayerLoader(plugin);
    }
    /*
    commands:
    1. add <player>
    2. remove <player>
    3. join <player> -> join server
    4. list
    5. msg -> toggle or send chat
    6. accept <request>
    7. reject <request>
    8. help

    friend add COOLDOWN / limit
    multiple friend bug
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("You must be a player to use this command"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            //show gui
            BaseComponent[] tc = new ComponentBuilder("§6Friends Help Menu").color(ChatColor.GOLD)
                    .append("\n/friend add <player>").color(ChatColor.YELLOW)
                        .append(" - send a friend request").color(ChatColor.GRAY)
                    .append("\n/friend remove <player>").color(ChatColor.YELLOW)
                        .append(" - unfriend a player").color(ChatColor.GRAY)
                    .append("\n/friend join <player>").color(ChatColor.YELLOW)
                        .append(" - join your friend's game").color(ChatColor.GRAY)
                    .append("\n/friend invite <player>").color(ChatColor.YELLOW)
                        .append(" - invite a friend to join your server").color(ChatColor.GRAY)
                    .append("\n/friend list").color(ChatColor.YELLOW)
                        .append(" - view all your friends and friend requests").color(ChatColor.GRAY)
                    //.append("\n/friend accept <player> - accept a friend request")
                    //.append("\n/friend reject <player> - reject a friend request")
                    .create();
            sender.sendMessage(tc);
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
                                sender.sendMessage(new TextComponent("§cYou are already friends with " + friend.getName()));
                                return;
                            }else if(friend_requests.contains(player.getUniqueId())) {
                                sender.sendMessage(new TextComponent("§cYou already sent a friend request to " + friend.getName()));
                                return;
                            }else if(friend == player) {
                                sender.sendMessage(new TextComponent("§cYou can't be friends with yourself!"));
                                return;
                            }
                            addFriendRequest(friend.getUniqueId(), player.getUniqueId());
                            sender.sendMessage(new TextComponent("§aSent a friend request to " + friend.getName()));

                            TextComponent base = new TextComponent("§e" + sender.getName() + " §7sent you a friend request ");
                            TextComponent approve = new TextComponent("[ACCEPT] ");
                            TextComponent reject = new TextComponent("[REJECT]");
                            approve.setColor(ChatColor.GREEN);
                            approve.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend approve " + player.getUniqueId()));
                            approve.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Approve friend request").create()));
                            reject.setColor(ChatColor.RED);
                            reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend reject " + player.getUniqueId()));
                            reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rejects friend request").create()));

                            base.addExtra(approve);
                            base.addExtra(reject);
                            friend.sendMessage(base);
                        } else {
                            sender.sendMessage(new TextComponent("§cCould not find any players online with that name."));
                        }
                    } else {
                        sender.sendMessage(new TextComponent("§cPlease enter an online player. Usage: /friend add <username>"));
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
                            sender.sendMessage(new TextComponent("§cRemoved " + friend.getName() + " from your friends list"));
                            friend.sendMessage(new TextComponent("§c" + player.getName() + " has unfriended you."));
                        }else{
                            sender.sendMessage(new TextComponent("§cCould not find any friend matching that name online."));
                        }
                    }else{
                        sender.sendMessage(new TextComponent("§cPlease enter a friend to remove. Usage: /friend remove <username>"));
                    }
                    break;
                }
                case "_join":
                    try {
                        UUID uuid = UUID.fromString(args[1]);
                        ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                        joinFriend(player, friend);
                    }catch(IllegalArgumentException e) {
                        player.sendMessage(new TextComponent("§cCould not find that player"));
                    }
                    break;
                case "join":
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            joinFriend(player, friend);
                        }else{
                            player.sendMessage(new TextComponent("§cCould not find any friend matching that name online."));
                        }
                    }else{
                        player.sendMessage(new TextComponent("§cPlease enter a friend to join. Usage: /friend join <username>"));
                    }
                    break;
                case "invite":
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            if(getFriends(player.getUniqueId()).contains(friend.getUniqueId())) {
                                player.sendMessage(new TextComponent("§aInvited " + friend.getName() + " to join your server."));
                                ServerInfo player_server = player.getServer().getInfo();

                                TextComponent tc = new TextComponent("§e" + player.getName() + " has sent you an invite to join " + player_server.getName());
                                TextComponent join = new TextComponent("[JOIN]");
                                join.setColor(ChatColor.GREEN);
                                join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend _join " + player.getUniqueId() ));
                                join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Join your friend's game").create()));
                                tc.addExtra(join);
                                friend.sendMessage(tc);
                            }else{
                                sender.sendMessage(new TextComponent("§cYou are not friends with " + friend.getName()));
                            }
                        }else{
                            sender.sendMessage(new TextComponent("§cCould not find any friend matching that name online."));
                        }
                    }else{
                        sender.sendMessage(new TextComponent("§cPlease enter a friend to invite. Usage: /friend invite <username>"));
                    }

                    break;
                case "list": {
                    List<UUID> friends = FRIENDS_LIST.get(player.getUniqueId());
                    List<UUID> requests = FRIEND_REQUESTS.get(player.getUniqueId());
                    ServerInfo player_server = player.getServer().getInfo();
                    TextComponent title_friends = new TextComponent("§6§nFriends§r");
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
                        title_friends.addExtra("\n§cYou have no friends, why not find some?");
                    }

                    if (requests != null && requests.size() > 0) {
                        TextComponent title_friend_requests = new TextComponent("\n§6§nFriend Requests§r");
                        title_friend_requests.setColor(ChatColor.GOLD);
                        title_friends.addExtra(title_friend_requests);
                        title_friends.addExtra("\n§7You have " + requests.size() + " friend requests");
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

                                sender.sendMessage(new TextComponent("§aYou accepted the friend request from " + friend.getLastUsername()));

                                ProxiedPlayer online_friend = friend.getOnlinePlayer(plugin.getProxy());
                                //if friend is online
                                if(online_friend != null) {
                                    online_friend.sendMessage(new TextComponent("§e" + player.getName() + " has accepted your friend request"));
                                }
                            }else{
                                sender.sendMessage(new TextComponent("§cThere is no pending friend request from that player."));
                            }
                        }catch(IllegalArgumentException e) {
                            sender.sendMessage(new TextComponent("§cCould not find that player"));
                        }

                    }else{
                        sender.sendMessage(new TextComponent("§cPlease enter the username of the player to accept"));
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
                                sender.sendMessage(new TextComponent("§cRejected friend request from " + friend.getLastUsername()));
                            }else{
                                sender.sendMessage(new TextComponent("§cThere is no pending friend request from that player."));
                            }
                        }catch(IllegalArgumentException e) {
                            sender.sendMessage(new TextComponent("§cCould not find that player"));
                        }

                    }else{
                        sender.sendMessage(new TextComponent("§cPlease enter the username of the player to reject"));
                    }
                    break;
                }
                default:
                    sender.sendMessage(new TextComponent("§cUnknown argument, try §e/friends help"));
            }
        }
    }

    private void joinFriend(ProxiedPlayer player, ProxiedPlayer friend) {
        if(friend != null) {
            if(getFriends(player.getUniqueId()).contains(friend.getUniqueId())) {
                player.connect(friend.getServer().getInfo());
                player.sendMessage(new TextComponent("§eConnecting you to " + friend.getName() + "'s active server..."));
            }else{
                player.sendMessage(new TextComponent("§cYou are not friends with " + friend.getName()));
            }
        }else{
            player.sendMessage(new TextComponent("§cCould not find an online friend with that name."));
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
        for (String id : sub_friends.getKeys()) {
            UUID uuid = UUID.fromString(id);
            Configuration sub_player = data.getSection("friends." + id);
            List<String> str_friends = sub_player.getStringList("friends");

            List<UUID> friends = str_friends.stream().map(UUID::fromString).collect(Collectors.toList());
            FRIENDS_LIST.put(uuid,friends);
        }
    }
}

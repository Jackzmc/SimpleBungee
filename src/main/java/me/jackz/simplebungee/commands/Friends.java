package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.lib.OfflinePlayerStore;
import me.jackz.simplebungee.lib.PlayerLoader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.*;

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

    //GUI probavly instead?
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
            BaseComponent[] tc = new ComponentBuilder("§6Friends Menu").color(ChatColor.GOLD)
                    .append("\n/friend add <player> - send a friend request").color(ChatColor.YELLOW)
                    .append("\n/friend remove <player> - unfriend a player")
                    .append("\n/friend join <player> - join your friend's game")
                    .append("\n/friend list - view all your friends and friend requests")
                    .append("\n/friend msg <player> <msg> - send a message to a friend")
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
                            List<UUID> friends_list = FRIEND_REQUESTS.get(friend.getUniqueId());
                            addFriendRequest(friend.getUniqueId(), player.getUniqueId());

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
                case "remove":
                case "join":
                case "leave":
                case "msg":
                case "list": {
                    List<UUID> friends = FRIENDS_LIST.get(player.getUniqueId());
                    List<UUID> requests = FRIEND_REQUESTS.get(player.getUniqueId());

                    TextComponent tc = new TextComponent("§6Friends");
                    if (friends != null && friends.size() > 0) {
                        for (UUID uuid : friends) {
                            boolean is_online = playerLoader.isPlayerOnline(uuid);
                            OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                            TextComponent comp_friend = new TextComponent(friend.getLastUsername());
                            comp_friend.setColor(is_online ? ChatColor.GREEN : ChatColor.RED);

                            tc.addExtra("\n");
                            tc.addExtra(comp_friend);
                        }
                    } else {
                        tc.addExtra("\n§cYou have no friends, why not find some?");
                    }

                    if (requests != null && requests.size() > 0) {
                        tc.addExtra("\n[debug] " + requests.size() + " friend requests");

                    }
                    sender.sendMessage(tc);
                    break;
                }
                case "accept": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = getFriends(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                requests.remove(uuid);
                                addFriend(player.getUniqueId(),uuid);
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
                case "reject": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = getFriends(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                requests.remove(uuid);
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
    private List<UUID> getFriends(UUID target) {
        List<UUID> list = FRIENDS_LIST.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    private List<UUID> getFriendRequests(UUID target) {
        List<UUID> list = FRIEND_REQUESTS.get(target);
        return (list != null) ? list : new ArrayList<>();
    }
    private void addFriend(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriends(target);
        list.add(friend);
        FRIENDS_LIST.put(target, list);
    }
    private void addFriendRequest(UUID target, UUID friend) {
        if(target == friend) return;
        List<UUID> list = getFriendRequests(target);
        list.add(friend);
        FRIEND_REQUESTS.put(target,list);
    }

    public void SaveFriendsList() {

    }
    public void LoadFriendsList() {

    }
}

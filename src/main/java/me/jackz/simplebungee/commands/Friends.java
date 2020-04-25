package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.FriendsManager;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.managers.PlayerLoader;
import me.jackz.simplebungee.utils.OfflinePlayerStore;
import me.jackz.simplebungee.utils.Placeholder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Friends extends Command {
    private final SimpleBungee plugin;
    private final PlayerLoader playerLoader;
    private final FriendsManager fm;

    private final LanguageManager lm;

    public Friends(SimpleBungee plugin) {
        super("friends","simplebungee.command.friends","friend","simplebungee:friend","simplebungee:friends");
        this.plugin = plugin;
        this.playerLoader = new PlayerLoader(plugin);
        this.lm = SimpleBungee.getLanguageManager();
        this.fm = plugin.getFriendsManager();
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            lm.sendMessage(sender,"core.PLAYER_ONLY");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            //show gui
            lm.sendMessage(sender,"friends.HELP");
        }else{
            switch(args[0].toLowerCase()) {
                case "add": {
                    if (args.length >= 2) {
                        Collection<ProxiedPlayer> playerMatches = plugin.getProxy().matchPlayer(args[1]);
                        if (playerMatches.size() > 0) {
                            ProxiedPlayer friend = playerMatches.iterator().next();
                            List<UUID> friends_list = fm.getFriends(friend.getUniqueId());
                            List<UUID> friend_requests = fm.getFriendRequests(friend.getUniqueId());
                            if(friends_list.contains(player.getUniqueId())) {
                                lm.sendMessage(sender,"friends.ALREADY_FRIENDS",friend);
                                return;
                            }else if(friend_requests.contains(player.getUniqueId())) {
                                lm.sendMessage(sender,"friends.ALREADY_SENT_REQUEST",friend);
                                return;
                            }else if(friend == player) {
                                lm.sendMessage(sender,"friends.FRIENDS_WITH_SELF");
                                return;
                            }
                            fm.addFriendRequest(friend.getUniqueId(), player.getUniqueId());
                            lm.sendMessage(sender,"friends.REQUEST_SEND",friend);

                            TextComponent base = lm.getTextComponent("friends.RECEIVE_REQUEST",player);
                            TextComponent approve = lm.getTextComponent("friends.ACCEPT_BUTTON");
                            TextComponent reject = lm.getTextComponent("friends.REJECT_BUTTON");
                            BaseComponent[] tooltip_approve = new ComponentBuilder(lm.getString("friends.ACCEPT_FRIEND_TOOLTIP")).create();
                            BaseComponent[] tooltip_reject = new ComponentBuilder(lm.getString("friends.REJECT_FRIEND_TOOLTIP")).create();

                            approve.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend approve " + player.getUniqueId()));
                            approve.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip_approve));
                            reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend reject " + player.getUniqueId()));
                            reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip_reject));

                            base.addExtra(" ");
                            base.addExtra(approve);
                            base.addExtra(" ");
                            base.addExtra(reject);
                            friend.sendMessage(base);
                        } else {
                            lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
                        }
                    } else {
                        lm.sendMessage(sender,"friends.ADD_FRIEND_USAGE");
                    }
                    break;
                }
                case "remove": {
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            fm.removeFriend(friend.getUniqueId(),player.getUniqueId());
                            fm.removeFriend(player.getUniqueId(),friend.getUniqueId());
                            lm.sendMessage(sender,"friends.REMOVE_PLAYER",friend);
                            friend.sendMessage(lm.getTextComponent("friends.FRIENDSHIP_REMOVED",player));
                        }else{
                            lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
                        }
                    }else{
                        lm.sendMessage(sender,"friends.DEL_FRIEND_USAGE");
                    }
                    break;
                }
                case "_join":
                    try {
                        UUID uuid = UUID.fromString(args[1]);
                        ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                        fm.joinFriend(player, friend);
                    }catch(IllegalArgumentException e) {
                        player.sendMessage(lm.getTextComponent("core.NO_PLAYER_FOUND_ALT"));
                    }
                    break;
                case "join":
                    if(args.length > 1) {
                        Collection<ProxiedPlayer> friends = plugin.getProxy().matchPlayer(args[1]);
                        if(friends.size() > 0) {
                            ProxiedPlayer friend = friends.iterator().next();
                            if(friend == player) {
                                player.sendMessage(lm.getTextComponent("friends.JOIN_SELF"));
                            }else if(friend.getServer().getInfo().getName().equals(player.getServer().getInfo().getName())) {
                                player.sendMessage(lm.getTextComponent("friends.SAME_SERVER_JOIN"));
                            }else{
                                fm.joinFriend(player, friend);
                            }
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
                            if(friend == player) {
                                player.sendMessage(lm.getTextComponent("friends.INVITE_SELF"));
                            } else if(friend.getServer().equals(player.getServer())) {
                                player.sendMessage(lm.getTextComponent("friends.SAME_SERVER_INVITE",friend));
                            }else if(friend.getServer().getInfo().getName().equals(player.getServer().getInfo().getName())) {
                                player.sendMessage(lm.getTextComponent("friends.SAME_SERVER_INVITE",friend));
                            }else if(fm.getFriends(player.getUniqueId()).contains(friend.getUniqueId())) {
                                player.sendMessage(lm.getTextComponent("friends.INVITE_SUCCESS",friend));

                                TextComponent tc = lm.getTextComponent("friends.RECEIVE_INVITE",player);
                                TextComponent join = lm.getTextComponent("friends.JOIN_SERVER_BUTTON");
                                join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend _join " + player.getUniqueId() ));
                                BaseComponent[] tooltip = new ComponentBuilder(lm.getString("friends.JOIN_FRIEND_TOOLTIP")).create();
                                join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,tooltip));
                                tc.addExtra(" ");
                                tc.addExtra(join);
                                friend.sendMessage(tc);
                            }else{
                                lm.sendMessage(sender,"friends.NOT_FRIENDS_WITH",friend);
                            }
                        }else{
                            lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
                        }
                    }else{
                        lm.sendMessage(sender,"INVITE_USAGE");
                    }

                    break;
                case "list": {
                    List<UUID> friends = fm.getFriends(player.getUniqueId()); //FRIENDS_LIST.get(player.getUniqueId());
                    List<UUID> requests = fm.getFriendRequests(player.getUniqueId()); //FRIEND_REQUESTS.get(player.getUniqueId());
                    ServerInfo player_server = player.getServer().getInfo();
                    TextComponent tc_base = lm.getTextComponent("friends.LIST_HEADING");
                    if (friends != null && friends.size() > 0) {
                        List<TextComponent> onlineFriends = new ArrayList<>();
                        List<TextComponent> offlineFriends = new ArrayList<>();

                        //get list of all friends
                        for (UUID uuid : friends) {
                            boolean is_online = playerLoader.isPlayerOnline(uuid);
                            TextComponent comp_friend;

                            if(is_online) {
                                ProxiedPlayer friend = plugin.getProxy().getPlayer(uuid);
                                ServerInfo server = friend.getServer().getInfo();
                                comp_friend = new TextComponent(friend.getName());
                                //print server information hovertext
                                BaseComponent[] hovertext = new ComponentBuilder("§7Server: §e" + server.getName())
                                        .append(" (" + server.getPlayers().size() + " online)")
                                        .append("\n§7Ping: §e" + friend.getPing() + " ms")
                                        .create();
                                comp_friend.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hovertext));
                                //send invite and join if not on same server
                                if(!server.equals(player_server)) {
                                    //sec: join
                                    TextComponent join = lm.getTextComponent("friends.JOIN_SERVER_BUTTON",friend);
                                    join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend _join " + uuid.toString()));
                                    BaseComponent[] tooltip_join = new ComponentBuilder(lm.getString("friends.JOIN_SERVER_TOOLTIP")).create();
                                    join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,tooltip_join));

                                    //sec: inv
                                    TextComponent invite = lm.getTextComponent("friends.INVITE_SERVER_BUTTON",friend);
                                    invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/friend invite " + friend.getName()));
                                    BaseComponent[] tooltip_invite = new ComponentBuilder(lm.getString("friends.INVITE_SERVER_TOOLTIP")).create();
                                    invite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,tooltip_invite));
                                    //sec: combined
                                    comp_friend.addExtra(" ");
                                    comp_friend.addExtra(join);
                                    comp_friend.addExtra(" ");
                                    comp_friend.addExtra(invite);
                                }else{
                                    TextComponent in_server = lm.getTextComponent("friends.FRIEND_IN_SERVER",friend);
                                    comp_friend.addExtra(" ");
                                    comp_friend.addExtra(in_server);
                                }
                                comp_friend.setColor(ChatColor.GREEN);
                                onlineFriends.add(comp_friend);
                            }else{
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                                comp_friend = new TextComponent(friend.getLastUsername());

                                //get last login info
                                BaseComponent[] hovertext = new ComponentBuilder("§7Last Online: §e" + friend.getLastOnline())
                                        .append("\n§7Last Server: §e" + friend.getLastServer())
                                        .create();

                                comp_friend.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hovertext));
                                comp_friend.setColor(ChatColor.RED);
                                offlineFriends.add(comp_friend);
                            }

                        }
                        //print online friends first, then offline
                        for (TextComponent onlineFriend : onlineFriends) {
                            tc_base.addExtra("\n");
                            tc_base.addExtra(onlineFriend);
                        }
                        for (TextComponent offlineFriend : offlineFriends) {
                            tc_base.addExtra("\n");
                            tc_base.addExtra(offlineFriend);
                        }
                    } else {
                        //show no friends msg if no friends
                        tc_base.addExtra("\n");
                        tc_base.addExtra(lm.getTextComponent("friends.NO_FRIENDS"));
                    }
                    sender.sendMessage(tc_base);
                    break;
                }
                case "requests" : {
                    List<UUID> requests = fm.getFriendRequests(player.getUniqueId());
                    if (requests != null && requests.size() > 0) {
                        TextComponent tc_base = lm.getTextComponent("friends.REQUESTS_HEADING",new Placeholder("requests",requests.size()));

                        for (UUID request : requests) {
                            OfflinePlayerStore friend = playerLoader.getPlayer(request);
                            TextComponent comp_request = new TextComponent(friend.getLastUsername());
                            comp_request.setColor(ChatColor.YELLOW);

                            //setup friend request [ACCEPT] [REJECT] buttons
                            TextComponent approve = lm.getTextComponent("friends.ACCEPT_BUTTON");
                            TextComponent reject = lm.getTextComponent("friends.REJECT_BUTTON");
                            BaseComponent[] tooltip_approve = new ComponentBuilder(lm.getString("friends.ACCEPT_FRIEND_TOOLTIP")).create();
                            BaseComponent[] tooltip_reject = new ComponentBuilder(lm.getString("friends.REJECT_FRIEND_TOOLTIP")).create();

                            //setup above button's click/hover events
                            approve.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend approve " + request));
                            approve.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip_approve));
                            reject.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend reject " + request));
                            reject.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip_reject));

                            //add buttons to main message
                            comp_request.addExtra(" ");
                            comp_request.addExtra(approve);
                            comp_request.addExtra(" ");
                            comp_request.addExtra(reject);

                            tc_base.addExtra("\n");
                            tc_base.addExtra(comp_request);
                        }
                    }else{
                        player.sendMessage(lm.getTextComponent("friends.NO_FRIEND_REQUESTS"));
                    }
                    break;
                }
                case "approve":
                case "accept": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = fm.getFriendRequests(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                            //TODO: actually remove request ? may not be removed now
                                requests.remove(uuid);
                                fm.addFriend(player.getUniqueId(),uuid);
                                fm.addFriend(uuid,player.getUniqueId());

                                Placeholder username = new Placeholder("player_name",friend.getLastUsername());
                                Placeholder server = new Placeholder("server_name",friend.getLastServer());
                                lm.sendMessage(sender,"friends.ACCEPT_SUCCESS",username,server);

                                ProxiedPlayer online_friend = friend.getOnlinePlayer(plugin.getProxy());
                                //if friend is online
                                if(online_friend != null) {
                                    online_friend.sendMessage(lm.getTextComponent("friends.RECEIVE_ACCEPT_SUCCESS",player));
                                }
                            }else{
                                lm.sendMessage(sender,"friends.NO_PENDING_REQUEST");
                            }
                        }catch(IllegalArgumentException e) {
                            lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
                        }

                    }else{
                        lm.sendMessage(sender,"friends.ACCEPT_USAGE");
                    }
                    break;
                }
                case "deny":
                case "reject": {
                    if(args.length >= 2) {
                        try {
                            UUID uuid = UUID.fromString(args[1]);
                            List<UUID> requests = fm.getFriendRequests(player.getUniqueId());
                            if(requests.contains(uuid)) {
                                OfflinePlayerStore friend = playerLoader.getPlayer(uuid);
                                //TODO: view todo at above
                                requests.remove(uuid);
                                Placeholder username = new Placeholder("player_name",friend.getLastUsername());
                                Placeholder server = new Placeholder("server_name",friend.getLastServer());
                                lm.sendMessage(sender,"friends.REJECT_REQUEST",username,server);
                            }else{
                                lm.sendMessage(sender,"friends.NO_PENDING_REQUEST");
                            }
                        }catch(IllegalArgumentException e) {
                            lm.sendMessage(sender,"core.NO_PLAYER_FOUND_ALT");
                        }

                    }else{
                        lm.sendMessage(sender,"friends.REJECT_USAGE");
                    }
                    break;
                }
                default:
                    lm.sendMessage(sender,"friends.UNKNOWN_ARGUMENT");
            }
        }
    }


}

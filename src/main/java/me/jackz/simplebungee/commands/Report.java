package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.*;

public class Report extends Command implements Listener {
    private List<String> REASONS = new ArrayList<>();
    private boolean USE_REASON_LIST;
    private SimpleBungee plugin;

    public Report(SimpleBungee plugin) {
        super("report","simplebungee.command.report");
        this.plugin = plugin;
        try {
            Configuration config = plugin.getConfig();
            REASONS = config.getStringList("report.reasons");
            USE_REASON_LIST = config.getBoolean("report.use_reason_list");
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot load config");
        }

    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("You must be a player to use this command"));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            //show gui
            BaseComponent[] tc = new ComponentBuilder("§6Report Help Menu").color(ChatColor.GOLD)
                    .append("\n/report <player>").color(ChatColor.YELLOW)
                    .append(" - start a report").color(ChatColor.GRAY)
                    .append("\n/report status").color(ChatColor.YELLOW)
                    .append(" - get status on your reports").color(ChatColor.GRAY)
                    .create();
            sender.sendMessage(tc);
        }else{
            switch(args[0].toLowerCase()) {
                case "status":
                    sender.sendMessage(new TextComponent("§cYou have no reports active."));
                    break;
                case "_step2":
                    if(args.length >= 3) {
                        String uuid;
                        UUID accused = UUID.fromString(args[1]);
                        String reason = args[1];
                        addReport(accused,reason,player);
                    }else{
                        player.sendMessage(new TextComponent("§cError"));
                    }
                    break;
                default:
                    ProxiedPlayer p = plugin.getProxy().getPlayer(args[0].toLowerCase());
                    if(p != null) {
                        if(USE_REASON_LIST) {
                            TextComponent tc = new TextComponent("§7Please choose a reason:");
                            for (String reason : REASONS) {
                                TextComponent comp_reason = new TextComponent("[" + reason + "]");
                                comp_reason.setColor(ChatColor.AQUA);
                                comp_reason.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/report _step2 " + p.getUniqueId() + " " + reason));
                                comp_reason.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Click to choose").create()));
                                tc.addExtra(" ");
                                tc.addExtra(comp_reason);
                            }
                            player.sendMessage(tc);
                        }else{
                            CHAT_STATUS.put(player.getUniqueId(),p.getUniqueId());
                            player.sendMessage(new TextComponent("§7Please type a reason you want to report §e" + p.getName() + "§7:"));
                        }
                    }else{
                        sender.sendMessage(new TextComponent("§cCould not find the specified player."));
                    }
            }
        }
    }
    private void addReport(UUID Victim, String reason, ProxiedPlayer reporter) {
        reporter.sendMessage(new TextComponent("§cNot implemented."));
    }
    private Map<UUID,UUID> CHAT_STATUS = new HashMap<>();
    @EventHandler
    public void onChat(ChatEvent e) {
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        UUID victim = CHAT_STATUS.get(player.getUniqueId());
        if(victim != null) {
            String reason = e.getMessage();
            addReport(victim,reason,player);
        }
        //int status = CHAT_STATUS.getOrDefault(player.getUniqueId(),0);
        /*if(status == 1) {

        }*/
    }
}

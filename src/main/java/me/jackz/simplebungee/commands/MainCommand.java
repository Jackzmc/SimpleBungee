package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public class MainCommand extends Command {
    private SimpleBungee plugin;
    public MainCommand(SimpleBungee plugin) {
        super("simplebungee","simplebungee.command.simplebungee","sb");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            TextComponent tc = new TextComponent("§6SimpleBungee Help");
            tc.addExtra("\n§e/simplebungee reload §7- reload the config.yml");
            tc.addExtra("\n§e/simplebungee commands §7- view all commands in plugin");
            sender.sendMessage(tc);
            return;
        }
        switch(args[0].toLowerCase()) {
            case "reload": {
                if(sender.hasPermission("simplebungee.command.reload")) {
                    try {
                        plugin.reloadConfig();
                        sender.sendMessage(new TextComponent(ChatColor.GREEN+ "Successfully reloaded the config."));
                    }catch(IOException ex) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "Failed to reload the config. " + ex.getMessage()));
                    }
                    //sender.sendMessage(new TextComponent("§cFeature not implemented"));
                }else{
                    sender.sendMessage(new TextComponent("§cYou don't have permission to use this command."));
                }
                break;
            }
            case "commands": {
                Configuration config = plugin.getConfig();
                TextComponent tc = new TextComponent("§6Commands");
                if(config.getBoolean("commands.lookup")) tc.addExtra("\n§e/lookup <player> §7- get information about a player");
                if(config.getBoolean("commands.ping")) tc.addExtra("\n§e/ping [player] §7- view a players ping to the network");
                if(config.getBoolean("commands.servers")) tc.addExtra("\n§e/servers §7- view all bungeecoord servers with ability to join");
                if(config.getBoolean("commands.uuid")) tc.addExtra("\n§e/uuid [player] §7- get a player's UUID");
                if(config.getBoolean("commands.online")) tc.addExtra("\n§e/online §7- view all online players");
                if(config.getBoolean("commands.friends")) tc.addExtra("\n§e/friends <help/add/list/etc..> §7- friends management system");
                sender.sendMessage(tc);
                break;
            }
            default:
                sender.sendMessage(new TextComponent("§cUnknown argument, try /simplebungee help"));
        }
    }
}

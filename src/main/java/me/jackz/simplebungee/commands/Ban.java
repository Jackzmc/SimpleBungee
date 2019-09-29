package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class Ban extends Command {
    private SimpleBungee plugin;
    public Ban(SimpleBungee plugin) {
        super("ban","simplebungee.command.ban");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}

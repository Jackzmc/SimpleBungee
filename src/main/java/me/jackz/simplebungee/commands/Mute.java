package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class Mute extends Command {
    private SimpleBungee plugin;
    public Mute(SimpleBungee plugin) {
        super("mute","simplebungee.command.mute");
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}

package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Kick extends Command {
    private SimpleBungee plugin;
    private LanguageManager lm;
    public Kick(SimpleBungee plugin) {
        super("kick","simplebungee.command.kick");
        this.plugin = plugin;
        this.lm = SimpleBungee.getLanguageManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean reason_required = plugin.getConfig().getBoolean("moderation.require-reason",true);
        if(args.length == 0) {
            lm.sendMessage(sender,"kick.USAGE1");
        }else{
            if(reason_required && args.length <= 1) {
                lm.sendMessage(sender,"kick.USAGE2");
            }
            ProxiedPlayer player = Util.findPlayer(args[0]);
            if(player != null) {

            }else{
                lm.sendMessage(sender,"kick.PLAYER_NOT_FOUND");
            }
        }
        //usage: /kick <player> [message]
    }
}

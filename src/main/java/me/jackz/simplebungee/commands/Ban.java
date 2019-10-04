package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.utils.Placeholder;
import me.jackz.simplebungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;

public class Ban extends Command {
    private SimpleBungee plugin;
    private LanguageManager lm;
    public Ban(SimpleBungee plugin) {
        super("ban","simplebungee.command.ban");
        this.plugin = plugin;
        this.lm = SimpleBungee.getLanguageManager();
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean reason_required = plugin.getConfig().getBoolean("moderation.require-reason",true);
        if(args.length == 0) {
            lm.sendMessage(sender,"kick.USAGE");
        }else{
            if(reason_required && args.length <= 1) {
                lm.sendMessage(sender,"kick.USAGE2");
                return;
            }
            ProxiedPlayer player = Util.findPlayer(args[0]);
            if(player != null) {
                String reasonText;
                Placeholder reason;
                Placeholder bannedBy = new Placeholder("bannedby",sender.getName());
                Placeholder bannedBy_DisplayName;
                if(args.length >= 2) {
                    String txt = String.join(" ", Arrays.copyOfRange(args,1,args.length));
                    reason = new Placeholder("reason",txt);
                }else{
                    reason = new Placeholder("reason","&o[none]");
                }
                if(sender instanceof ProxiedPlayer) {
                    bannedBy_DisplayName = new Placeholder("bannedby_display",((ProxiedPlayer) sender).getDisplayName());
                }else{
                    bannedBy_DisplayName = new Placeholder("bannedby_display",sender.getName());
                }
                player.disconnect(lm.getTextComponent("ban.REASON",reason,bannedBy,bannedBy_DisplayName));
                lm.sendMessage(sender,"ban.SUCCESS",player);
            }else{
                lm.sendMessage(sender,"core.PLAYER_NOT_FOUND");
            }

        }
    }
}

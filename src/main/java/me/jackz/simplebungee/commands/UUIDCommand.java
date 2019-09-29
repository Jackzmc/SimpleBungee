package me.jackz.simplebungee.commands;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.utils.Placeholder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;

public class UUIDCommand extends Command {
    private final SimpleBungee plugin;
    private final LanguageManager lm;
    public UUIDCommand(SimpleBungee plugin)  {
        super("uuid","simplebungee.command.uuid","simplebungee:uuid");
        this.plugin = plugin;
        this.lm = SimpleBungee.getLanguageManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length > 0) {
            Collection<ProxiedPlayer> players = plugin.getProxy().matchPlayer(args[0]);
            if(players.size() > 0) {
                ProxiedPlayer player = players.iterator().next();
                Placeholder uuid = new Placeholder("uuid",player.getUniqueId());

                lm.sendMessage(sender,"uuid.OTHER",player,uuid);
            }else{
                lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
            }
        }else{
            if(sender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) sender;
                Placeholder uuid = new Placeholder("uuid",player.getUniqueId());

                lm.sendMessage(sender,"uuid.SELF",uuid);
            }else{
                lm.sendMessage(sender,"core.NO_PLAYER_FOUND");
            }
        }
    }
}

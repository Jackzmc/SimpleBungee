package me.jackz.simplebungee.managers;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.utils.Placeholder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public class LanguageManager {
    private Configuration messages;
    private final TextComponent default_component = new TextComponent("§cCould not get value from language file for this message.&r");
    public LanguageManager(SimpleBungee plugin) {
        try {
            messages = plugin.getMessages();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load the language file.  " + e.getMessage());
        }
    }
    //region getString
    public String getRawString(String path) {
        return messages.getString(path);
    }
    public String getString(String path, ProxiedPlayer player) {
        String message = messages.getString(path);
        return ChatColor.translateAlternateColorCodes('&', formatPlaceholders(message+"&r",player));
    }
    public String getString(String path) {
        String message = messages.getString(path);
        return ChatColor.translateAlternateColorCodes('&',message+"&r");
    }
    //endregion
    //region sendMessae
    public void sendMessage(ProxiedPlayer player, String path) {
        TextComponent tc = getTextComponent(path,player);
        player.sendMessage(tc);
    }
    public void sendMessage(ProxiedPlayer player, String path, Placeholder... placeholders) {
        TextComponent tc = getTextComponent(path,player,placeholders);
        player.sendMessage(tc);
    }
    public void sendMessage(CommandSender sender, String path) {
        TextComponent tc = getTextComponent(path);
        sender.sendMessage(tc);
    }
    public void sendMessage(CommandSender sender, String path, Placeholder... placeholders) {
        TextComponent tc = getTextComponent(path,placeholders);
        sender.sendMessage(tc);
    }
    public void sendMessage(CommandSender sender, String path, ProxiedPlayer player, Placeholder... placeholders) {
        TextComponent tc = getTextComponent(path,player,placeholders);
        sender.sendMessage(tc);
    }
    //endregion
    //region getTextComponent
    public TextComponent getTextComponent(String path) {
        String v = messages.getString(path);
        return (v != null) ? new TextComponent(ChatColor.translateAlternateColorCodes('&',v+"&r")) : default_component;
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player) {
        String v = messages.getString(path);
        if(v != null && player != null) {
            v += "&r";
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',formatPlaceholders(v,player)));
        }else{
            return default_component;
        }
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player, Placeholder... placeholders) {
        String v = messages.getString(path);
        if(v != null && player != null) {
            for (Placeholder placeholder : placeholders) {
                v = placeholder.process(v);
            }
            v += "&r";
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',formatPlaceholders(v,player)));
        }else{
            return default_component;
        }
    }
    public TextComponent getTextComponent(String path, Placeholder... placeholders) {
        String v = messages.getString(path);
        if(v != null) {
            for (Placeholder placeholder : placeholders) {
                v = placeholder.process(v);
            }
            v += "&r";
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',v));
        }else{
            return default_component;
        }
    }
    //endregion
    //region internal
    private static String formatPlaceholders(String string, ProxiedPlayer player) {
        if(player != null) {
            String server = player.getServer() != null? player.getServer().getInfo().getName() : "";
            string = string
                    .replaceAll("%player_server%", server)
                    .replaceAll("%player_display%", player.getDisplayName())
                    .replaceAll("%player_name%", player.getName())
                    .replaceAll("%player%", player.getName());
        }
        return ChatColor.translateAlternateColorCodes('&',string+"&r");
    }
}


package me.jackz.simplebungee.managers;

import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.utils.Placeholder;
import me.jackz.simplebungee.utils.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public class LanguageManager {
    private Configuration messages;
    private final TextComponent default_component = new TextComponent("§cCould not get value from language file for this message.");
    public LanguageManager(SimpleBungee plugin) {
        try {
            messages = plugin.getMessages();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load the language file.  " + e.getMessage());
        }
    }
    public String getRawString(String path) {
        return messages.getString(path);
    }
    public String getString(String path, ProxiedPlayer player) {
        String message = messages.getString(path);
        return ChatColor.translateAlternateColorCodes('&', Util.formatPlaceholders(message,player));
    }
    public String getString(String path) {
        String message = messages.getString(path);
        return ChatColor.translateAlternateColorCodes('&',message);
    }
    public TextComponent getTextComponent(String path) {
        String v = messages.getString(path);
        return (v != null) ? new TextComponent(ChatColor.translateAlternateColorCodes('&',v)) : default_component;
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player) {
        String v = messages.getString(path);
        if(v != null) {
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',Util.formatPlaceholders(v,player)));
        }else{
            return default_component;
        }
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player, Placeholder... placeholders) {
        String v = messages.getString(path);
        if(v != null) {
            for (Placeholder placeholder : placeholders) {
                v = placeholder.process(v);
            }
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',Util.formatPlaceholders(v,player)));
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
            return new TextComponent(ChatColor.translateAlternateColorCodes('&',v));
        }else{
            return default_component;
        }
    }
}


package me.jackz.simplebungee.lib;

import me.jackz.simplebungee.SimpleBungee;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.io.IOException;

public class LanguageManager {
    private Configuration messages;
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
    public String getRawString(String path, ProxiedPlayer player) {
        String message = messages.getString(path);
        return Util.formatPlaceholders(message,player);
    }
    public TextComponent getTextComponent(String path) {
        String v = messages.getString(path);
        if(v != null) {
            return new TextComponent(v);
        }else{
            return null;
        }
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player) {
        String v = messages.getString(path);
        if(v != null) {
            return new TextComponent(Util.formatPlaceholders(v,player));
        }else{
            return null;
        }
    }
    public TextComponent getTextComponent(String path, ProxiedPlayer player, Placeholder... placeholders) {
        String v = messages.getString(path);
        if(v != null) {
            for (Placeholder placeholder : placeholders) {
                v = placeholder.process(v);
            }
            return new TextComponent(Util.formatPlaceholders(v,player));
        }else{
            return null;
        }
    }
    public TextComponent getTextComponent(String path, Placeholder... placeholders) {
        String v = messages.getString(path);
        if(v != null) {
            for (Placeholder placeholder : placeholders) {
                v = placeholder.process(v);
            }
            return new TextComponent(v);
        }else{
            return null;
        }
    }
}


package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.events.PlayerEvents;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/*
TODO:
1. reporting
2. bans?
3. parties?
4. friends?
5. staff chat
6. global chat?
[1/2] 7. lookup (name, ip, ping, last login, playtime)
    -> data.yml, store users & their last login, and when online store session
[1.75] 8. global join/leave messages
9. mail utilities
 */

public final class SimpleBungee extends Plugin {



    @Override
    public void onEnable() {
        // Plugin startup logic

        try {
            Configuration config = getConfig();
            PluginManager pm = getProxy().getPluginManager();
            if(config.getBoolean("commands.ping")) pm.registerCommand(this,new PingCommand(this));
            if(config.getBoolean("commands.servers")) pm.registerCommand(this,new Servers(this));
            if(config.getBoolean("commands.online")) pm.registerCommand(this,new OnlineCount(this));
            if(config.getBoolean("commands.uuid")) pm.registerCommand(this,new UUIDCommand(this));
            pm.registerCommand(this,new Friends(this));
            pm.registerCommand(this,new Lookup(this));

            pm.registerListener(this,new PlayerEvents(this));
        } catch (IOException e) {
            getLogger().severe("Could not save or load config.yml. " + e.getMessage());
        }
        try {
            Configuration data = getData();
            //PLAYER_MAP;
        }catch(IOException e) {
            getLogger().severe("Could not save or load data.yml. " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {

        // Plugin shutdown logic
    }

    public Configuration getConfig() throws IOException {
        File config_file = new File(getDataFolder(),"config.yml");
        if(!getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdir();
        }
        if(config_file.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
            } catch (IOException e) {
                getLogger().warning("Could not load config.yml, using default config");
            }
        }
        Configuration config = new Configuration();
        config.set("commands.ping",true);
        config.set("commands.servers",true);
        config.set("commands.online",true);
        config.set("commands.uuid",true);
        config.set("server_shortcuts",new ArrayList<String>());
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, config_file);
        return config;
    }

    public Configuration getData() throws IOException {
        File data_file = new File(getDataFolder(),"data.yml");
        if(data_file.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(data_file);
            } catch (IOException e) {
                getLogger().warning("Could not load data.yml, using defaults");
            }
        }
        Configuration data = new Configuration();
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(data, data_file);
        return data;
    }
    public void saveConfiguration(Configuration c, File file) throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(c, file);
    }
}

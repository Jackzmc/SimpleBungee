package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.events.PlayerEvents;
import me.jackz.simplebungee.lib.PlayerLoader;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
[10%] 1. reporting
[10%] 2. bans?
[10%] 3. parties?
[80%] 4. friends? -> saved on quit, possibly save loop;
        NEED friend join/leave message? (disable if global on)
5. staff chat
[10%] 6. global chat?
[DONE] 7. lookup (name, ip, ping, last login, playtime) -> last online info stored on player quit
    -> data.yml, store users & their last login, and when online store session
[DONE] 8. global join/leave messages
9. mail utilities

10. save loop (run tasks to save, ex: friends)
 */

public final class SimpleBungee extends Plugin {
    private Friends friends;
    private PlayerLoader playerLoader;
    public Configuration data;

    @Override
    public void onEnable() {
        // Plugin startup logic
        friends = new Friends(this);
        playerLoader = new PlayerLoader(this);
        try {
            data = loadData();
            //PLAYER_MAP;
        }catch(IOException e) {
            getLogger().severe("Could not save or load data.yml. " + e.getMessage());
        }
        try {
            Configuration config = getConfig();
            PluginManager pm = getProxy().getPluginManager();
            if(config.getBoolean("commands.ping")) pm.registerCommand(this,new PingCommand(this));
            if(config.getBoolean("commands.servers")) pm.registerCommand(this,new Servers(this));
            if(config.getBoolean("commands.online")) pm.registerCommand(this,new OnlineCount(this));
            if(config.getBoolean("commands.uuid")) pm.registerCommand(this,new UUIDCommand(this));
            pm.registerCommand(this,new MainCommand(this));
            if(config.getBoolean("commands.friends")) {
                friends.LoadFriendsList();
                pm.registerCommand(this, friends);
            }
            if(config.getBoolean("commands.lookup")) pm.registerCommand(this,new Lookup(this));
            pm.registerListener(this,new PlayerEvents(this));

        } catch (IOException e) {
            getLogger().severe("Could not save or load config.yml. " + e.getMessage());
        }

    }

    @Override
    public void onDisable() {
        try {
            friends.SaveFriendsList();
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                playerLoader.save(player);
            }
        } catch (IOException e) {
            getLogger().severe("Could not save friend list and friend requests.");
        }
        // Plugin shutdown logic
    }
    public PlayerLoader getPlayerLoader() {
        return playerLoader;
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
        config.set("commands.friends",true);
        config.set("commands.report",true);
        config.set("commands.lookup",true);
        config.set("connection-messages.bungee",true);
        config.set("connection-messages.serverswitch",true);
        config.set("server_shortcuts",new ArrayList<String>());
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, config_file);
        return config;
    }

    private Configuration loadData() throws IOException {
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
    public void saveConfiguration(Configuration c, File file ) throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(c, file);
    }
    public void saveData() throws IOException {
        File file = new File(getDataFolder(),"data.yml");
        if(data == null) throw new NullPointerException("Data Configuration is null");
        saveConfiguration(data,file);
    }
}

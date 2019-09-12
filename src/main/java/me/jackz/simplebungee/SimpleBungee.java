package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.events.PlayerEvents;
import me.jackz.simplebungee.lib.ConfigProperty;
import me.jackz.simplebungee.lib.PlayerLoader;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
TODO:
moved to
https://trello.com/b/9v1N8q0l/simplebungee
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
            if(config.getBoolean("commands.ping"))    pm.registerCommand(this,new PingCommand(this));
            if(config.getBoolean("commands.servers")) pm.registerCommand(this,new Servers(this));
            if(config.getBoolean("commands.online"))  pm.registerCommand(this,new OnlineCount(this));
            if(config.getBoolean("commands.uuid"))    pm.registerCommand(this,new UUIDCommand(this));
            //if(config.getBoolean("commands.report"))  pm.registerCommand(this,new Report(this));
            if(config.getBoolean("commands.global"))  {
                Global global = new Global(this);
                pm.registerCommand(this,global);
                pm.registerListener(this,global);
            }
            if(config.getBoolean("commands.lookup"))  pm.registerCommand(this,new Lookup(this));
            pm.registerCommand(this,new MainCommand(this));
            if(config.getBoolean("commands.friends")) {
                friends.LoadFriendsList();
                pm.registerCommand(this, friends);
            }
            pm.registerListener(this,new PlayerEvents(this));

        } catch (IOException e) {
            getLogger().severe("Could not save or load config.yml. " + e.getMessage());
        }

    }

    @Override
    public void onDisable() {
        try {
            friends.SaveFriendsList();
            boolean kick_players_on_shutdown = getConfig().getBoolean("kick-players-on-shutdown",false);
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                playerLoader.save(player);
                //use messages.yml later
                if(kick_players_on_shutdown) player.disconnect(new TextComponent("§cServer is shutting down"));
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
        Configuration config = null;
        if(config_file.exists()) {
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
            } catch (IOException e) {
                getLogger().warning("Could not load config.yml, using default config");
            }
        }
        if(config == null) config = new Configuration();
        ConfigProperty cp = new ConfigProperty(config);
        List<String> REPORT_REASONS = Arrays.asList("Griefing","Harassment","Hacking");

        cp.addDefault("commands.ping",true);
        cp.addDefault("commands.servers",true);
        cp.addDefault("commands.online",true);
        cp.addDefault("commands.global",true);
        cp.addDefault("commands.uuid",true);
        cp.addDefault("commands.friends",true);
        //cp.addDefault("commands.report",true);
        cp.addDefault("commands.lookup",true);
        cp.addDefault("report.use_reason_list",true);
        cp.addDefault("report.reasons",REPORT_REASONS);
        cp.addDefault("formats.global","&9GLOBAL %servername%> &e%displayname%:&r");
        cp.addDefault("connection-messages.bungee",true);
        cp.addDefault("connection-messages.serverswitch",true);
        cp.addDefault("connection-messages.friends",true);
        cp.addDefault("kick-players-on-shutdown",false);
        cp.addDefault("server_shortcuts",new ArrayList<String>());
        cp.addDefault("show_restricted_servers",false);
        config = cp.getConfig();
        saveConfiguration(config,config_file);
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

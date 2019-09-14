package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.events.PlayerEvents;
import me.jackz.simplebungee.lib.LanguageManager;
import me.jackz.simplebungee.lib.PlayerLoader;
import me.jackz.simplebungee.lib.ServerShortcut;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


public final class SimpleBungee extends Plugin {
    private Friends friends;
    private PlayerLoader playerLoader;
    public Configuration data;
    private LanguageManager languageManager;

    private final static String LATEST_CONFIG_VERSION = "1.0";

    @Override
    public void onEnable() {
        // Plugin startup logic
        friends = new Friends(this);
        playerLoader = new PlayerLoader(this);
        try {
            if(!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdir();
            }
            data = loadData();
            //PLAYER_MAP;
        }catch(IOException e) {
            getLogger().severe("Could not save or load data.yml. " + e.getMessage());
        }
        try {
            saveResource("config.yml");
            saveResource("english.yml");
            languageManager = new LanguageManager(this);
        }catch(IOException ex) {
            getLogger().severe("Failed to copy resources " + ex.getMessage());
        }
        try {
            Configuration config = getConfig();
            String version = config.getString("config-version");
            if(version != null || !version.equalsIgnoreCase(LATEST_CONFIG_VERSION )) {
                getLogger().warning("The config file has been updated since last time. Please delete your config.yml to upgrade it to the latest version");
            }

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
            if(config.contains("server_shortcuts")) {
                Configuration servers = config.getSection("server_shortcuts");
                ServerShortcut.setupShortcuts(this, servers);
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
                //use english.yml later
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
        if(config_file.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
            } catch (IOException e) {
                getLogger().warning("Could not load config.yml, using default config");
            }
        }
        saveResource("config.yml");
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
    }
    public Configuration getMessages() throws IOException {
        String lang_file = getConfig().getString("language-file","english.yml");
        File messages_file = new File(getDataFolder(), lang_file);
        if(messages_file.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(messages_file);
            } catch (IOException e) {
                getLogger().warning("Could not load english.yml, using default english.yml");
            }
        }
        saveResource("english.yml");
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(messages_file);
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
    public void saveData() throws IOException {
        File file = new File(getDataFolder(),"data.yml");
        if(data == null) throw new NullPointerException("Data Configuration is null");
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(data,file);
    }
    private void saveResource(String filename) throws IOException {
        File file = new File(getDataFolder(),filename);
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream(filename)) {
                Files.copy(in, file.toPath());
            }
        }

    }
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}

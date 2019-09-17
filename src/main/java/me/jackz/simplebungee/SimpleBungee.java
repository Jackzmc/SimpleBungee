package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.listeners.PlayerEvents;
import me.jackz.simplebungee.managers.FriendsManager;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.managers.PlayerLoader;
import me.jackz.simplebungee.utils.ServerShortcut;
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
    private PlayerLoader playerLoader;
    public Configuration data;
    private LanguageManager languageManager;
    private FriendsManager friendsManager;

    private final static String LATEST_CONFIG_VERSION = "1.0";

    @Override
    public void onEnable() {
        /* load data */
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
        /*load resources & language manager */
        try {
            saveResource("config.yml");
            saveResource("english.yml");
            languageManager = new LanguageManager(this);
        }catch(IOException ex) {
            getLogger().severe("Failed to copy resources " + ex.getMessage());
        }
        /* load main config and commands */
        try {
            playerLoader = new PlayerLoader(this);
            this.friendsManager = new FriendsManager(this);

            Configuration config = getConfig();
            String version = config.getString("config-version","0");
            if(version == null || !version.equalsIgnoreCase(LATEST_CONFIG_VERSION )) {
                String message = String.format("Your config file is version %s, the latest is %s. Please upgrade the file by deleting the config.yml.", version,LATEST_CONFIG_VERSION);
                getLogger().warning(message);
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
                friendsManager.loadFriendsList();
                pm.registerCommand(this, new Friends(this));
            }
            if(config.contains("server_shortcuts")) {
                Configuration servers = config.getSection("server_shortcuts");
                ServerShortcut.setupShortcuts(this, servers);
            }

            pm.registerListener(this,new PlayerEvents(this));

        } catch (IOException e) {
            getLogger().severe("A critical error while loading the plugin has occurred. " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            friendsManager.saveFriendsList();
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                playerLoader.save(player);
                //use english.yml later
            }
        } catch (IOException e) {
            getLogger().severe("Could not save friend list and friend requests.");
        }
        // Plugin shutdown logic
    }
    public PlayerLoader getPlayerLoader() {
        return playerLoader;
    }
    public FriendsManager getFriendsManager() { return friendsManager; }
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

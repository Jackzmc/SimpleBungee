package me.jackz.simplebungee;

import me.jackz.simplebungee.commands.*;
import me.jackz.simplebungee.listeners.PlayerEvents;
import me.jackz.simplebungee.managers.FriendsManager;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.managers.PlayerLoader;
import me.jackz.simplebungee.utils.ServerShortcut;
import me.jackz.simplebungee.utils.Version;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public final class SimpleBungee extends Plugin {
    private PlayerLoader playerLoader;
    public Configuration data;
    private Configuration config;
    private LanguageManager languageManager;
    private FriendsManager friendsManager;

    private final static Version LATEST_CONFIG_VERSION = new Version("1.0");

    @Override
    public void onEnable() {
        /*load resources & language manager */
        try {
            saveDefaultResource("config.yml",false);
            saveDefaultResource("english.yml",true);
        }catch(IOException ex) {
            getLogger().severe("Failed to copy resources " + ex.getMessage());
        }
        /* load data & config */

        try {
            if(!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdir();
            }
            config = loadConfig();
            data = loadData();
            //PLAYER_MAP;
        }catch(IOException e) {
            getLogger().severe("Error occurred while loading configuration or data files. " + e.getMessage());
        }

        /* load managers, commands, and listeners */
        languageManager = new LanguageManager(this);
        playerLoader = new PlayerLoader(this);
        this.friendsManager = new FriendsManager(this);

        String config_version = config.getString("config-version","0");
        Version current_config_version = new Version(config_version);
        //check if the current config version is less than LATEST_CONFIG_VERSION
        if(config_version == null || current_config_version.compareTo(LATEST_CONFIG_VERSION) < 0) {
            String message = String.format("Your config file is version %s, the latest is %s. Please upgrade the file by deleting the config.yml.", config_version,LATEST_CONFIG_VERSION);
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
    //region getters
    public PlayerLoader getPlayerLoader() {
        return playerLoader;
    }
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    public FriendsManager getFriendsManager() {
        return friendsManager;
    }
    public Configuration getConfig() {
        return this.config;
    }
    //endregion
    //region configuration
    private Configuration loadConfig() throws IOException {
        File config_file = new File(getDataFolder(),"config.yml");
        if(config_file.exists()) {
            try {
                return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
            } catch (IOException e) {
                getLogger().warning("Could not load config.yml, using default config");
            }
        }
        saveDefaultResource("config.yml",false);
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config_file);
    }

    public void reloadConfig() throws IOException {
        this.config = loadConfig();
    }
    //endregion
    //region data.yml
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
    //endregion
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
        saveDefaultResource("english.yml",true);
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(messages_file);
    }
    private void saveDefaultResource(String filename, boolean force) throws IOException {
        Path file = Paths.get(getDataFolder().getAbsolutePath(),filename);
        if (force || !Files.exists(file)) {
            try (InputStream in = getResourceAsStream(filename)) {
                //won't replace existing because of File.exists, unless force
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}

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
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.MetricsLite;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;


public final class SimpleBungee extends Plugin {
    private PlayerLoader playerLoader;
    public Configuration data;
    private Configuration config;
    private static LanguageManager languageManager;
    private FriendsManager friendsManager;
    private Notes notes;

    private final static Version LATEST_CONFIG_VERSION = new Version("1.1");
    private final static String UPDATE_CHECK_URL = "https://api.spigotmc.org/legacy/update.php?resource=71230";
    private final long AUTOSAVE_INTERVAL_MIN = 60;

    private String latest_update = null;
    private String plugin_version = null;

    @Override
    public void onEnable() {
        /*load resources & language manager */
        try {
            saveDefaultResource("config.yml",false);
            saveDefaultResource("english.yml",true);
        }catch(IOException ex) {
            getLogger().severe("Failed to copy resources. " + ex.getMessage());
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
            getLogger().severe("Error occurred while loading plugin data & configuration. " + e.getMessage());
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
        //update check
        if(config.getBoolean("check-for-updates",true)) {
            PluginDescription pd = getDescription();
            plugin_version = pd.getVersion();
            getLatestUpdate();
            if(latest_update != null) {
                getLogger().info("There is a new version of SimpleBungee. Current: " + plugin_version + ", Latest: " + latest_update);
            }
        }
        //load commands & events
        loadCommands();
        loadEvents();

        //set up autosave
        getProxy().getScheduler().schedule(this, () -> {
            try {
                saveData();
            } catch (IOException e) {
                getLogger().warning("Exception during plugin data autosave. " + e.getMessage());
            }
        },0L, AUTOSAVE_INTERVAL_MIN, TimeUnit.MINUTES);

        //finally, load metrics
        if(config.getBoolean("metrics-enabled",true)) {
            MetricsLite metrics = new MetricsLite(this);
            if(metrics.isEnabled()) {
                getLogger().info("bStats metrics has been enabled.");
            }
        }


    }

    @Override
    public void onDisable() {
        getProxy().getScheduler().cancel(this);
        try {
            if(friendsManager != null) friendsManager.saveFriendsList();
            if(notes != null) notes.saveNotes();
            for (ProxiedPlayer player : getProxy().getPlayers()) {
                playerLoader.save(player);
                //use english.yml later
            }
            saveData();
        } catch (IOException e) {
            getLogger().severe("An exception occurred while saving plugin data. ");
            e.printStackTrace();
        }
        // Plugin shutdown logic
    }

    private void loadCommands() {
        PluginManager pm = getProxy().getPluginManager();
        if(config.getBoolean("commands.ping",true))    pm.registerCommand(this,new PingCommand(this));
        if(config.getBoolean("commands.servers",true)) pm.registerCommand(this,new Servers(this));
        if(config.getBoolean("commands.online",true))  pm.registerCommand(this,new OnlineCount(this));
        if(config.getBoolean("commands.uuid",true))    pm.registerCommand(this,new UUIDCommand(this));
        if(config.getBoolean("commands.notes",true))    {
            notes = new Notes(this);
            notes.loadNotes();
            pm.registerCommand(this,notes);
        }
        //if(config.getBoolean("commands.report"))  pm.registerCommand(this,new Report(this));
        if(config.getBoolean("commands.global"))  {
            Global global = new Global(this);
            pm.registerCommand(this,global);
            pm.registerListener(this,global);
        }
        if(config.getBoolean("commands.lookup",true))  pm.registerCommand(this,new Lookup(this));
        pm.registerCommand(this,new MainCommand(this));
        if(config.getBoolean("commands.friends",true)) {
            friendsManager.loadFriendsList();
            pm.registerCommand(this, new Friends(this));
        }
        if(config.contains("server_shortcuts")) {
            Configuration servers = config.getSection("server_shortcuts");
            ServerShortcut.setupShortcuts(this, servers);
        }

    }
    private void loadEvents() {
        PluginManager pm = getProxy().getPluginManager();
        pm.registerListener(this,new PlayerEvents(this));
    }

    //region getters
    public PlayerLoader getPlayerLoader() {
        return playerLoader;
    }
    public static LanguageManager getLanguageManager() {
        return languageManager;
    }
    public static LanguageManager lm() { return languageManager; }
    public FriendsManager getFriendsManager() {
        return friendsManager;
    }
    public Configuration getConfig() {
        return this.config;
    }
    public String getLatestUpdate() {
        if(latest_update != null) {
            return latest_update;
        }else{
            try {
                return fetchLatestUpdate();
            }catch (Exception ex) {
                getLogger().warning("Update Checker ran into an error while fetching latest update." );
                return null;
            }
        }
    }
    public String getVersion() {
        return plugin_version;
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
    public String fetchLatestUpdate() throws IOException {
        PluginDescription pd = getDescription();
        try {
            Version current = new Version(pd.getVersion());
            // create the url
            URL url = new URL(UPDATE_CHECK_URL);
            // open the url stream, wrap it an a few "readers"
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // write the output to stdout
            String line = reader.readLine();
            Version latest = new Version(line);

            if(latest.compareTo(current) >= 0) {
                this.latest_update = line;
                return line;
            }

            // close our reader
            reader.close();


        }catch(IllegalArgumentException ignored) {

        }
        return null;
    }
}

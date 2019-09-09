package me.jackz.simplebungee.lib;

import net.md_5.bungee.config.Configuration;

public class ConfigProperty {
    private Configuration config;
    public ConfigProperty(Configuration config) {
        this.config = config;
    }
    public void addDefault(String path, Object v) {
        if(!config.contains(path)) {
            config.set(path,v);
        }
    }
    public Configuration getConfig() {
        return config;
    }
}

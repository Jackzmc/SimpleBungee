package me.jackz.simplebungee;

import me.jackz.simplebungee.utils.OfflinePlayerStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataStore {
    public static Map<UUID, OfflinePlayerStore> PLAYER_MAP = new HashMap<>();
    public static final Map<UUID,Long> CURRENT_PLAYTIME_STORE = new HashMap<>();

}

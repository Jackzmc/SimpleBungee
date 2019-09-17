package me.jackz.simplebungee.utils;

import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {
    public static String getTimeBetween(long seconds1, long seconds2) {
        long diff = seconds2 - seconds1;
        long diffSeconds = diff % 60;
        long diffMinutes = diff / (60) % 60;
        long diffHours = diff / (60 * 60) % 24;
        long diffDays = diff / (24 * 60 * 60);

        List<String> strings = new ArrayList<>();
        strings.add(diffSeconds + " seconds");
        if(diffMinutes > 0) strings.add(diffMinutes + " minutes");
        if(diffHours > 0) strings.add(diffHours + " hours");
        if(diffDays > 0) strings.add(diffDays + " days");
        Collections.reverse(strings);
        return String.join(", ", strings);
    }
    public static String getSectioned(String... keys) {
        return String.join(".",keys);
    }
    public static String getServerName(Server server) {
        if(server == null) return null;
        return (server.isConnected()) ? server.getInfo().getName() : null;
    }
}

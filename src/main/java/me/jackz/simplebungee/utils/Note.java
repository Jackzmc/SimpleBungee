package me.jackz.simplebungee.utils;

public class Note {
    private String key = null;
    private String text;
    private long created;

    public Note(String text) {
        this.text = text;
    }
    public Note(String key, String text, long created) {
        this.setKey(key);
        this.text = text;
        this.setCreated(created);
    }

    public String getText() {
        return text;
    }

    public String getKey() {
        return key;
    }
    public boolean hasKey() {
        return this.key != null && !this.key.equals("");
    }

    public long getCreated() {
        return created;
    }
    public void setCreated(long created) {
        this.created = created;
    }

    public String getCreatedFormatted() {
        long now = System.currentTimeMillis() / 1000;;
        return Util.getTimeBetween(created,now) + " ago";
    }

    public void setKey(String key) {
        this.key = key;
    }
}

package me.jackz.simplebungee.utils;

public class Placeholder {
    private final String name;
    private final Object value;
    public Placeholder(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String process(String input) {
        return input.replaceAll("%" + name + "%",value.toString());
    }
}

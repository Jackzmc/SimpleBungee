package me.jackz.simplebungee.lib;

public class Placeholder {
    private String name;
    private Object value;
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

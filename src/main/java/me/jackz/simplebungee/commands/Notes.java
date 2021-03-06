package me.jackz.simplebungee.commands;

import javafx.util.Pair;
import me.jackz.simplebungee.SimpleBungee;
import me.jackz.simplebungee.managers.LanguageManager;
import me.jackz.simplebungee.utils.Note;
import me.jackz.simplebungee.utils.Placeholder;
import me.jackz.simplebungee.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Notes extends Command {
    private final SimpleBungee plugin;
    private final LanguageManager lm;
    private final Map<UUID, List<Note>> NOTES = new HashMap<>();

    private final int PAGE_SIZE = 15;

    public Notes(SimpleBungee plugin) {
        super("notes","simplebungee.command.notes","note","simplebungee:notes","simplebungee:note");
        this.plugin = plugin;
        this.lm = SimpleBungee.getLanguageManager();
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
                lm.sendMessage(sender,"notes.HELP",player);
                return;
            }
            switch(args[0].toLowerCase()) {
                case "add":
                case "new": {
                    if(args.length > 1) {
                        long now = System.currentTimeMillis() / 1000;
                        String text = String.join(" ",Arrays.copyOfRange(args, 1, args.length));
                        Note note = new Note(text);
                        note.setCreated(now);

                        int index = addToList(player,note);
                        Placeholder id = new Placeholder("id",index);
                        lm.sendMessage(sender,"notes.ADD_SUCCESS",id);
                    }else{
                        lm.sendMessage(sender,"notes.MISSING_TEXT");
                    }
                    break;
                }
                case "list": {
//                    if(!NOTES.containsKey(player.getUniqueId())) {
//                        player.sendMessage(lm.getTextComponent("notes.NO_NOTES"));
//                        return;
//                    }
                    List<Note> playerNotes = NOTES.getOrDefault(player.getUniqueId(), new ArrayList<>());
                    if(playerNotes.size() == 0) {
                        player.sendMessage(lm.getTextComponent("notes.NO_NOTES"));
                        return;
                    }
                    //get page number & starting index
                    String pageno = args.length > 1 ? args[1] : null;
                    int page = Util.parseIntegerDefault(pageno,1);
                    int totalpages = (int) Math.ceil((double)playerNotes.size() / PAGE_SIZE);
                    if(page <= 1) page = 1;
                    if(page > totalpages) page = totalpages;

                    int starting_index = PAGE_SIZE * (page-1);
                    int ending_index = (PAGE_SIZE * (page)) - 1;

                    //setup placeholders for basically only notes.LIST_HEADING
                    Placeholder ph_count = new Placeholder("count",playerNotes.size());
                    Placeholder ph_page = new Placeholder("page",page);
                    Placeholder ph_total_pages = new Placeholder("total",totalpages);
                    Placeholder ph_page_size = new Placeholder("page_size",PAGE_SIZE);
                    player.sendMessage(lm.getTextComponent("notes.LIST_HEADING",ph_count,ph_page,ph_total_pages,ph_page_size));

                    //get all notes from page start, only loop if less than PAGE_SIZE or total notes
                    for (int i = starting_index; i < ending_index && i < playerNotes.size(); i++) {
                        Note note = playerNotes.get(i);
                        Placeholder ph_id = new Placeholder("id",i+1);
                        Placeholder ph_created = new Placeholder("created",note.getCreatedFormatted());
                        Placeholder ph_text = new Placeholder("text",note.getText());
                        if(note.hasKey()) {
                            Placeholder name = new Placeholder("name",note.getKey());
                            player.sendMessage(lm.getTextComponent("notes.LIST_NOTE_NAMED",ph_id,ph_created,ph_text,name));
                        }else{
                            player.sendMessage(lm.getTextComponent("notes.LIST_NOTE",ph_id,ph_created,ph_text));
                        }
                    }
                    if(page < totalpages) {
                        Placeholder ph_next_page = new Placeholder("next_page",page+1);
                        TextComponent TC_nextpage = lm.getTextComponent("notes.LIST_MORE_NOTES",ph_page,ph_total_pages,ph_next_page);
                        TC_nextpage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/note list " + page+1 ));
                        TC_nextpage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Click to see next page").create()));
                        player.sendMessage(TC_nextpage);
                    }
                    break;
                }
                case "remove": {
                    Pair<Integer,Note> pair = findNote(player,args[1]);
                    if(pair != null) {
                        List<Note> playerNotes = NOTES.get(player.getUniqueId());
                        playerNotes.remove(pair.getKey().intValue());
                        //Placeholder name = new Placeholder("name",pair.getValue().getKey());
                        Placeholder id = new Placeholder("id",pair.getKey());
                        lm.sendMessage(sender,"notes.REMOVE_SUCCESS",player,id);
                    }else{
                        lm.sendMessage(sender,"notes.NOT_FOUND",player);
                    }
                    break;
                }
                case "setkey":
                case "setname": {
                    Pair<Integer,Note> pair = findNote(player,args[1]);
                    if(pair != null) {
                        List<Note> playerNotes = NOTES.get(player.getUniqueId());
                        Note note = pair.getValue();
                        note.setKey(args[1]);
                        playerNotes.set(pair.getKey(),note);
                        Placeholder name = new Placeholder("name",note.getKey());
                        Placeholder id = new Placeholder("id",pair.getKey());

                        lm.sendMessage(sender,"notes.RENAME_SUCCESS",player,name,id);
                    }else{
                        lm.sendMessage(sender,"notes.NOT_FOUND",player);
                    }
                    break;
                }
                default:
                    Pair<Integer,Note> pair = findNote(player,args[0]);
                    if(pair != null) {
                        Note note = pair.getValue();
                        int id = pair.getKey();
                        long now = System.currentTimeMillis() / 1000;

                        TextComponent tc = new net.md_5.bungee.api.chat.TextComponent("§6Note Info");
                        tc.addExtra("\n§eKey: §r");
                        if(note.hasKey()) {
                            tc.addExtra(note.getKey());
                        }else{
                            TextComponent set_key = new TextComponent("§c[none]");
                            set_key.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/note setname " + id + " "));
                            set_key.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Set a name for this note.").create()));
                            tc.addExtra(set_key);
                        }
                        tc.addExtra("\n§eCreated: §r" + note.getCreatedFormatted());
                        tc.addExtra("\n§7" + note.getText());
                        player.sendMessage(tc);
                    }else{
                        lm.sendMessage(sender,"notes.NOT_FOUND",player);
                    }
                    ///notes <note id or key/list/remove/name>
                    //lm.sendMessage(sender,"notes.USAGE",player));
            }
        }else{
            lm.sendMessage(sender,"PLAYER_ONLY");
        }
    }
    private Pair<Integer,Note> findNote(ProxiedPlayer player, String key) {
        Integer number = tryParse(key);
        List<Note> playerNotes = NOTES.getOrDefault(player.getUniqueId(),new ArrayList<>());
        if(playerNotes.size() == 0) return null;
        for (int i = 0; i < playerNotes.size(); i++) {
            Note note = playerNotes.get(i);
            if(number != null && number == i) return new Pair<>(i, note);
            if(note.hasKey() && note.getKey().equalsIgnoreCase(key)) return new Pair<>(i, note);
        }
        return null;
    }
    private int addToList(ProxiedPlayer player, Note note) {
        if (!NOTES.containsKey(player.getUniqueId())) {
            NOTES.put(player.getUniqueId(), new ArrayList<>());
        }
        List<Note> playerNotes = NOTES.get(player.getUniqueId());
        playerNotes.add(note);
        //NOTES.put(player.getUniqueId(),playerNotes);
        return playerNotes.size();
    }
    private static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    //todo: possibly just use data.yml, no harm?
    public void loadNotes() {
        File data_file = new File(plugin.getDataFolder(),"notes.yml");
        if(data_file.exists()) {
            migrateNotes(data_file); //migrate notes, and load from here
            return;
        }
        Configuration notes = plugin.data.getSection("notes");
        for (String uuidkey : notes.getKeys()) {
            List<Note> noteList = new ArrayList<>();
            for(String id : notes.getSection(uuidkey).getKeys()) {
                String _key = notes.getString(Util.getSectioned(uuidkey,id,"key"),null);
                String _text = notes.getString(Util.getSectioned(uuidkey,id,"text"),null);
                long _created = notes.getLong(Util.getSectioned(uuidkey,id,"created"));
                if(_key != null && _key.equals("")) _key = null;
                //ignore blank text
                if(_text == null || _text.equals("")) {
                    continue;
                }

                Note note = new Note(_key,_text,_created);
                noteList.add(note);
            }
            UUID uuid = UUID.fromString(uuidkey);
            NOTES.put(uuid,noteList);
        }
    }
    private void migrateNotes(File file) {
        Configuration c = null;
        try {
            c = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            for (String uuidkey : c.getKeys()) {
                List<Note> noteList = new ArrayList<>();
                for(String id : c.getSection(uuidkey).getKeys()) {
                    String _key = c.getString(Util.getSectioned(uuidkey,id,"key"),null);
                    String _text = c.getString(Util.getSectioned(uuidkey,id,"text"),null);
                    long _created = c.getLong(Util.getSectioned(uuidkey,id,"created"));
                    if(_key != null && _key.equals("")) _key = null;
                    //ignore blank text
                    if(_text == null || _text.equals("")) {
                        continue;
                    }

                    Note note = new Note(_key,_text,_created);
                    noteList.add(note);
                }
                UUID uuid = UUID.fromString(uuidkey);
                NOTES.put(uuid,noteList);
            }
            saveNotes();
            file.delete();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not migrate notes.yml to data.yml. File has been untouched.");
        }
    }
    public void saveNotes() {
        Configuration section = plugin.data.getSection("notes");
        for (Map.Entry<UUID, List<Note>> uuidListEntry : NOTES.entrySet()) {
            UUID key = uuidListEntry.getKey();
            List<Note> notes = uuidListEntry.getValue();
            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                section.set(key + "." + i + ".text",note.getText());
                section.set(key + "." + i + ".key",note.getKey());
                section.set(key + "." + i + ".created",note.getCreated());
            }
        }
        //don't need to save, plugin will handle it
    }
}

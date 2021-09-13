package com.minepunks.mpc.treecmds;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.minepunks.mpc.MPCommands;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.*;

public class TreeCommandManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(TreeCommand.class, new TreeCommand.JsonSerializer()).create();
    private final File commandsFile;
    private final Map<String, TreeCommand> rootCommands;

    public TreeCommandManager(File dataFolder) {
        this.rootCommands = new HashMap<>();
        this.commandsFile = new File(dataFolder, "commands.json");
        this.load();
    }

    public String[] getCommandNames() {
        return this.rootCommands.keySet().toArray(new String[0]);
    }

    private void updateFile() {
        try(FileWriter fileWriter = new FileWriter(this.commandsFile)) {
            JsonArray jsonRootCommands = new JsonArray();
            this.rootCommands.forEach((name, command) -> {
                JsonObject jsonRootCommand = new JsonObject();
                jsonRootCommand.addProperty("name", name);
                jsonRootCommand.add("rootCommand", gson.toJsonTree(command, TreeCommand.class));
                jsonRootCommands.add(jsonRootCommand);
            });
            fileWriter.write(gson.toJson(jsonRootCommands));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try(FileReader fileReader = new FileReader(this.commandsFile)) {
            JsonArray jsonRootCommands = JsonParser.parseReader(fileReader).getAsJsonArray();
            this.loadCommands(jsonRootCommands);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCommands(JsonArray jsonRootCommands) {

        this.rootCommands.clear();
        for (JsonElement jsonElement : jsonRootCommands) {
            JsonObject jsonRootCommand = jsonElement.getAsJsonObject();
            String name = jsonRootCommand.get("name").getAsString();
            JsonObject jsonTreeCommand = jsonRootCommand.get("rootCommand").getAsJsonObject();
            TreeCommand rootCommand = gson.fromJson(jsonTreeCommand, TreeCommand.class);
            this.rootCommands.put(name, rootCommand);
        }
    }

    public boolean addCommand(String commandName) {
        if(this.hasCommand(commandName)) {
            return false;
        }

        this.rootCommands.put(commandName, new TreeCommand());
        this.updateFile();
        return true;
    }

    public boolean removeCommand(String commandName) {
        if(!this.hasCommand(commandName)) {
            return false;
        }

        this.rootCommands.remove(commandName);
        this.updateFile();
        return true;
    }

    public boolean hasCommand(String name) {
        return this.rootCommands.containsKey(name);
    }

    public boolean renameCommand(String oldName, String newName) {
        if(!this.hasCommand(oldName)) {
            return false;
        }

        TreeCommand command = this.rootCommands.remove(oldName);
        this.rootCommands.put(newName, command);
        this.updateFile();
        return true;
    }

    public void runRootCommand(String name) {
        TreeCommand rootCommand = this.rootCommands.get(name);
        Bukkit.getScheduler().runTaskAsynchronously(MPCommands.INSTANCE, () -> rootCommand.execute(Bukkit.getConsoleSender()));
    }

}

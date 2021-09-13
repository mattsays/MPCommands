package com.minepunks.mpc;

import com.minepunks.mpc.commands.MPCommandManager;
import com.minepunks.mpc.treecmds.TreeCommandManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public final class MPCommands extends JavaPlugin {

    public static MPCommands INSTANCE;
    public static Logger LOGGER;

    private TreeCommandManager commandManager;

    private BukkitAudiences adventure;

    public @NotNull BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;
        LOGGER = this.getLogger();
        this.adventure = BukkitAudiences.create(this);

        if(!new File(this.getDataFolder(), "commands.json").exists()) {
            this.saveResource("commands.json", false);
        }

        this.commandManager = new TreeCommandManager(this.getDataFolder());
        LOGGER.info("MPCommandManager started.");

        MPCommandManager mpCommandManager = new MPCommandManager(this.commandManager);
        PluginCommand command = this.getCommand("mpc");
        command.setExecutor(mpCommandManager);
        command.setTabCompleter(mpCommandManager);
        LOGGER.info("MPCommandManager command registered.");

        LOGGER.info("Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        this.commandManager = null;

        LOGGER.info("Plugin disabled successfully!");

        LOGGER = null;
        INSTANCE = null;
    }
}

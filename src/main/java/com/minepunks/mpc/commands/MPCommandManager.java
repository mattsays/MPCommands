package com.minepunks.mpc.commands;

import com.minepunks.mpc.MPCommands;
import com.minepunks.mpc.treecmds.TreeCommandManager;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MPCommandManager extends Command {

    private final TreeCommandManager treeCommandManager;

    public MPCommandManager(TreeCommandManager treeCommandManager) {

        this.treeCommandManager = treeCommandManager;

        this.addSubCommand("add", new Command() {
            @Override
            protected void execute(CommandSender commandSender, String[] arguments) {
                Audience commandSenderAudience = MPCommands.INSTANCE.adventure().sender(commandSender);

                if(arguments.length != 1) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid command syntax. Use /mpc add [commandName]](color=red)"));
                    return;
                }

                String commandName = arguments[0];

                if(commandName == null || commandName.isEmpty()) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid MPCommand name.](color=red)"));
                    return;
                }

                if(!treeCommandManager.addCommand(commandName)) {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand already exists.](color=red)"));
                } else {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand added successfully](color=green)"));
                }
            }

            @Override
            protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
                return Optional.empty();
            }

            @Override
            protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
                return commandSender.hasPermission("com.minepunks.mpc.add");
            }
        });
        this.addSubCommand("remove", new Command() {
            @Override
            protected void execute(CommandSender commandSender, String[] arguments) {
                Audience commandSenderAudience = MPCommands.INSTANCE.adventure().sender(commandSender);

                if(arguments.length != 1) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid command syntax. Use /mpc remove [commandName]](color=red)"));
                    return;
                }

                String commandName = arguments[0];

                if(commandName == null || commandName.isEmpty()) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid MPCommand name.](color=red)"));
                    return;
                }

                if(!treeCommandManager.removeCommand(commandName)) {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand doesn't exists.](color=red)"));
                } else {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand removed successfully](color=green)"));
                }
            }

            @Override
            protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
                if(arguments.length == 1) {
                    return Optional.of(Arrays.asList(treeCommandManager.getCommandNames()));
                }
                return Optional.empty();
            }

            @Override
            protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
                return commandSender.hasPermission("com.minepunks.mpc.remove");
            }
        });
        this.addSubCommand("reload", new Command() {
            @Override
            protected void execute(CommandSender commandSender, String[] arguments) {
                treeCommandManager.load();
                Audience commandSenderAudience = MPCommands.INSTANCE.adventure().sender(commandSender);
                commandSenderAudience.sendMessage(MineDown.parse("[MPCommand reloaded successfully](color=green)"));
            }

            @Override
            protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
                return Optional.empty();
            }

            @Override
            protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
                return commandSender.hasPermission("com.minepunks.mpc.reload");
            }
        });
        this.addSubCommand("rename", new Command() {
            @Override
            protected void execute(CommandSender commandSender, String[] arguments) {
                Audience commandSenderAudience = MPCommands.INSTANCE.adventure().sender(commandSender);

                if(arguments.length != 2) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid command syntax. Use /mpc rename [oldCommandName] [newCommandName]](color=red)"));
                    return;
                }

                String oldName = arguments[0];

                if(oldName == null || oldName.isEmpty()) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid MPCommand name.](color=red)"));
                    return;
                }

                String newName = arguments[1];

                if(newName == null || newName.isEmpty()) {
                    commandSenderAudience.sendMessage(MineDown.parse("[Invalid new MPCommand name.](color=red)"));
                    return;
                }

                if(!treeCommandManager.renameCommand(oldName, newName)) {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand doesn't exists.](color=red)"));
                } else {
                    commandSenderAudience.sendMessage(MineDown.parse("[MPCommand renamed successfully](color=green)"));
                }
            }

            @Override
            protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
                if(arguments.length == 1) {
                    return Optional.of(Arrays.asList(treeCommandManager.getCommandNames()));
                }
                return Optional.empty();
            }

            @Override
            protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
                return commandSender.hasPermission("com.minepunks.mpc.rename");
            }
        });
    }

    @Override
    protected void execute(CommandSender commandSender, String[] arguments) {
        Audience commandSenderAudience = MPCommands.INSTANCE.adventure().sender(commandSender);

        if(arguments.length != 1) {
            commandSenderAudience.sendMessage(MineDown.parse("[Invalid command syntax. Use /mpc [commandName]](color=red)"));
            return;
        }

        String commandName = arguments[0];

        if(commandName == null || commandName.isEmpty()) {
            commandSenderAudience.sendMessage(MineDown.parse("[Invalid MPCommand name.](color=red)"));
            return;
        }

        if(this.treeCommandManager.hasCommand(commandName)) {
            if(commandSender.hasPermission("com.minepunks.mpc.exec." + commandName)) {
                this.treeCommandManager.runRootCommand(commandName);
            } else {
                commandSenderAudience.sendMessage(MineDown.parse("[You don't have permissions to execute this MPCommand](color=red)"));
            }
        } else {
            commandSenderAudience.sendMessage(MineDown.parse("[MPCommand name not found](color=red)"));
        }
    }

    @Override
    protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
        return commandSender.hasPermission("com.minepunks.mpc.exec");
    }

    @Override
    protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
        ArrayList<String> commandNames = new ArrayList<>();

        for (String commandName : this.treeCommandManager.getCommandNames()) {
            if(commandSender.hasPermission("com.minepunks.mpc.exec." + commandName)) {
                commandNames.add(commandName);
            }
        }

        return Optional.of(commandNames);
    }
}

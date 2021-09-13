package com.minepunks.mpc.commands;

import com.minepunks.mpc.MPCommands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Command implements CommandExecutor, TabExecutor {

    private final HashMap<String, Command> subCommands;

    public Command() {
        this.subCommands = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if(this.handlePermissions(sender, args)) {
            this.handleCommands(sender, args);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        return this.handleSuggestions(sender, args);
    }

    protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
        return Optional.empty();
    }

    protected void execute(CommandSender commandSender, String[] arguments) {

    }

    protected abstract boolean hasPermissions(CommandSender commandSender, String[] arguments);

    protected void addSubCommand(String subCommandLabel, Command command) {
        this.subCommands.put(subCommandLabel, command);
    }

    protected void setHelpMessage(Component message) {
        this.addSubCommand("help", new Command() {
            @Override
            protected Optional<List<String>> getSuggestions(CommandSender commandSender, String[] arguments) {
                return Optional.empty();
            }

            @Override
            protected void execute(CommandSender commandSender, String[] arguments) {
                MPCommands.INSTANCE.adventure().sender(commandSender).sendMessage(message);
            }

            @Override
            protected boolean hasPermissions(CommandSender commandSender, String[] arguments) {
                return true;
            }
        });
    }

    private void handleCommands(CommandSender commandSender, String[] arguments) {
        if (arguments.length < 1) {
            this.execute(commandSender, arguments);
            return;
        }

        String[] subCommandArgs = Arrays.copyOfRange(arguments, 1, arguments.length);
        Command subCommand = subCommands.getOrDefault(arguments[0], this);

        if (subCommand != this)
            subCommand.handleCommands(commandSender, subCommandArgs);
        else
            this.execute(commandSender, arguments);
    }

    private boolean handlePermissions(CommandSender commandSender, String[] arguments) {
        if (!this.hasPermissions(commandSender, arguments))
            return false;

        if (arguments.length > 1) {
            String[] subCommandArgs = Arrays.copyOfRange(arguments, 1, arguments.length);
            Command subCommand = subCommands.getOrDefault(arguments[0], this);

            return subCommand.handlePermissions(commandSender, subCommandArgs);
        }

        return true;
    }

    private List<String> handleSuggestions(CommandSender commandSender, String[] arguments) {
        if (arguments.length <= 1) {
            List<String> suggestions = this.getSuggestions(commandSender, arguments).orElse(new ArrayList<>());
            suggestions.addAll(this.subCommands.keySet());
            Collections.sort(suggestions);
            return suggestions;
        }

        String[] subCommandArgs = Arrays.copyOfRange(arguments, 1, arguments.length);
        Command subCommand = subCommands.getOrDefault(arguments[0], this);

        if (subCommand != this)
            return subCommand.handleSuggestions(commandSender, subCommandArgs);
        else
            return this.getSuggestions(commandSender, arguments).orElse(new ArrayList<>());
    }


}

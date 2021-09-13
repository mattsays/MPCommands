package com.minepunks.mpc.treecmds;

import com.google.gson.*;
import com.minepunks.mpc.MPCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.concurrent.ThreadLocalRandom;

public class TreeCommand {

    public static final class JsonSerializer implements com.google.gson.JsonDeserializer<TreeCommand>, com.google.gson.JsonSerializer<TreeCommand> {

        @Override
        public TreeCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if(!json.isJsonObject()) {
                throw new JsonParseException("JsonTreeCommand is not a json object");
            }

            JsonObject jsonTreeCommand = json.getAsJsonObject();

            float triggerChance = 1.0f;

            if(jsonTreeCommand.has("triggerChance")) {
                float chance = jsonTreeCommand.get("triggerChance").getAsFloat() / 100;

                if(chance != 2.0f) {
                    triggerChance = Math.max(0.0f, Math.min(1.0f, chance));
                } else {
                    triggerChance = chance;
                }
            }

            boolean randomPicking = false;

            if(jsonTreeCommand.has("randomPicking")) {
                randomPicking = jsonTreeCommand.get("randomPicking").getAsBoolean();
            }

            String command = null;

            if(jsonTreeCommand.has("command")) {
                command = jsonTreeCommand.get("command").getAsString();
            }

            TreeCommand[] children = null;

            if(jsonTreeCommand.has("children")) {
                JsonArray jsonChildren = jsonTreeCommand.getAsJsonArray("children");
                if(jsonChildren.size() > 0) {
                    children = new TreeCommand[jsonChildren.size()];

                    for (int i = 0; i < children.length; i++) {
                        JsonElement jsonElement = jsonChildren.get(i);
                        children[i] = context.deserialize(jsonElement, TreeCommand.class);
                    }
                }
                else {
                    MPCommands.LOGGER.warning("commands.json: Children member declared but has no elements in it");
                }
            }

            return new TreeCommand(command, triggerChance, randomPicking, children);
        }

        @Override
        public JsonElement serialize(TreeCommand src, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject jsonCommand = new JsonObject();
            if(src.root) {
                jsonCommand.addProperty("randomPicking", src.randomPicking);
                jsonCommand.add("children", new JsonArray());
            } else {
                if (src.triggerChance != 1.0f) {
                    jsonCommand.addProperty("triggerChance", src.triggerChance * 100);
                }

                if (src.randomPicking) {
                    jsonCommand.addProperty("randomPicking", true);
                }

                if (src.command != null) {
                    jsonCommand.addProperty("command", src.command);
                }

                if (src.children != null) {
                    JsonArray jsonChildren = new JsonArray();
                    for (TreeCommand child : src.children) {
                        jsonChildren.add(context.serialize(child));
                    }
                    jsonCommand.add("children", jsonChildren);
                }
            }
            return jsonCommand;
        }
    }

    private @Nullable final String command;
    private boolean root = false;
    private final float triggerChance;
    private final boolean randomPicking;
    private @Nullable final TreeCommand[] children;

    public TreeCommand(@Nullable String command, float triggerChance, boolean randomPicking, @Nullable TreeCommand[] children) {
        this.command = command;
        this.triggerChance = triggerChance;
        this.randomPicking = randomPicking;
        this.children = children;
    }

    public TreeCommand() {
        this(null, 1.0f, false, null);
        this.root = true;
    }

    public void execute(ConsoleCommandSender commandSender) {
        if (ThreadLocalRandom.current().nextFloat() > this.triggerChance) {
            return;
        }

        if (this.command != null) {
            if (this.triggerChance == 2.0f) {
                Bukkit.getScheduler().callSyncMethod(MPCommands.INSTANCE,
                        () -> {
                            boolean command1 = Bukkit.dispatchCommand(commandSender, this.command);
                            boolean command2 = Bukkit.dispatchCommand(commandSender, this.command);
                            return command1 && command2;
                        });
            } else {
                Bukkit.getScheduler().callSyncMethod(MPCommands.INSTANCE,
                        () -> Bukkit.dispatchCommand(commandSender, this.command));
            }
        }
        if (this.children != null) {
            if (this.randomPicking) {
                int randomIndex = ThreadLocalRandom.current().nextInt(this.children.length);
                TreeCommand randomChild = this.children[randomIndex];

                if (randomChild != null) {
                    randomChild.execute(commandSender);
                }
            } else {
                for (TreeCommand child : this.children) {
                    if (child != null) {
                        child.execute(commandSender);
                    }
                }
            }
        }
    }

}

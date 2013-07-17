package com.codisimus.plugins.phatloots.loot;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsCommandSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A CommandLoot is a Command which may be executed from the player or the console
 *
 * @author Cody
 */
@SerializableAs("Command")
public class CommandLoot extends Loot {
    private static PhatLootsCommandSender cs = new PhatLootsCommandSender();
    private String command;
    private boolean fromConsole;
    private boolean tempOP;

    /**
     * Constructs a new CommandLoot for the given command
     *
     * @param command The given command
     */
    public CommandLoot(String command) {
        //The command should not start with '/'
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        //Check if the command should be run by the player rather than the console
        if (command.startsWith("sudo ")) {
            fromConsole = false;
            tempOP = true;
            //Remove 'sudo ' from the command
            this.command = command.substring(5);
        } else {
            fromConsole = true;
            this.command = command;
        }
    }

    /**
     * Constructs a new CommandLoot from a Configuration Serialized phase
     *
     * @param map The map of data values
     */
    public CommandLoot(Map<String, Object> map) {
        String currentLine = null; //The value that is about to be loaded (used for debugging)
        try {
            probability = (Double) map.get(currentLine = "Probability");
            command = (String) map.get(currentLine = "Command");
            fromConsole = (Boolean) map.get(currentLine = "FromConsole");
            tempOP = (Boolean) map.get(currentLine = "TempOP");
        } catch (Exception ex) {
            //Print debug messages
            PhatLoots.logger.severe("Failed to load CommandLoot line: " + currentLine);
            PhatLoots.logger.severe("of PhatLoot: " + (PhatLoot.current == null ? "unknown" : PhatLoot.current));
            PhatLoots.logger.severe("Last successfull load was...");
            PhatLoots.logger.severe("PhatLoot: " + (PhatLoot.last == null ? "unknown" : PhatLoot.last));
            PhatLoots.logger.severe("Loot: " + (Loot.last == null ? "unknown" : Loot.last.toString()));
        }
    }

    /**
     * Adds this command to the LootBundle
     *
     * @param lootBundle The loot that has been rolled for
     * @param lootingBonus The increased chance of getting rarer loots
     */
    @Override
    public void getLoot(LootBundle lootBundle, double lootingBonus) {
        lootBundle.addCommand(this);
    }

    /**
     * Executes the command for the looting player
     *
     * @param player The Player looting or null if no Player is involved
     */
    public void execute(Player player) {
        String cmd;
        if (player == null) {
            //Return if the command is ment for a player
            if (!fromConsole || command.contains("<player>")) {
                return;
            } else {
                cmd = command;
            }
        } else {
            cmd = command.replace("<player>", player.getName());
        }
        if (fromConsole) { //From console
            Bukkit.dispatchCommand(cs, cmd);
        } else if (tempOP) { //From Player as OP
            //Make the player OP for long enough to execute the command
            player.setOp(true);
            Bukkit.dispatchCommand(player, cmd);
            player.setOp(false);
        } else { //From Player
            Bukkit.dispatchCommand(player, cmd);
        }
    }

    /**
     * Returns the information of the CommandLoot in the form of an ItemStack
     *
     * @return An ItemStack representation of the Loot
     */
    @Override
    public ItemStack getInfoStack() {
        //A CommandLoot is represented by a Command Block
        ItemStack infoStack = new ItemStack(Material.COMMAND);

        //Set the display name to the command
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2Command");

        //Add more specific details of the command
        List<String> details = new ArrayList();
        details.add("§4Probability: §6" + probability);
        details.add("§4Command: §6" + command);
        details.add(fromConsole ? "§6From Console" : "§6From Player");
        if (tempOP) {
            details.add("§6Player is temporarily OPed");
        }

        //Construct the ItemStack and return it
        info.setLore(details);
        infoStack.setItemMeta(info);
        return infoStack;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        if (!fromConsole) {
            sb.append("sudo ");
        }
        sb.append("command @ ");
        //Only display the decimal values if the probability is not a whole number
        sb.append(String.valueOf(Math.floor(probability) == probability ? (int) probability : probability));
        sb.append("%");
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CommandLoot) {
            CommandLoot loot = (CommandLoot) object;
            return loot.fromConsole == fromConsole
                    && loot.tempOP == tempOP
                    && loot.command.equals(command);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.command != null ? this.command.hashCode() : 0);
        hash = 37 * hash + (this.fromConsole ? 1 : 0);
        hash = 37 * hash + (this.tempOP ? 1 : 0);
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map map = new TreeMap();
        map.put("Probability", probability);
        map.put("Command", command);
        map.put("FromConsole", fromConsole);
        map.put("TempOP", tempOP);
        return map;
    }
}

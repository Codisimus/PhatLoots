package com.codisimus.plugins.phatloots;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
@SerializableAs("Command")
public class CommandLoot extends Loot {
    private static PhatLootsCommandSender cs = new PhatLootsCommandSender();
    private String command;
    private boolean fromConsole;
    private boolean tempOP;

    public CommandLoot(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.startsWith("sudo ")) {
            fromConsole = false;
            tempOP = true;
            this.command = command.substring(5);
        } else {
            fromConsole = true;
            this.command = command;
        }
    }

    public CommandLoot(Map<String, Object> map) {
        probability = (Double) map.get("Probability");
        command = (String) map.get("Command");
        fromConsole = (Boolean) map.get("FromConsole");
        tempOP = (Boolean) map.get("TempOP");
    }

    @Override
    public void getLoot(Player player, double lootingBonus, LinkedList<ItemStack> items) {
        String cmd = command.replace("<player>", player.getName());
        if (fromConsole) {
            PhatLoots.server.dispatchCommand(cs, cmd);
        } else if (tempOP) {
            player.setOp(true);
            PhatLoots.server.dispatchCommand(player, cmd);
            player.setOp(false);
        } else {
            PhatLoots.server.dispatchCommand(player, cmd);
        }
    }

    @Override
    public ItemStack getInfoStack() {
        ItemStack infoStack = new ItemStack(Material.COMMAND);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2Command");
        List<String> details = new ArrayList();
        details.add("§4Probability: §6" + probability);
        details.add("§4Command: §6" + command);
        details.add(fromConsole ? "§6From Console" : "§6From Player");
        if (tempOP) {
            details.add("§6Player is temporarily OPed");
        }
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
        sb.append("command");
        sb.append(" @ ");
        sb.append(Math.floor(probability) == probability ? String.valueOf((int) probability) : String.valueOf(probability));
        sb.append("%");
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CommandLoot)) {
            return false;
        }

        CommandLoot loot = (CommandLoot) object;
        return loot.fromConsole == fromConsole
                && loot.tempOP == tempOP
                && loot.command.equals(command);
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

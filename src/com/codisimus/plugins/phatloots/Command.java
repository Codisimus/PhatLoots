package com.codisimus.plugins.phatloots;

import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

/**
 * A Loot is a ItemStack and with a probability of looting
 *
 * @author Codisimus
 */
@SerializableAs("Command")
public class Command extends Loot implements ConfigurationSerializable {
    private String command;
    private boolean fromConsole;
    private boolean tempOP;

    public Command(String command) {
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

    public Command(Map<String, Object> map) {
        this.command = (String) map.get("Command");
        this.probability = (Double) map.get("Probability");
        this.fromConsole = (Boolean) map.get("FromConsole");
        this.tempOP = (Boolean) map.get("TempOP");
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
        if (!(object instanceof Command)) {
            return false;
        }

        Command loot = (Command) object;
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
        map.put("Command", command);
        map.put("FromConsole", fromConsole);
        map.put("TempOP", tempOP);
        return map;
    }
}

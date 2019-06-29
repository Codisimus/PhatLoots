package com.codisimus.plugins.phatloots.conditions;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loot condition that checks for time.
 *
 * @author Redned
 */
@SerializableAs("TimeCondition")
public class TimeCondition extends LootCondition {

    private String time;

    public TimeCondition(String name) {
        super(name);

        time = "day";
    }

    public TimeCondition(Map<String, Object> map) {
        super(map);

        time = (String) map.get("Time");
    }

    @Override
    public boolean checkCondition(Player player) {
        // Check if the time value is a number
        try {
            long timeLong = Long.parseLong(time);
            if (player.getWorld().getTime() != timeLong)
                return false;
        } catch (NumberFormatException ex) {
            // Check if the time value is a string (e.g. day, night)
            if (time.equalsIgnoreCase("day")) {
                if (player.getWorld().getTime() > 13000 && player.getWorld().getTime() < 23850)
                    return false;

            } else if (time.equalsIgnoreCase("night")) {
                if (player.getWorld().getTime() < 13000 || player.getWorld().getTime() > 23850)
                    return false;

            }
        }

        return true;
    }

    // TODO: Find a way to obtain user input and allow for specific time ##'s
    @Override
    public ItemStack handleClick(Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            if (time.equalsIgnoreCase("day")) {
                time = "night";
            } else if (time.equalsIgnoreCase("night")) {
                time = "day";
            }
        }

        ItemStack item = super.handleClick(inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eTime Condition");
        item.setType(Material.CLOCK);
        List<String> lore = meta.getLore();
        lore.add("§6 Required Time: §e" + time);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Change the Time");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Time", time);
        return map;
    }
}

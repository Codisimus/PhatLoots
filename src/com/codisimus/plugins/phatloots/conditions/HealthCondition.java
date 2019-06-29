package com.codisimus.plugins.phatloots.conditions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Loot condition that checks for health.
 *
 * @author Redned
 */
@SerializableAs("HealthCondition")
public class HealthCondition extends LootCondition {

    private double health;
    private String option;

    public HealthCondition(String name) {
        super(name);

        health = 0;
        option = "above";
    }

    public HealthCondition(Map<String, Object> map) {
        super(map);

        health = (double) map.get("Health");
        option = (String) map.get("Option");
    }

    @Override
    public boolean checkCondition(Player player) {
        switch (option) {
            case "below":
                if (player.getHealth() < health)
                    return true;
                break;
            case "above":
                if (player.getHealth() > health)
                    return true;
                break;
            case "at":
                if (player.getHealth() == health)
                    return true;
                break;
        }

        return false;
    }

    @Override
    public ItemStack handleClick(Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (option) {
                case "below":
                    option = "above";
                    break;
                case "above":
                    option = "at";
                    break;
                case "at":
                    option = "below";
                    break;
            }

        }

        if (click == ClickType.SHIFT_LEFT) {
            health += 1;
        }

        if (click == ClickType.SHIFT_RIGHT) {
            health -= 1;
        }

        if (click == ClickType.MIDDLE)
            health = 1;

        ItemStack item = super.handleClick(inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Health Condition");
        item.setType(Material.BEEF);
        List<String> lore = meta.getLore();
        lore.add("§6 Required Health: §e" + health);
        lore.add("§6 Health Option: §e" + option);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Cycle Through the Options");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Add Health");
        lore.add("§4SHIFT + RIGHT CLICK:");
        lore.add("§6 Remove Health");
        lore.add("§4SCROLL CLICK:");
        lore.add("§6 Reset Health to 1");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Health", health);
        map.put("Option", option);
        return map;
    }
}

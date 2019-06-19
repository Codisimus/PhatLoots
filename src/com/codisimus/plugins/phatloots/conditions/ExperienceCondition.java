package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLootsUtil;
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
 * Loot condition that checks for experience.
 *
 * @author Redned
 */
@SerializableAs("ExperienceCondition")
public class ExperienceCondition extends LootCondition {

    private int experience;
    private String option;

    public ExperienceCondition(Map<String, Object> map) {
        super(map);

        experience = (int) map.get("Experience");
        option = (String) map.get("Option");
    }
    @Override
    public boolean checkCondition(Player player) {
        switch (option) {
            case "below":
                if (PhatLootsUtil.getTotalExperience(player) < experience)
                    return true;
                break;
            case "above":
                if (PhatLootsUtil.getTotalExperience(player) > experience)
                    return true;
                break;
            case "at":
                if (PhatLootsUtil.getTotalExperience(player) == experience)
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

            if (click == ClickType.SHIFT_LEFT) {
                experience += 1;
            }

            if (click == ClickType.SHIFT_RIGHT) {
                experience -= 1;
            }

            if (click == ClickType.MIDDLE)
                experience = 1;
        }

        ItemStack item = super.handleClick(inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Experience Condition");
        item.setType(Material.EXPERIENCE_BOTTLE);
        List<String> lore = meta.getLore();
        lore.add("§6 Required Experience: §e" + experience);
        lore.add("§6 Experience Option: §e" + option);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Cycle Through the Options");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Add Experience");
        lore.add("§4SHIFT + RIGHT CLICK:");
        lore.add("§6 Remove Experience");
        lore.add("§4SCROLL CLICK:");
        lore.add("§6 Reset Experience to 1");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Experience", experience);
        map.put("Option", option);
        return map;
    }
}

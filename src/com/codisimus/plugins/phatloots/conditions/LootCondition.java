package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A loot condition is a condition that is ran before the loot is open. If the condition
 * returns true, the loot opening proceeds. If it returns false, the loot is not given
 * or opened.
 *
 * @author Redned
 */
public abstract class LootCondition implements ConfigurationSerializable {

    protected String name;
    protected boolean enabled;

    public LootCondition(String name) {
        this.name = name;

        enabled = false;
    }

    public LootCondition(Map<String, Object> map) {
        name = (String) map.get("Name");
        enabled = (boolean) map.get("Enabled");
    }

    /**
     * Checks the condition
     *
     * @param player The player to check the condition on
     * @return boolean The result of the condition
     */
    public abstract boolean checkCondition(Player player);

    /**
     * Handles the click
     *
     * @param player The player that clicked
     * @param phatLoot The PhatLoot in which the conditions are being changed
     * @param inventory The inventory clicked in
     * @param click The click type initiated
     * @return ItemStack The ItemStack constructed
     */
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.RIGHT) {
            this.enabled = !enabled;
        }

        ItemStack condition = new ItemStack(Material.REDSTONE);
        ItemMeta meta = condition.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§4Condition Info:");
        lore.add("§6 Enabled: §e" + enabled);
        meta.setLore(lore);
        condition.setItemMeta(meta);
        return condition;
    }

    /**
     * Gets the name of the condition
     *
     * @return The condition name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets if the condition is enabled
     *
     * @return If the condition is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggles if the condition is enabled
     *
     * @param enabled The new value for the condition
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map =  new TreeMap<>();
        map.put("Name", name);
        map.put("Enabled", enabled);
        return map;
    }
}

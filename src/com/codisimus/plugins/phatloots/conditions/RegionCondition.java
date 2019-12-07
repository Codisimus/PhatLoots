package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.gui.InventoryConditionListener;
import com.codisimus.plugins.phatloots.listeners.MobListener;
import com.codisimus.plugins.phatloots.util.ChatInput;
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
 * Loot condition that checks for region.
 *
 * @author Redned
 */
@SerializableAs("RegionCondition")
public class RegionCondition extends LootCondition {

    private String region;
    private String option;

    public RegionCondition(String name) {
        super(name);

        region = "none";
        option = "inside";
    }

    public RegionCondition(Map<String, Object> map) {
        super(map);

        region = (String) map.get("Region");
        option = (String) map.get("Option");
    }

    @Override
    public boolean checkCondition(Player player) {
        if (MobListener.regionHook == null)
            return false;

        List<String> regions = MobListener.regionHook.getRegionNames(player.getLocation());
        switch (option) {
            case "inside":
                if (regions.isEmpty() && (region.isEmpty() || region.equals("none"))) {
                    return true;
                }

                if (regions.contains(region))
                    return true;
                break;
            case "outside":
                if (!regions.contains(region))
                    return true;

                if (region.equals("none"))
                    return true;
        }

        return false;
    }

    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (option) {
                case "inside":
                    option = "outside";
                    break;
                case "outside":
                    option = "inside";
                    break;
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Region Condition");
        item.setType(Material.BARRIER);
        List<String> lore = meta.getLore();
        lore.add("§6 Required Region: §e" + region);
        lore.add("§6 Region Option: §e" + option);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Cycle Through the Options");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Set Region");
        meta.setLore(lore);
        item.setItemMeta(meta);

        if (click == ClickType.SHIFT_LEFT) {
            player.closeInventory();
            new ChatInput(player) {

                @Override
                public void onChatInput(String input) {
                    region = input;
                    InventoryConditionListener.viewConditionMenu(player, phatLoot);
                }
            };
        }

        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Region", region);
        map.put("Option", option);
        return map;
    }
}

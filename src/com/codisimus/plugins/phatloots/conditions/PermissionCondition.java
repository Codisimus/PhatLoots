package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.gui.InventoryConditionListener;
import com.codisimus.plugins.phatloots.util.InteractionInputs;
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
 * Loot condition that checks for permission.
 *
 * @author Redned
 */
@SerializableAs("PermissionCondition")
public class PermissionCondition extends LootCondition {

    private String permission;
    private String option;

    public PermissionCondition(String name) {
        super(name);

        permission = "none";
        option = "has";
    }

    public PermissionCondition(Map<String, Object> map) {
        super(map);

        permission = (String) map.get("Permission");
        option = (String) map.get("Option");
    }

    @Override
    public boolean checkCondition(Player player) {
        switch (option) {
            case "has":
                if (player.hasPermission(permission))
                    return true;
                break;
            case "lacks":
                if (!player.hasPermission(permission))
                    return true;
                break;
        }

        return false;
    }

    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (option) {
                case "has":
                    option = "lacks";
                    break;
                case "lacks":
                    option = "has";
                    break;
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Permission Condition");
        item.setType(Material.PAPER);
        List<String> lore = meta.getLore();
        lore.add("§6 Permission: §e" + permission);
        lore.add("§6 Permission Option: §e" + option);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Cycle Through the Options");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Set Permission");
        meta.setLore(lore);
        item.setItemMeta(meta);

        if (click == ClickType.SHIFT_LEFT) {
            player.closeInventory();
            new InteractionInputs.ChatInput(player) {

                @Override
                public void onChatInput(String input) {
                    permission = input;
                    InventoryConditionListener.viewConditionMenu(player, phatLoot);
                }
            };
        }

        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Permission", permission);
        map.put("Option", option);
        return map;
    }
}

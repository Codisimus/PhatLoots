package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.gui.InventoryConditionListener;
import com.codisimus.plugins.phatloots.util.InteractionInputs;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Loot condition that checks for items
 *
 * @author Redned
 */
@SerializableAs("ItemCondition")
public class ItemCondition extends LootCondition {

    private ItemStack item;
    private EquipmentSlot slot;

    public ItemCondition(String name) {
        super(name);

        this.item = null;
        this.slot = null;
    }

    public ItemCondition(Map<String, Object> map) {
        super(map);

        this.item = (ItemStack) map.get("Item");
        this.slot = getSlot((String) map.get("Slot"));
    }

    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            player.closeInventory();
            new InteractionInputs.InventoryInput(player) {

                @Override
                public void onInventoryInteract(ItemStack item) {
                    ItemCondition.this.item = item;
                    player.closeInventory();

                    Bukkit.getScheduler().runTaskLater(PhatLoots.plugin, () -> {
                        InventoryConditionListener.viewConditionMenu(player, phatLoot);
                    }, 1);
                }
            };
        }
        if (click == ClickType.SHIFT_LEFT) {
            if (this.slot == null) {
                this.slot = EquipmentSlot.HAND;
            } else {
                this.slot = getSlot(this.slot.ordinal() + 1);
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Item Condition");
        item.setType(Material.IRON_SWORD);
        List<String> lore = meta.getLore();
        lore.add("§6 Item: §e" + formattedItemName());
        lore.add("§6 Slot: §e" + (this.slot == null ? "None" : WordUtils.capitalize(this.slot.name().toLowerCase().replace("_", " "))));
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Select an Item");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Set slot item needs to be in");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean checkCondition(Player player) {
        if (this.item == null) {
            return true;
        }
        if (this.slot == null) {
            return player.getInventory().contains(this.item);
        }
        // Method for this was not available in 1.13, so we have to use this
        switch (this.slot) {
            case HAND:
                return this.item.equals(player.getInventory().getItemInMainHand());
            case OFF_HAND:
                return this.item.equals(player.getInventory().getItemInOffHand());
            case HEAD:
                return this.item.equals(player.getInventory().getHelmet());
            case CHEST:
                return this.item.equals(player.getInventory().getChestplate());
            case LEGS:
                return this.item.equals(player.getInventory().getLeggings());
            case FEET:
                return this.item.equals(player.getInventory().getBoots());
            default:
                return false;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("ItemStack", this.item);
        map.put("Slot", this.slot == null ? null : this.slot.toString());
        return map;
    }

    private String formattedItemName() {
        if (this.item == null) {
            return "None";
        }
        if (this.item.getItemMeta() == null || !this.item.getItemMeta().hasDisplayName()) {
            return WordUtils.capitalize(this.item.getType().name().toLowerCase().replace("_", " ")) + " x" + this.item.getAmount();
        }
        return this.item.getItemMeta().getDisplayName() + " §r§e(" + WordUtils.capitalize(this.item.getType().name().toLowerCase().replace("_", " ")) + ") x" + this.item.getAmount();
    }

    private EquipmentSlot getSlot(String slotStr) {
        if (slotStr == null) {
            return null;
        }

        try {
            return EquipmentSlot.valueOf(slotStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private EquipmentSlot getSlot(int ordinal) {
        if (ordinal < EquipmentSlot.values().length) {
            return EquipmentSlot.values()[ordinal];
        }
        return null;
    }
}

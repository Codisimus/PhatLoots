package com.codisimus.plugins.phatloots.gui;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.conditions.LootCondition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Listens for interactions with PhatLoot Condition info GUIs
 *
 * @author Redned
 */

// TODO: Remove all of the static methods and variables from this class
public class InventoryConditionListener implements Listener {

    private final static int SIZE = 54;
    private static final Map<UUID, PhatLoot> conditionViewers = new HashMap<>(); //Player -> PhatLoot they are viewing

    public static void viewConditionMenu(Player player, PhatLoot phatLoot) {
        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, 54, phatLoot.name + " Conditions");
        if (phatLoot.getLootConditions() != null && !phatLoot.getLootConditions().isEmpty()) {
            List<LootCondition> lootConditions = phatLoot.getLootConditions();
            for (int i = 0; i < lootConditions.size(); i++) {
                inv.setItem(i, lootConditions.get(i).handleClick(player, phatLoot, inv, null));
            }
        }

        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Up to...");
        meta.setLore(Collections.singletonList(ChatColor.GOLD + phatLoot.name));
        item.setItemMeta(meta);
        inv.setItem(SIZE - 1, item);

        conditionViewers.put(player.getUniqueId(), phatLoot);
        player.openInventory(inv);
    }

    /**
     * Processes Players clicking within the PhatLoot condition GUI
     *
     * @param event The InventoryClickEvent which occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }

        Player player = (Player) human;
        if (!conditionViewers.containsKey(player.getUniqueId())) {
            return;
        }

        if (!event.getView().getTitle().contains("Conditions")) {
            conditionViewers.remove(player.getUniqueId());
        }

        if (event.getCurrentItem() == null)
            return;

        // Don't allow any inventory clicking
        event.setResult(Event.Result.DENY);
        player.updateInventory();

        // Store popularly accessed variables
        PhatLoot phatLoot = conditionViewers.get(player.getUniqueId());
        if (phatLoot == null)
            return;

        List<LootCondition> lootConditions = phatLoot.getLootConditions();
        if (lootConditions.size() > event.getSlot() && lootConditions.get(event.getSlot()) != null) {
            event.getClickedInventory().setItem(event.getSlot(), lootConditions.get(event.getSlot()).handleClick(player, phatLoot, event.getInventory(), event.getClick()));
            return;
        }

        if (event.getSlot() == SIZE - 1) {
            conditionViewers.remove(player.getUniqueId());
            InventoryListener.viewPhatLoot(player, phatLoot);
        }
    }

    /**
     * Handles if the condition GUI is closed
     *
     * @param event The InventoryCloseEvent which occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }

        // Check if the menu being closed is the condition menu
        if (!event.getView().getTitle().contains("Conditions"))
            return;

        // Return if the Player is not viewing the condition menu
        Player player = (Player) human;
        if (!conditionViewers.containsKey(player.getUniqueId()))
            return;

        conditionViewers.remove(player.getUniqueId());
    }

    /**
     * Returns a map of the condition viewers
     *
     * @return a map of the condition viewers
     */
    public static Map<UUID, PhatLoot> getConditionViewers() {
        return conditionViewers;
    }
}

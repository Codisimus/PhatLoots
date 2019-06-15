package com.codisimus.plugins.phatloots.gui;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.conditions.LootCondition;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for interactions with PhatLoot Condition info GUIs
 *
 * @author Redned
 */

// TODO: Remove all of the static methods and variables from this class
public class InventoryConditionListener implements Listener {

    private static final HashMap<UUID, PhatLoot> conditionViewers = new HashMap<>(); //Player -> PhatLoot they are viewing

    public static void viewConditionMenu(Player player, PhatLoot phatLoot) {
        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, 54, phatLoot.name + " Conditions");
        if (phatLoot.getLootConditions() != null && !phatLoot.getLootConditions().isEmpty()) {
            Map<Integer, LootCondition> lootConditionMap = phatLoot.getLootConditionsMap();
            for (int i = 0; i < lootConditionMap.size(); i++) {
                inv.setItem(i, lootConditionMap.get(i).handleClick(inv, null));
            }
        }

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
        } else if (!event.getView().getTitle().contains("Conditions")) {
            conditionViewers.remove(player.getUniqueId());
        }

        if (event.getCurrentItem() == null)
            return;

        // Don't allow any inventory clicking
        event.setResult(Event.Result.DENY);
        player.updateInventory();

        // Store popularly accessed variables
        PhatLoot phatLoot = conditionViewers.get(player.getUniqueId());

        Map<Integer, LootCondition> lootConditionMap = phatLoot.getLootConditionsMap();
        for (int i = 0; i < lootConditionMap.size(); i++) {
            event.getClickedInventory().setItem(i, lootConditionMap.get(i).handleClick(event.getInventory(), event.getClick()));
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
}

package com.codisimus.plugins.phatloots.gui;

import com.codisimus.plugins.phatloots.PhatLoot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

/**
 * Listens for interactions with PhatLoot Condition info GUIs
 *
 * @author Redned
 */
public class InventoryConditionListener implements Listener {

    public static void viewConditionMenu(Player player, PhatLoot phatLoot) {
        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, 54, phatLoot.name + " Conditions");

    }
}

package com.codisimus.plugins.phatloots;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * A Forgettable Inventory is a virtual Inventory that will be removed from memory after the set delay
 *
 * @author Mtihc, Codisimus
 */
public class ForgettableInventory {
    static long delay;
    private static final HashMap<String, ForgettableInventory> inventories = new HashMap<>(); //User+Chest Location -> Inventory
    private Inventory inventory;
    private final String key;
    private BukkitTask task;

    /**
     * Constructs a new ForgettableInventory with the given key and Inventory
     *
     * @param key The given key
     * @param inventory The given Inventory
     */
    public ForgettableInventory(String key, Inventory inventory) {
        this.key = key;
        this.inventory = inventory;
    }

    /**
     * Schedules this ForgettableInventory to be forgotten
     */
    public void schedule() {
        inventories.put(key, this);
        if (task != null) {
            task.cancel();
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                inventories.remove(key);
            }
        }.runTaskLater(PhatLoots.plugin, delay);

    }

    /**
     * Gets the Inventory of this ForgettableInventory
     *
     * @return The Inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns the ForgettableInventory of the given key
     *
     * @param key The given key
     * @return The ForgettableInventory of the given key
     */
    public static ForgettableInventory get(String key) {
        return inventories.get(key);
    }

    /**
     * Returns true if there is a Inventory for the given key
     *
     * @param key The given key
     * @return true if there is a Inventory for the given key
     */
    public static boolean has(String key) {
        return inventories.containsKey(key);
    }

    /**
     * Refreshes the inventory with the given title. Due to
     * the inventory changes in 1.14, this method had to be
     * added since there is no way to easily retrieve the title
     * of an inventory anymore.
     *
     * @param title the title to set
     */
    public void refresh(String title) {
        if (inventory == null)
            return;

        Inventory inventory = Bukkit.createInventory(null, this.inventory.getSize(), title);
        inventory.setContents(this.inventory.getContents());
        inventory.setStorageContents(this.inventory.getStorageContents());
        this.inventory = inventory;

        inventories.put(key, this);
    }
}

package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * A Forgettable Inventory is a virtual Inventory that will be removed from memory after the set delay
 *
 * @author Mtihc
 */
public class ForgettableInventory {
    static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>(); //User+Chest Location -> Inventory
    static long delay;
    private Inventory inventory;
    private String key;
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
        task = Bukkit.getScheduler().runTaskLater(PhatLoots.plugin, new Runnable() {
                @Override
                public void run() {
                    inventories.remove(key);
                }
            }, delay);

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
}

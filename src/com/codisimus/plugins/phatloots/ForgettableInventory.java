package com.codisimus.plugins.phatloots;

import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A Forgettable Inventory is a virtual Inventory that will be removed from memory after the given delay
 *
 * @author Mtihc
 */
public abstract class ForgettableInventory implements Runnable {
    private JavaPlugin plugin;
    private long delay = 6000L;
    private int taskId;

    private Inventory inventory;

    public ForgettableInventory(JavaPlugin plugin, Inventory inventory) {
        this.plugin = plugin;
        this.taskId = 0;
        this.inventory = inventory;
    }

    /**
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    public void schedule() {
        cancel();
        taskId  = plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(plugin, this, delay);
    }

    public void cancel() {
        if (taskId != 0) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = 0;
        }
    }

    @Override
    public void run() {
        cancel();
        execute();
    }

    protected abstract void execute();
}

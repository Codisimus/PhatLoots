package com.codisimus.plugins.phatloots;

import org.bukkit.inventory.ItemStack;

/**
 * A PhatLootsChest is a Block location and a Map of Users with times attached to it
 * 
 * @author Codisimus
 */
public class Loot {
    public ItemStack item;
    public int probability;

    /**
     * Constructs a new PhatLootsChest with the given Block Location data
     * 
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     */
    public Loot (int id, short durability, int amount, int probability) {
        item = new ItemStack(id, amount);
        if (durability >= 0)
            item.setDurability(durability);
        this.probability = probability;
    }

    @Override
    public String toString() {
        return item.getTypeId()+"'"+item.getDurability()+"'"+item.getAmount()+"'"+probability;
    }
}
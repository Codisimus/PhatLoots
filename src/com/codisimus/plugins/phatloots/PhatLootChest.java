package com.codisimus.plugins.phatloots;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A PhatLootChest is a Block location and a Map of Users with times attached to them
 * 
 * @author Codisimus
 */
public class PhatLootChest {
    private String world;
    private int x, y, z;
    public boolean isDispenser;
    

    /**
     * Constructs a new PhatLootChest with the given Block
     * 
     * @param block The given Block
     */
    public PhatLootChest(Block block) {
        world = block.getWorld().getName();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        isDispenser = block.getTypeId() == 23;
    }
    
    /**
     * Constructs a new PhatLootChest with the given Block Location data
     * 
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     */
    public PhatLootChest(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        Block block = PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
        isDispenser = block.getTypeId() == 23;
    }
    
    /**
     * Returns the Block that this Chest Represents
     * 
     * @return The Block that this Chest Represents
     */
    public Block getBlock() {
        return PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
    }
    
    /**
     * Returns true if the given Block is this PhatLootChest
     * 
     * @param block The given Block
     * @return True if the given Block is the same Dispenser or part of the double Chest
     */
    public boolean isBlock(Block block) {
        //Return false if Blocks are not in the same x-axis
        if (x != block.getX())
            return false;
        
        //Return false if Blocks are not in the same y-axis
        if (y != block.getY())
            return false;
        
        //Return false if Blocks are not in the same z-axis
        if (z != block.getZ())
            return false;
        
        //Return false if Blocks are not in the same World
        return world.equals(block.getWorld().getName());
    }

    /**
     * Returns whether the given Block is left or right of the PhatLootChest Block
     * Will only return true is both the Block and the PhatLootChest are Chests
     * 
     * @param block The given Block
     * @return true if the given Block is left or right of the PhatLootChest
     */
    public boolean isNeighbor(Block block) {
        //Return false if either Block is not a Chest
        if (isDispenser || block.getTypeId() != 54)
            return false;
        
        //Return false if Blocks are not in the same y-axis
        if (y != block.getY())
            return false;
        
        //Return false if Blocks are not in the same World
        if (!world.equals(block.getWorld().getName()))
            return false;
        
        //Return true if the Blocks are side by side
        int a = block.getX();
        int c = block.getZ();
        if (a == x)
            return c == z+1 || c == z-1;
        else if (c == z)
            return a == x+1 || a == x-1;
        else
            return false;
    }
    
    /**
     * Drops the given item outside the PhatLootChest
     * 
     * @param item The ItemStack that will be dropped
     * @param player The Player (if any) that will be informed of the drop
     */
    public void overFlow(ItemStack item, Player player) {
        Block block = getBlock();
        block.getWorld().dropItemNaturally(block.getLocation(), item);
        if (player != null)
            player.sendMessage(PhatLootsMessages.overflow.replaceAll("<item>", item.getType().name()));
    }

    /**
     * Returns the String representation of this PhatLootChest
     * The format of the returned String is world'x'y'z
     * 
     * @return The String representation of this Chest
     */
    @Override
    public String toString() {
        return world+"'"+x+"'"+y+"'"+z;
    }
}
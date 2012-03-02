package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A PhatLootChest is a Block location and a Map of Users with times attached to them
 * 
 * @author Codisimus
 */
public class PhatLootChest {
    public Block block;
    public boolean isDispenser;
    public HashMap users = new HashMap(); //A map of each Player that loots the Chest {PlayerName=TimeActivated}

    /**
     * Constructs a new PhatLootChest with the given Block
     * 
     * @param block The given Block
     */
    public PhatLootChest (Block block) {
        this.block = block;
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
    public PhatLootChest (String world, int x, int y, int z) {
        block = PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
        isDispenser = block.getTypeId() == 23;
    }
    
    /**
     * Retrieves the time for the given Player
     * 
     * @param player The Player whose time is requested
     * @return The time as an array of ints
     */
    public int[] getTime(String player) {
        return (int[])users.get(player);
    }
    
    /**
     * Return the other half of the Double Chest
     * If this is a dispenser then null is returned
     * If this is a single Chest then the normal Chest Block is returned
     * 
     * @return The other half of the Double Chest
     */
    public Chest getOtherHalf() {
        if (isDispenser)
            return null;
        
        Block neighbor = block.getRelative(0, 0, 1);
        if (neighbor.getTypeId() == 54)
            return (Chest)neighbor.getState();
        
        neighbor = block.getRelative(1, 0, 0);
        if (neighbor.getTypeId() == 54)
            return (Chest)neighbor.getState();
        
        neighbor = block.getRelative(0, 0, -1);
        if (neighbor.getTypeId() == 54)
            return (Chest)neighbor.getState();
        
        neighbor = block.getRelative(-1, 0, 0);
        if (neighbor.getTypeId() == 54)
            return (Chest)neighbor.getState();
        
        //Return the single block Chest
        return (Chest)block.getState();
    }
    
    /**
     * Clears all items from the Chest's Inventory
     * 
     */
    public void clear() {
        if (!isDispenser) {
            Chest chest = (Chest)block.getState();
            chest.getInventory().clear();
            chest.update();
        }
    }
    
    /**
     * Drops the given item outside the PhatLootChest
     * 
     * @param item The ItemStack that will be dropped
     * @param player The Player (if any) that will be informed of the drop
     */
    public void overFlow(ItemStack item, Player player) {
        block.getWorld().dropItemNaturally(block.getLocation(), item);
        if (player != null)
            player.sendMessage(PhatLootsMessages.overflow.replaceAll("<item>", item.getType().name()));
    }

    /**
     * Returns true if the given Block is this PhatLootChest
     * 
     * @param block The given Block
     * @return True if the given Block is the same Dispenser or part of the double Chest
     */
    public boolean isBlock(Block block) {
        //Return true if they are the same Block
        if (this.block.equals(block))
            return true;
        
        //Return false if the PhatLootChest is a Dispenser or the given Block is not a Chest
        if (isDispenser || block.getTypeId() != 54)
            return false;
        
        //Return whether if the given Block is not the other half of the PhatLootChest
        return getOtherHalf().equals((Chest)block.getState());
    }

    /**
     * Returns the String representation of this PhatLootChest
     * The format of the returned String is as follows
     * world'x'y'z'{Player1@Days'Hours'Minutes'Seconds, Player1@Days'Hours'Minutes'Seconds}
     * 
     * @return The String representation of this Button
     */
    @Override
    public String toString() {
        String string = block.getWorld().getName()+"'"+block.getX()+"'"+block.getY()+"'"+block.getZ()+"{";

        Iterator itr = users.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            int[] time = getTime(key);

            string = string.concat(key+"@"+time[0]+"'"+time[1]+"'"+time[2]+"'"+time[3]+"'"+time[4]);
            
            if (itr.hasNext())
                string = string.concat(", ");
        }

        return string.concat("}");
    }
}
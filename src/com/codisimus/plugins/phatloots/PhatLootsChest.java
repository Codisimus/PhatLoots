package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A PhatLootsChest is a Block location and a Map of Users with times attached to them
 * 
 * @author Codisimus
 */
public class PhatLootsChest {
    public String world;
    public int x;
    public int y;
    public int z;
    public HashMap users = new HashMap(); //A map of each Player that activates the button {PlayerName=TimeActivated}

    /**
     * Constructs a new PhatLootsChest with the given Block
     * 
     * @param block The given Block
     */
    public PhatLootsChest (Block block) {
        world = block.getWorld().getName();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }
    
    /**
     * Constructs a new PhatLootsChest with the given Block Location data
     * 
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     */
    public PhatLootsChest (String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
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
     * Returns true if the given Block has the same Location data as this Button
     * 
     * @param block The given Block
     * @return True if the Location data is the same
     */
    public boolean isBlock(Block block) {
        //Return false if the y coordinate is not the same
        if (block.getY() != y)
            return false;

        //Return false if the world is not the same
        if (!block.getWorld().getName().equals(world))
            return false;
        
        int a = block.getX();
        int c = block.getZ();
        
        if (a != x) {
            //Return false if the z coordinate is not the same
            if (c != z)
                return false;
            
            //Return false if the Block Material is not a Chest
            if (block.getTypeId() != 54)
                return false;
            
            World blockWorld = block.getWorld();
            
            //Return whether the neighboring Block is a linked Chest
            return a != x + 1 && blockWorld.getBlockTypeIdAt(a + 1, y, z) == 54 ||
                    a != x - 1 && blockWorld.getBlockTypeIdAt(a - 1, y, z) == 54;
        }

        if (c != z) {
            //Return false if the Block Material is not a Chest
            if (block.getTypeId() != 54)
                return false;
            
            World blockWorld = block.getWorld();
            
            //Return whether the neighboring Block is a linked Chest
            return c != z + 1 && blockWorld.getBlockTypeIdAt(x, y, c + 1) == 54 ||
                    c != z - 1 && blockWorld.getBlockTypeIdAt(x, y, c - 1) == 54;
        }
        
        return true;
    }

    /**
     * Returns the String representation of this PhatLootsChest
     * The format of the returned String is as follows
     * world'x'y'z'{Player1@Days'Hours'Minutes'Seconds, Player1@Days'Hours'Minutes'Seconds}
     * 
     * @return The String representation of this Button
     */
    @Override
    public String toString() {
        String string = world+"'"+x+"'"+y+"'"+z+"{";

        Iterator itr = users.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            int[] time = getTime(key);

            string = string.concat(key+"@"+time[0]+"'"+time[1]+"'"+time[2]+"'"+time[3]);
            
            if (itr.hasNext())
                string = string.concat(", ");
        }

        return string.concat("}");
    }
}
package com.codisimus.plugins.phatloots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * API for the PhatLoots plugin
 *
 * @author Cody
 */
public class PhatLootsAPI {
    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block The given Block
     * @return true if the Block is a PhatLootChest
     */
    public static boolean isPhatLootChest(Block block) {
        return PhatLootChest.isPhatLootChest(block);
    }

    /**
     * Returns the PhatLoot of the given name
     *
     * @param phatLootName The name of the PhatLoot to find
     * @return The PhatLoot or null if there is none by that name
     */
    public static PhatLoot getPhatLoot(String phatLootName) {
        return PhatLoots.getPhatLoot(phatLootName);
    }

    /**
     * Rolls for loot of the specified loot tables
     * An empty list is returned if the PhatLoot of the given name does not exist
     *
     * @param phatLootName The name of the specified loot tables
     * @return The loot that was given from the roll
     */
    public static LootBundle rollForLoot(String phatLootName) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot(phatLootName);
        return phatLoot == null
               ? new LootBundle()
               : phatLoot.rollForLoot();
    }

    /**
     * Returns all PhatLoots that are linked to the given Block
     *
     * @param block The given Block
     * @return a list of PhatLoots linked to the Block
     */
    public static LinkedList<PhatLoot> getLinkedPhatLoots(Block block) {
        return PhatLoots.getPhatLoots(block);
    }

    /**
     * Returns all PhatLoots
     *
     * @return A collection of all PhatLoots
     */
    public static Collection<PhatLoot> getAllPhatLoots() {
        return PhatLoots.getPhatLoots();
    }

    /**
     * Returns all PhatLootChests
     *
     * @return A collection of all PhatLootChests
     */
    public static Collection<PhatLootChest> getAllPhatLootChests() {
        return PhatLootChest.chests.values();
    }

    /**
     * Returns all Blocks that are linked to a PhatLoot
     *
     * @return a List of Blocks that are PhatLootChests
     */
    public static ArrayList<Block> getAllPhatLootChestBlocks() {
        ArrayList<Block> blockList = new ArrayList<Block>();
        for (PhatLootChest chest : getAllPhatLootChests()) {
            blockList.add(chest.getBlock());
        }
        return blockList;
    }

    /**
     * Reloads the PhatLoot plugin
     * This includes save data and config settings
     */
    public static void reload() {
        PhatLoots.rl();
    }

    /**
     * Saves all plugin information
     * This includes loot tables, linked chests, and loot times
     * This is a lot of data writing and should not be called too often
     * It is better to save individual PhatLoots and only the modified information
     */
    public static void saveAll() {
        PhatLoots.saveAll();
    }
}

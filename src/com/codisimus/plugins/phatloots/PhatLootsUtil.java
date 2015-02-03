package com.codisimus.plugins.phatloots;

import java.util.Random;
import org.apache.commons.lang.WordUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Cody
 */
public class PhatLootsUtil {
    private static Random random = new Random();

    /**
     * Returns true if the given player is allowed to loot the specified PhatLoot
     *
     * @param player The Player who is being checked for permission
     * @param phatLoot The PhatLoot in question
     * @return true if the player is allowed to loot the PhatLoot
     */
    public static boolean canLoot(Player player, PhatLoot phatLoot) {
        //Check if the PhatLoot is restricted
        if (PhatLootsConfig.restrictAll || PhatLootsConfig.restricted.contains(phatLoot.name)) {
            return player.hasPermission("phatloots.loot.*") //Check for the loot all permission
                   ? true
                   : player.hasPermission("phatloots.loot." + phatLoot.name); //Check if the Player has the specific loot permission
        } else {
            return true;
        }
    }

    /**
     * Returns true if the given Block is a type that is able to be linked
     *
     * @param block the given Block
     * @return true if the given Block is able to be linked
     */
    public static boolean isLinkableType(Block block) {
        return PhatLoots.types.containsKey(block.getType());
    }

    /**
     * Returns a random int between 0 (inclusive) and y (inclusive)
     *
     * @param upper y
     * @return a random int between 0 and y
     */
    public static int rollForInt(int upper) {
        return random.nextInt(upper + 1); //+1 is needed to make it inclusive
    }

    /**
     * Returns a random int between x (inclusive) and y (inclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random int between x and y
     */
    public static int rollForInt(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Returns a random double between 0 (inclusive) and y (exclusive)
     *
     * @param upper y
     * @return a random double between 0 and y
     */
    public static double rollForDouble(double upper) {
        return random.nextDouble() * upper;
    }

    /**
     * Returns a random double between x (inclusive) and y (exclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random double between x and y
     */
    public static double rollForDouble(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Returns a user friendly String of the given ItemStack's name
     *
     * @param item The given ItemStack
     * @return The name of the item
     */
    public static String getItemName(ItemStack item) {
        //Return the Display name of the item if there is one
        if (item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        //A display name was not found so use a cleaned up version of the Material name
        return WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));
    }
}

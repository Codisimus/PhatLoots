package com.codisimus.plugins.phatloots;

import java.util.LinkedList;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A Loot has a probability of looting
 *
 * @author Cody
 */
public abstract class Loot implements Comparable, ConfigurationSerializable {
    static Loot last; //The last successfully loaded Loot (used for debugging)
    double probability = 100;

    /**
     * Adds the loot to the item list
     *
     * @param lootBundle The loot that has been rolled for
     * @param lootingBonus The increased chance of getting rarer loots
     */
    public abstract void getLoot(LootBundle lootBundle, double lootingBonus);

    /**
     * Returns the information of the loot in the form of an ItemStack
     *
     * @return An ItemStack representation of the Loot
     */
    public abstract ItemStack getInfoStack();

    /**
     * Returns the chance of looting
     *
     * @return The chance of looting
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Sets the Probability of looting
     *
     * @param probability The Probability to be set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * Rolls for the loot and returns true if it was a successful roll
     *
     * @param lootingBonus The increased chance of getting rarer loots
     * @return true if the loot should be looted
     */
    public boolean rollForLoot(double lootingBonus) {
        return roll() < (probability - lootingBonus);
    }

    /**
     * Returns a double between 0 (inclusive) and 100 (exclusive)
     *
     * @return The number that was rolled
     */
    public static double roll() {
        return PhatLoots.rollForDouble(100);
    }

    /**
     * Compares Loot objects to order them by their probability (lowest first)
     *
     * @param object An object which may not be an instance of Loot
     * @return 0 if the object is equal to this, 1 if the objects probability is lower, and -1 otherwise
     */
    @Override
    public int compareTo(Object object) {
        if ((object instanceof Loot)) {
            Loot loot = (Loot) object;
            if (loot.probability < probability) {
                return 1;
            }
            if (loot.probability > probability) {
                return -1;
            }
            if (equals(loot)) {
                return 0;
            }
        }
        return -1;
    }
}

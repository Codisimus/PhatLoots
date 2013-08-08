package com.codisimus.plugins.phatloots.loot;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A Collection is a list of Loots which has a unique name
 *
 * @author Cody
 */
@SerializableAs("LootCollection")
public class LootCollection extends Loot {
    public static boolean allowDuplicates;
    public String name;
    private int lowerNumberOfLoots;
    private int upperNumberOfLoots;
    private LinkedList<Loot> lootList;

    /**
     * Contructs a new LootCollection with the given name
     *
     * @param name The given name
     */
    public LootCollection(String name) {
        this.name = name;
        lowerNumberOfLoots = PhatLootsConfig.defaultLowerNumberOfLoots;
        upperNumberOfLoots = PhatLootsConfig.defaultUpperNumberOfLoots;
        lootList = new LinkedList<Loot>();
    }

    /**
     * Contructs a new LootCollection with the given name and range
     *
     * @param name The given name
     * @param lowerNumberOfLoots The lower bound of the range
     * @param upperNumberOfLoots The upper bound of the range
     */
    public LootCollection(String name, int lowerNumberOfLoots, int upperNumberOfLoots) {
        this.name = name;
        this.lowerNumberOfLoots = lowerNumberOfLoots;
        this.upperNumberOfLoots = upperNumberOfLoots;
        lootList = new LinkedList<Loot>();
    }

    /**
     * Constructs a new LootCollection from a Configuration Serialized phase
     *
     * @param map The map of data values
     */
    public LootCollection(Map<String, Object> map) {
        String currentLine = null; //The value that is about to be loaded (used for debugging)
        try {
            probability = (Double) map.get(currentLine = "Probability");
            name = (String) map.get(currentLine = "Name");
            lowerNumberOfLoots = (Integer) map.get(currentLine = "LowerNumberOfLoots");
            upperNumberOfLoots = (Integer) map.get(currentLine = "UpperNumberOfLoots");
            lootList = new LinkedList<Loot>((List) map.get(currentLine = "LootList"));
        } catch (Exception ex) {
            //Print debug messages
            PhatLoots.logger.severe("Failed to load LootCollection line: " + currentLine);
            PhatLoots.logger.severe("of PhatLoot: " + (PhatLoot.current == null ? "unknown" : PhatLoot.current));
            PhatLoots.logger.severe("Last successfull load was...");
            PhatLoots.logger.severe("PhatLoot: " + (PhatLoot.last == null ? "unknown" : PhatLoot.last));
            PhatLoots.logger.severe("Loot: " + (Loot.last == null ? "unknown" : Loot.last.toString()));
        }
    }

    /**
     * Rolls for each Loot within the collection
     *
     * @param lootBundle The loot that has been rolled for
     * @param lootingBonus The increased chance of getting rarer loots
     */
    @Override
    public void getLoot(LootBundle lootBundle, double lootingBonus) {
        if (isRollForEach()) { //Roll for each Loot individually
            for (Loot loot : lootList) {
                if (loot.rollForLoot(lootingBonus)) {
                    if (loot.rollForLoot(lootingBonus)) {
                        loot.getLoot(lootBundle, lootingBonus);
                    }
                }
            }
        } else { //Roll for all Loot collectively
            //Roll for the amount of loots
            int numberOfLoots = lowerNumberOfLoots == upperNumberOfLoots
                                ? lowerNumberOfLoots
                                : PhatLoots.rollForInt(lowerNumberOfLoots, upperNumberOfLoots);
            //Make sure there are items that will be looted before entering the loop
            if (!lootList.isEmpty()) {
                //Sort the loot from lowest probability to highest
                Collections.sort(lootList);
                int numberLooted = 0;
                LinkedList<Loot> removed = new LinkedList<Loot>();
                while (numberLooted < numberOfLoots) {
                    //Calculate the total probability to determine the maximum amount that can be rolled
                    double total = 0;
                    for (Loot loot : lootList) {
                        total += loot.probability;
                    }
                    //Roll a number between 0 and total and then subtract the looting bonus
                    //We subtract because a lower roll is better in this case
                    double roll = PhatLoots.rollForDouble(total) - lootingBonus;
                    for (Loot loot : lootList) {
                        //Subtract the probability of each Loot from the roll until we drop below 0
                        roll -= loot.getProbability();
                        if (roll <= 0) {
                            //Give this loot
                            loot.getLoot(lootBundle, lootingBonus);
                            if (!allowDuplicates) {
                                removed.add(loot);
                                lootList.remove(loot);
                            }
                            break;
                        }
                    }
                    numberLooted++;
                }
                //Readd all removed Loot
                lootList.addAll(removed);
            }
        }
    }

    /**
     * Returns the list of loot in the collection
     *
     * @return The list of Loot in this Collection
     */
    public LinkedList<Loot> getLootList() {
        return lootList;
    }

    /**
     * Returns the information of the LootCollection in the form of an ItemStack
     *
     * @return An ItemStack representation of the collection
     */
    @Override
    public ItemStack getInfoStack() {
        //A LootCollection is represented by an Ender Chest
        ItemStack infoStack = new ItemStack(Material.ENDER_CHEST);

        //Set the display name to the name of the collection
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2" + name + " (Collection)");

        //Add more specific details of the collection
        List<String> details = new ArrayList();
        details.add("§1Probability: §6" + probability);
        if (isRollForEach()) {
            details.add("§6Each loot is rolled for");
        } else if (lowerNumberOfLoots == upperNumberOfLoots) {
            details.add("§1Number of Loots: §6" + lowerNumberOfLoots);
        } else {
            details.add("§1Number of Loots: §6" + lowerNumberOfLoots + '-' + upperNumberOfLoots);
        }

        //Construct the ItemStack and return it
        info.setLore(details);
        infoStack.setItemMeta(info);
        return infoStack;
    }

    /**
     * Returns true if the number of loots for this collection is not positive
     *
     * @return true if each loot should be rolled for separately
     */
    public boolean isRollForEach() {
        return upperNumberOfLoots <= 0;
    }

    /**
     * Adds the given loot to this collection
     *
     * @param target the given Loot
     * @return true if it was successfully added
     */
    public boolean addLoot(Loot target) {
        for (Loot loot : lootList) {
            if (loot.equals(target)) {
                return false;
            }
        }
        lootList.add(target);
        return true;
    }

    /**
     * Removes the given loot from this collection
     *
     * @param target the given Loot
     * @return false if it was not found
     */
    public boolean removeLoot(Loot target) {
        Iterator<Loot> itr = lootList.iterator();
        while (itr.hasNext()) {
            if (itr.next().equals(target)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the collection of the given name
     *
     * @param target The name of the collection to search for
     * @return The LootCollection or null if it was not found
     */
    public LootCollection findCollection(String target) {
        //First check if this is the collection in question
        if (name.equals(target)) {
            return this;
        }
        //Scan each loot object
        for (Loot loot : lootList) {
            if (loot instanceof LootCollection) {
                //Recursively look for the collection
                LootCollection coll = ((LootCollection) loot).findCollection(target);
                if (coll != null) {
                    return coll;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (Collection)";
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LootCollection)) {
            return false;
        }

        LootCollection loot = (LootCollection) object;
        return loot.name.equals(name);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map map = new TreeMap();
        map.put("Probability", probability);
        map.put("Name", name);
        map.put("LowerNumberOfLoots", lowerNumberOfLoots);
        map.put("UpperNumberOfLoots", upperNumberOfLoots);
        map.put("LootList", lootList);
        return map;
    }
}

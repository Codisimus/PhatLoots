package com.codisimus.plugins.phatloots;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A Collection is a list of Loots which has a unique name
 *
 * @author Codisimus
 */
@SerializableAs("LootCollection")
public class LootCollection extends Loot {
    String name;
    int lowerNumberOfLoots;
    int upperNumberOfLoots;
    LinkedList<Loot> lootList;

    public LootCollection(String name) {
        this.name = name;
        lowerNumberOfLoots = PhatLootsConfig.defaultLowerNumberOfLoots;
        upperNumberOfLoots = PhatLootsConfig.defaultUpperNumberOfLoots;
        lootList = new LinkedList<Loot>();
    }

    public LootCollection(Map<String, Object> map) {
        probability = (Double) map.get("Probability");
        name = (String) map.get("Name");
        lowerNumberOfLoots = (Integer) map.get("LowerNumberOfLoots");
        upperNumberOfLoots = (Integer) map.get("UpperNumberOfLoots");
        lootList = new LinkedList<Loot>((List) map.get("LootList"));
    }

    @Override
    public void getLoot(Player player, double lootingBonus, LinkedList<ItemStack> items) {
        if (upperNumberOfLoots > 0) {
            int numberOfLoots = PhatLoots.random.nextInt(upperNumberOfLoots - lowerNumberOfLoots) + lowerNumberOfLoots;
            //Make sure there are items that will be looted before entering the loop
            if (!lootList.isEmpty()) {
                //Do not loot if the probability does not add up to 100
                if (getPercentRemaining() != 0) {
                    PhatLoots.logger.warning("Cannot loot Collection " + name +
                            " because the probability does not equal 100%");
                } else {
                    //Roll for weighted loot
                    int numberLooted = 0;
                    while (numberLooted < numberOfLoots) {
                        int j = Math.min(100, PhatLoots.random.nextInt(100) + (int) lootingBonus);
                        for (Loot loot : lootList) {
                            j -= loot.getProbability();
                            if (j <= 0) {
                                loot.getLoot(player, lootingBonus, items);
                                break;
                            }
                        }
                        numberLooted++;
                    }
                }
            }
        } else {
            for (Loot loot : lootList) {
                if (loot.rollForLoot(lootingBonus)) {
                    loot.getLoot(player, lootingBonus, items);
                }
            }
        }
    }

    /**
     * Returns the remaining percent of the collection
     *
     * @return Total probability of all Loots in the collection subtracted from 100
     */
    public double getPercentRemaining() {
        //Subtract the probabilty of each loot from 100
        double total = 100;
        for (Loot loot : lootList) {
            total -= loot.getProbability();
        }
        return total;
    }

    @Override
    public String toString() {
        return null;
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

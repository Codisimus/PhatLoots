package com.codisimus.plugins.phatloots;

import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * A Loot is a ItemStack and with a probability of looting
 * 
 * @author Codisimus
 */
public class Loot {
    private ItemStack item;
    private int bonus = 0;
    private double probability;

    /**
     * Constructs a new Loot with the given Item data and probability
     * 
     * @param id The Material id of the item
     * @param durability The durability of the item
     * @param amountLower The lower bound of the stack size of the item
     * @param amountUpper The upper bound of the stack size of the item
     * @param probability The chance of looting the item
     */
    public Loot (int id, short durability, int amountLower, int amountUpper, double probability) {
        item = new ItemStack(id, amountLower);
        if (durability >= 0)
            item.setDurability(durability);
        
        bonus = amountUpper - amountLower;
        this.probability = probability;
    }
    
    /**
     * Constructs a new Loot with the given Item data and probability
     * 
     * @param id The Material id of the item
     * @param enchantments The enchantments on the item
     * @param amountLower The lower bound of the stack size of the item
     * @param amountUpper The upper bound of the stack size of the item
     * @param probability The chance of looting the item
     */
    public Loot (int id, Map<Enchantment, Integer> enchantments, int amountLower, int amountUpper, double probability) {
        item = new ItemStack(id, amountLower);
        item.addEnchantments(enchantments);
        
        bonus = amountUpper - amountLower;
        this.probability = probability;
    }
    
    /**
     * Constructs a new Loot with the given ItemStack and probability
     * 
     * @param item The ItemStack that will be looted
     * @param probability The chance of looting the item
     */
    public Loot (ItemStack item, int bonus, double probability) {
        this.item = item;
        this.bonus = bonus;
        this.probability = probability;
    }
    
    /**
     * Returns the item with the bonus amount
     * 
     * @return The item with the bonus amount
     */
    public ItemStack getItem() {
        return bonus == 0 ? item : new ItemStack(item.getTypeId(), item.getAmount() +
                PhatLoots.random.nextInt(bonus), item.getDurability(), item.getData().getData());
    }
    
    /**
     * Returns the chance of looting
     * 
     * @return The chance of looting
     */
    public double getProbability() {
        return probability;
    }
    
    /**
     * Returns the Enchantments of this Loot as a String
     * 
     * @return The String representation of this Loot's Enchantments
     */
    public String enchantmentsToString() {
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        String string = "";
        for (Enchantment enchantment: enchantments.keySet()) {
            string = string.concat("&"+enchantment.getName());
            
            int level = enchantments.get(enchantment);
            if (level != enchantment.getStartLevel())
                string = string.concat("("+enchantments.get(enchantment)+")");
        }
        return string.substring(1);
    }
    
    /**
     * Returns the info of this Loot as a String
     * This String is user friendly
     * 
     * @return The String representation of this Loot
     */
    public String toInfoString() {
        short durability = item.getDurability();
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        int amount = item.getAmount();
        return amount+(bonus == 0 ? " of " : "-"+(amount + bonus)+" of ")+item.getType().name()+
                (!enchantments.isEmpty() ? " with enchantments "+enchantmentsToString() :
                (durability > 0 ? " with data "+durability : ""))+" @ "+
                (Math.floor(probability) == probability ? (int)probability : probability)+"%";
    }
    
    /**
     * Returns the String representation of this Loot
     * The format for a Loot with Enchantments is MaterialID'Enchantment1(level)&Enchantment2(level)...'Amount'Probability
     * The format for a Loot without Enchantments is MaterialID'Durability'Amount'Probability
     * 
     * @return The String representation of this Loot
     */
    @Override
    public String toString() {
        String string = item.getTypeId()+"'";
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        string = string.concat(enchantments.isEmpty() ? String.valueOf(item.getDurability()) : enchantmentsToString());
        string = string.concat("'"+item.getAmount());
        string = string.concat(bonus == 0 ? "'" : ("-"+(item.getAmount() + bonus)+"'"));
        
        if (Math.floor(probability) == probability)
            string = string.concat(String.valueOf((int)probability));
        else
            string = string.concat(String.valueOf(probability));
        
        return string;
    }
    
    /**
     * Compares the  properties of this Loot with the given Loot
     * 
     * @param object The given Loot
     * @return true if both of the Loot Objects represent the same Loot
     */
    @Override
    public boolean equals(Object object) {
        Loot loot = (Loot)object;
        return probability == loot.probability && item.getTypeId() == loot.item.getTypeId()
                && item.getAmount() == loot.item.getAmount() && bonus == loot.bonus &&
                item.getDurability() == loot.item.getDurability();
    }
}
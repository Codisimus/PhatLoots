package com.codisimus.plugins.phatloots;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 * A LootBundle is a collection of items, commands, money, and experience that has been rolled for
 *
 * @author Cody
 */
public class LootBundle {
    private List<ItemStack> itemList;
    private List<CommandLoot> commandList = new LinkedList<CommandLoot>();
    private double money = 0;
    private int exp = 0;

    public LootBundle(List<ItemStack> itemList) {
        this.itemList = itemList;
    }

    public LootBundle() {
        this.itemList = new LinkedList<ItemStack>();
    }

    public List<ItemStack> getItemList() {
        return itemList;
    }

    public void addItem(ItemStack item) {
        itemList.add(item);
    }

    public List<CommandLoot> getCommandList() {
        return commandList;
    }

    public void addCommand(CommandLoot command) {
        commandList.add(command);
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }
}

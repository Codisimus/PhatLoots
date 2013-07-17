package com.codisimus.plugins.phatloots;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a Player loots a PhatLoot
 *
 * @author Cody
 */
public class PlayerLootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player looter;
    private PhatLoot phatLoot;
    private PhatLootChest chest;
    private LootBundle lootBundle;
    private boolean cancelled;

    /**
     * Creates a new event with the given data
     *
     * @param looter The Player who is looting
     * @param itemList The items that will be looted
     * @param money The amount of money that the Player will loot
     * @param exp The amount of experience that will be looted
     * @param phatLoot The PhatLoot that the Player looted
     * @param chest The PhatLootChest being looted or null if no chest was involved
     */
    public PlayerLootEvent(Player looter, PhatLoot phatLoot, PhatLootChest chest, LootBundle lootBundle) {
        this.looter = looter;
        this.phatLoot = phatLoot;
        this.chest = chest;
        this.lootBundle = lootBundle;
    }

    /**
     * Returns the Player who is looting
     *
     * @return The Player who is looting
     */
    public Player getLooter() {
        return looter;
    }

    /**
     * Returns the PhatLoot that provided the loot
     *
     * @return The PhatLoot that provided the loot
     */
    public PhatLoot getPhatLoot() {
        return phatLoot;
    }

    /**
     * Returns the chest that is being looted
     *
     * @return The PhatLootChest being looted or null if no chest was involved
     */
    public PhatLootChest getChest() {
        return chest;
    }

    /**
     * Returns the loot that will be added to the chest
     *
     * @return the bundle of loot that will be looted
     */
    public LootBundle getLootBundle() {
        return lootBundle;
    }

    /**
     * Returns the items that will be looted
     *
     * @return A list of items that will be looted
     */
    public List<ItemStack> getItemList() {
        return lootBundle.getItemList();
    }

    /**
     * Returns the amount of money that the Player will loot
     *
     * @return The amount of money that the Player will loot
     */
    public double getMoney() {
        return lootBundle.getMoney();
    }

    /**
     * Sets the amount of money to be looted by the player
     *
     * @param money The amount of money to be looted
     */
    public void setMoney(double money) {
        lootBundle.setMoney(money);
    }

    /**
     * Returns the amount of experience that will be looted
     *
     * @return The amount of experience looted
     */
    public int getExp() {
        return lootBundle.getExp();
    }

    /**
     * Sets the amount of experience to be looted
     *
     * @param money The amount of experience to be looted
     */
    public void setExp(int exp) {
        lootBundle.setExp(exp);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean arg) {
        cancelled = arg;
    }
}

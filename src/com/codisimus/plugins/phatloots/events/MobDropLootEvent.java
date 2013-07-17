package com.codisimus.plugins.phatloots.events;

import com.codisimus.plugins.phatloots.loot.LootBundle;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a mob is killed and drops custom loot
 *
 * @author Cody
 */
public class MobDropLootEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private LivingEntity mob;
    private Player killer;
    private LootBundle lootBundle;
    private boolean cancelled;

    /**
     * Creates a new event with the given data
     *
     * @param mob The mob that was killed
     * @param killer The Player who killed the mob or null if the mob died of natural causes
     * @param lootBundle The bundle of loot dropped by the mob
     */
    public MobDropLootEvent(LivingEntity mob, Player killer, LootBundle lootBundle) {
        this.mob = mob;
        this.killer = killer;
        this.lootBundle = lootBundle;
    }

    /**
     * Returns the mob that was killed
     *
     * @return The mob that was killed
     */
    public LivingEntity getMob() {
        return mob;
    }

    /**
     * Returns the Player who killed the mob
     *
     * @return The Player who killed the mob or null if the mob died of natural causes
     */
    public Player getKiller() {
        return killer;
    }

    /**
     * Returns the loot that will be dropped by the mob
     *
     * @return the bundle of loot that will be dropped by the mob
     */
    public LootBundle getLootBundle() {
        return lootBundle;
    }

    /**
     * Returns the items that will be dropped by the mob
     *
     * @return A list of items that will be dropped by the mob
     */
    public List<ItemStack> getDrops() {
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
     * Returns the amount of experience that will be dropped by the mob
     *
     * @return The amount of experience dropped
     */
    public int getExp() {
        return lootBundle.getExp();
    }

    /**
     * Sets the amount of experience to be dropped by the mob
     *
     * @param money The amount of experience to be dropped
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

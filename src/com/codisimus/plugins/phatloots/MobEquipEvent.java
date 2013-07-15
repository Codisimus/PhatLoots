package com.codisimus.plugins.phatloots;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EntityEquipment;

/**
 * Called when a mob is spawned and given a weapon/armor
 *
 * @author Cody
 */
public class MobEquipEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private LivingEntity mob;
    private boolean cancelled;

    /**
     * Creates a new event with the given data
     *
     * @param mob The mob whose equipment was modified
     */
    public MobEquipEvent(LivingEntity mob) {
        this.mob = mob;
    }

    /**
     * Returns the mob that was spawned
     *
     * @return The mob that was spawned
     */
    public LivingEntity getMob() {
        return mob;
    }

    /**
     * Returns the Equipment that the mob spawned with
     *
     * @return The EntityEquipment of the mob
     */
    public EntityEquipment getEquipment() {
        return mob.getEquipment();
    }

    /**
     * Returns the custom name of the mob
     *
     * @return The name of the mob
     */
    public String getCustomName() {
        return mob.getCustomName();
    }

    /**
     * Set the custom name of the mob
     *
     * @param name The new name for the mob
     */
    public void getCustomName(String name) {
        mob.setCustomName(name);
        mob.setCustomNameVisible(true);
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

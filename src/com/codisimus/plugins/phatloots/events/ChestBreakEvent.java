package com.codisimus.plugins.phatloots.events;

import com.codisimus.plugins.phatloots.PhatLootChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a PhatLootChest breaks
 *
 * @author Cody
 */
public class ChestBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Player lastLooter;
    private PhatLootChest chest;
    private long respawnTime;
    private boolean cancelled;

    /**
     * Creates a new event with the given data
     *
     * @param lastLooter The Player who was last seen looting the chest
     * @param chest The PhatLootChest being looted or null if no chest was involved
     * @param respawnTime The amount of time (in ticks) until the chest respawns or -1 if it never respawns
     */
    public ChestBreakEvent(Player lastLooter, PhatLootChest chest, long respawnTime) {
        this.lastLooter = lastLooter;
        this.chest = chest;
    }

    /**
     * Returns the Player who was last seen looting the chest
     *
     * @return The Player who was last seen looting the chest
     */
    public Player getLastLooter() {
        return lastLooter;
    }

    /**
     * Returns the chest that broke
     *
     * @return The PhatLootChest that has been broken
     */
    public PhatLootChest getChest() {
        return chest;
    }

    /**
     * Returns the amount of time (in ticks) until the chest respawns
     *
     * @return The amount of time until the chest respawns or -1 if it never respawns
     */
    public long getRespawnTime() {
        return respawnTime;
    }

    /**
     * Sets how long until the chest respawns
     *
     * @param time The new time in ticks
     */
    public void setRespawnTime(long time) {
        respawnTime = time;
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

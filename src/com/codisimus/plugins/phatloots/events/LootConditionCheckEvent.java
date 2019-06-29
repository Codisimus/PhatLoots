package com.codisimus.plugins.phatloots.events;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.conditions.LootCondition;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * Called when the loot conditions for a PhatLoot are checked
 *
 * @author Redned
 */
public class LootConditionCheckEvent extends PhatLootsEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player looter;
    private final PhatLoot phatLoot;
    private List<LootCondition> lootConditions;

    /**
     * Creates a new event with the given data
     *
     * @param looter The Player who is looting
     * @param lootConditions The PhatLoot conditions
     */
    public LootConditionCheckEvent(Player looter, PhatLoot phatLoot, List<LootCondition> lootConditions) {
        this.looter = looter;
        this.phatLoot = phatLoot;
        this.lootConditions = lootConditions;
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
     * Returns the loot conditions for the given PhatLoot
     *
     * @return The loot conditions for the given PhatLoot
     */
    public List<LootCondition> getLootConditions() {
        return lootConditions;
    }

    /**
     * Sets the loot conditions for the given PhatLoot
     *
     * @param lootConditions The loot conditions you want the PhatLoot to check for this event
     */
    public void setLootConditions(List<LootCondition> lootConditions) {
        this.lootConditions = lootConditions;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
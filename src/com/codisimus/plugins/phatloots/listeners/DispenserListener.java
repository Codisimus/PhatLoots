package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * Listens for redstone activating PhatLoot Dispensers
 *
 * @author Codisimus
 */
public class DispenserListener implements Listener {
    /**
     * Checks if a PhatLoot Dispenser is powered
     *
     * @param event The BlockPhysicsEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onBlockPowered(BlockPhysicsEvent event) {
        //Check if the Block is a Dispenser/Dropper
        Block block = event.getBlock();
        switch (block.getType()) {
        case DISPENSER: break;
        case DROPPER: break;
        default: return;
        }

        //We don't care if a block loses power
        if (block.getBlockPower() == 0) {
            return;
        }

        //Return if the Dispenser is not a PhatLootChest
        if (!PhatLootChest.isPhatLootChest(block)) {
            return;
        }

        //Return if there are not any player that are close enough
        Player player = PhatLootsUtil.getNearestPlayer(block.getLocation());
        if (player == null) {
            return;
        }

        //Roll for linked loot
        PhatLootChest plChest = PhatLootChest.getChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots(block, player)) {
            if (PhatLootsUtil.canLoot(player, phatLoot)) {
                phatLoot.rollForChestLoot(player, plChest);
            }
        }
    }
}

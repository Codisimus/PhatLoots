package com.codisimus.plugins.phatloots;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.Inventory;

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
        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }

        //We don't care if a block loses power
        if (block.getBlockPower() == 0) {
            return;
        }

        //Return if the Dispenser is not a PhatLootChest
        if (!PhatLootChest.isPhatLootChest(block)) {
            return;
        }

        //Return if there is not any player that is close enough
        Player player = getNearestPlayer(block.getLocation());
        if (player == null) {
            return;
        }

        //Get the inventory of the Dispenser
        Dispenser dispenser = (Dispenser) block.getState();
        Inventory inventory = dispenser.getInventory();

        //Roll for linked loot
        PhatLootChest plChest = PhatLootChest.getChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots(block, player)) {
            if (PhatLoots.canLoot(player, phatLoot)) {
                phatLoot.rollForLoot(player, plChest, inventory);
            }
        }
    }

    /**
     * Returns the Player that is closest to the given Location
     * Returns null if no Players are within 50 Blocks
     *
     * @param location The given Location
     * @return the closest Player
     */
    private Player getNearestPlayer(Location location) {
        Player nearestPlayer = null;
        double shortestDistance = 2500;
        for (Player player: location.getWorld().getPlayers()) {
            Location playerLocation = player.getLocation();
            //Use the squared distance because is it much less resource intensive
            double distanceToPlayer = location.distanceSquared(playerLocation);
            if (distanceToPlayer < shortestDistance) {
                nearestPlayer = player;
                shortestDistance = distanceToPlayer;
            }
        }
        return nearestPlayer;
    }
}

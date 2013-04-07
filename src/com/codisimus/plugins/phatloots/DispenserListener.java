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

        if (block.getBlockPower() == 0) {
            return;
        }

        //Return if the Dispenser is not a PhatLootChest
        if (!PhatLoots.isPhatLootChest(block)) {
            return;
        }

        Player player = getNearestPlayer(block.getLocation());
        if (player == null) {
            return;
        }

        //Return if the Player does not have permission to receive loots
        if (!player.hasPermission("phatloots.use")) {
            player.sendMessage(PhatLootsConfig.permission);
            return;
        }

        Dispenser dispenser = (Dispenser) block.getState();
        Inventory inventory = dispenser.getInventory();

        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots(block, player)) {
            phatLoot.rollForLoot(player, plChest, inventory);
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
            double distanceToPlayer = location.distanceSquared(playerLocation);
            if (distanceToPlayer < shortestDistance) {
                nearestPlayer = player;
                shortestDistance = distanceToPlayer;
            }
        }
        return nearestPlayer;
    }
}

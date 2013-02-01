package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.regionown.Region;
import com.codisimus.plugins.regionown.RegionOwn;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    static String chestName;
    private static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>();

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Chest chest = null;
        Inventory inventory = null;
        Block block = event.getClickedBlock();
        switch (block.getType()) {
        case DISPENSER:
            //Return if the Dispenser was not punched
            if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                return;
            }

            //Return if the Dispenser is not a PhatLootChest
            if (!isPhatLootChest(block)) {
                return;
            }

            Dispenser dispenser = (Dispenser) block.getState();
            inventory = dispenser.getInventory();

            break;

        case CHEST: //Fall through
        case ENDER_CHEST:
            //Return if the Chest was not opened
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (block.getType() == Material.CHEST) {
                chest = (Chest) block.getState();
                inventory = chest.getInventory();

                //We only care about the left side because that is the Block that would be linked
                if (inventory instanceof DoubleChestInventory) {
                    chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                    block = chest.getBlock();
                }
            }

            //Return if the Chest is not a PhatLootChest
            LinkedList<PhatLoot> phatLoots = PhatLoots.getPhatLoots(block);
            if (phatLoots.isEmpty()) {
                return;
            }

            boolean global = true;
            for (PhatLoot phatLoot: phatLoots) {
                if (!phatLoot.global) {
                    global = false;
                }
            }

            //Create the custom key using the Player Name and Block location
            final String KEY = (global ? "global" : player.getName())
                                + "@" + block.getLocation().toString();

            //Grab the custom Inventory belonging to the Player
            ForgettableInventory fInventory = inventories.get(KEY);
            if (fInventory != null) {
                inventory = fInventory.getInventory();
            } else {
                String phatLootName = "PhatLoots Chest";
                for (PhatLoot phatLoot: phatLoots) {
                    if (PhatLoots.canLoot(player, phatLoot)) {
                        phatLootName = phatLoot.name;
                        break;
                    }
                }

                phatLootName = phatLootName.replace('_', ' ');
                String name = chestName.replace("<name>", phatLootName);

                //Create a new Inventory for the Player
                inventory = PhatLoots.server.createInventory(chest,
                                                             inventory == null
                                                             ? 27
                                                             : inventory.getSize()
                                                             , name);

                fInventory = new ForgettableInventory(PhatLoots.plugin, inventory) {
                    @Override
                    protected void execute() {
                        inventories.remove(KEY);
                    }
                };
                inventories.put(KEY, fInventory);
            }

            //Forget the Inventory in the scheduled time
            fInventory.schedule();

            //Swap the Inventories
            event.setCancelled(true);
            player.openInventory(inventory);
            PhatLoots.openInventory(player, inventory, block.getLocation(), global);

            break;

        default: return;
        }

        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest) && PhatLoots.canLoot(player, phatLoot)) {
                phatLoot.rollForLoot(player, plChest, inventory);
                phatLoot.save();
            }
        }
    }

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Chest) {
            HumanEntity human = event.getPlayer();
            if (human instanceof Player) {
                Player player = (Player) human;
                Location location = ((Chest) holder).getLocation();
                String key = "global@" + location.toString();
                if (inventories.containsKey(key)) {
                    PhatLoots.closeInventory(player, inv, location, true);
                } else if (inventories.containsKey(player.getName() + key.substring(6))) {
                    PhatLoots.closeInventory(player, inv, location, false);
                }
            }
        }
    }

    /**
     * Checks if a PhatLootChest is powered
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
        if (!isPhatLootChest(block)) {
            return;
        }

        Player player = getNearestPlayer(block.getLocation());
        if (player == null) {
            return;
        }

        //Return if the Player does not have permission to receive loots
        if (!PhatLoots.hasPermission(player, "use")) {
            player.sendMessage(PhatLootsMessages.permission);
            return;
        }

        Dispenser dispenser = (Dispenser) block.getState();
        Inventory inventory = dispenser.getInventory();

        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest) && PhatLoots.canLoot(player, phatLoot)) {
                phatLoot.rollForLoot(player, plChest, inventory);
                phatLoot.save();
            }
        }
    }

    /**
     * Prevents non-admins from breaking PhatLootsChests
     *
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        //Return if the Material of the Block is not a Chest or Furnace
        Block block = event.getBlock();
        switch (block.getType()) {
            case ENDER_CHEST: break;
            case CHEST: break;
            case FURNACE: break;
            default: return;
        }

        if (!isPhatLootChest(block)) {
            return;
        }

        //Cancel if the Block was not broken by a Player
        Player player = event.getPlayer();
        if (player == null) {
            event.setCancelled(true);
            return;
        }

        //Cancel if the Block was not broken by an Admin
        if (!PhatLoots.hasPermission(player, "admin")) {
            player.sendMessage(PhatLootsMessages.permission);
            event.setCancelled(true);
        }
    }

    /**
     * Manages Mob drops
     *
     * @param event The EntityDeathEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Location location = entity.getLocation();
        String name = entity.getType().getName();

        if (PhatLoots.pm.isPluginEnabled("RegionOwn")) {
            for (Region region : RegionOwn.mobRegions.values()) {
                if (region.contains(location)) {
                    name += "@" + region.name;
                }
            }
        }

        PhatLoot phatLoot = PhatLoots.getPhatLoot(name);
        if (phatLoot != null) {
            event.setDroppedExp(phatLoot.rollForLoot(entity.getKiller(), event.getDrops()));
        }
    }

    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public boolean isPhatLootChest(Block block) {
        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest)) {
                return true;
            }
        }
        return false;
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

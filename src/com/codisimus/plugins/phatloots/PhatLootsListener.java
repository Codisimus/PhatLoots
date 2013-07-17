package com.codisimus.plugins.phatloots;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Cody
 */
public class PhatLootsListener implements Listener {
    static EnumMap<Material, HashMap<String, String>> types = new EnumMap(Material.class); //Material -> World Name -> PhatLoot Name

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Return if the Block is not a linkable type
        Material type = event.getClickedBlock().getType();
        if (!types.containsKey(type)) {
            return;
        }

        Player player = event.getPlayer();
        LinkedList<PhatLoot> phatLoots = null;

        //Check if this Block has been automatically linked
        if (types.get(type) != null) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            HashMap<String, String> map = types.get(type);
            String world = player.getWorld().getName();
            String pName = map.containsKey(world)
                           ? map.get(world)
                           : map.get("all");
            PhatLoot phatLoot = PhatLoots.getPhatLoot(pName);
            if (phatLoot == null) {
                PhatLoots.logger.warning("PhatLoot " + pName + " does not exist.");
                PhatLoots.logger.warning("Please adjust your config or create the PhatLoot");
            }

            phatLoots = new LinkedList<PhatLoot>();
            phatLoots.add(phatLoot);
        }


        Block block = event.getClickedBlock();
        if (!PhatLootChest.isPhatLootChest(block)) {
            return;
        }
        PhatLootChest plChest = PhatLootChest.getChest(block);

        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            //PhatLoot Dispensers may be activated by punching them
            if (type == Material.DISPENSER) {
                if (phatLoots == null) {
                    //Return if the Dispenser is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }
                break;
            }
            return;

        case RIGHT_CLICK_BLOCK:
            if (phatLoots == null) {
                //Return if the Block is not a PhatLootChest
                phatLoots = PhatLoots.getPhatLoots(block, player);
                if (phatLoots.isEmpty()) {
                    switch (type) {
                    case TRAPPED_CHEST:
                    case CHEST:
                        Chest chest = (Chest) block.getState();
                        Inventory inventory = chest.getInventory();
                        //We only care about the left side because that is the Block that would be linked
                        if (inventory instanceof DoubleChestInventory) {
                            chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                            block = chest.getBlock();
                            phatLoots = PhatLoots.getPhatLoots(block, player);
                            if (phatLoots.isEmpty()) {
                                return;
                            } else {
                                break;
                            }
                        }
                    default:
                        return;
                    }
                }
            }
            break;

        default: return;
        }

        //Cancel any interaction events
        event.setCancelled(true);

        //Roll for Loot of each linked PhatLoot
        for (PhatLoot phatLoot : phatLoots) {
            phatLoot.rollForChestLoot(player, plChest);
        }
    }

    /**
     * Listens for a Player closing a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (human instanceof Player) {
            Player player = (Player) human;
            if (PhatLootChest.openPhatLootChests.containsKey(player)) {
                PhatLootChest chest = PhatLootChest.openPhatLootChests.get(player);
                boolean global = ForgettableInventory.has("global@" + chest.toString());
                chest.closeInventory(player, event.getInventory(), global);
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
        //Return if the Material of the Block is not a linkable type
        Block block = event.getBlock();
        if (!PhatLoots.isLinkableType(block)) {
            return;
        }

        //Return if the Block is not a PhatLootChest
        if (!PhatLootChest.isPhatLootChest(block)) {
            return;
        }

        //Cancel if the Block was not broken by a Player
        Player player = event.getPlayer();
        if (player == null) {
            event.setCancelled(true);
            return;
        }

        //Cancel if the Block was not broken by an Admin
        if (!player.hasPermission("phatloots.admin")) {
            player.sendMessage(PhatLootsConfig.permission);
            event.setCancelled(true);
        }

        //Unlink the broken Block
        for (PhatLoot phatLoot : PhatLootsCommand.getPhatLoots(player, null)) {
            phatLoot.removeChest(block);
            player.sendMessage("§5Broken " + block.getType().toString() + " has been unlinked from PhatLoot §6" + phatLoot.name);
            phatLoot.saveChests();
        }
    }
}

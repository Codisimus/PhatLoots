package com.codisimus.plugins.phatloots;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
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
import org.bukkit.inventory.InventoryHolder;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    static String chestName;
    static EnumMap<Material, String> types = new EnumMap(Material.class);
    private static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>();

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Material type = block.getType();
        if (!types.containsKey(type)) {
            return;
        }

        Player player = event.getPlayer();
        Inventory inventory = null;
        LinkedList<PhatLoot> phatLoots = null;

        if (types.get(type) != null) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            PhatLoot phatLoot = PhatLoots.getPhatLoot(types.get(type));
            if (phatLoot == null) {
                PhatLoots.logger.warning("PhatLoot " + types.get(type) + " does not exist");
            }

            phatLoots = new LinkedList<PhatLoot>();
            phatLoots.add(phatLoot);
        }

        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            if (type == Material.DISPENSER) {
                if (phatLoots == null) {
                    //Return if the Dispenser is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                Dispenser dispenser = (Dispenser) block.getState();
                inventory = dispenser.getInventory();
                break;
            }
            return;

        case RIGHT_CLICK_BLOCK:
            switch (type) {
            case TRAPPED_CHEST:
            case CHEST:
                Chest chest = (Chest) block.getState();
                inventory = chest.getInventory();

                if (phatLoots == null) {
                    //We only care about the left side because that is the Block that would be linked
                    if (inventory instanceof DoubleChestInventory) {
                        chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                        block = chest.getBlock();
                    }

                    //Return if the Chest is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                boolean individual = false;
                for (PhatLoot phatLoot : phatLoots) {
                    if (!phatLoot.global) {
                        individual = true;
                        break;
                    }
                }

                if (individual) {
                    //Create the custom key using the Player Name and Block location
                    final String KEY = player.getName() + "@" + block.getLocation().toString();

                    //Grab the custom Inventory belonging to the Player
                    ForgettableInventory fInventory = inventories.get(KEY);
                    if (fInventory != null) {
                        inventory = fInventory.getInventory();
                    } else {
                        String name = chestName.replace("<name>", phatLoots.getFirst().name.replace('_', ' '));

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
                    PhatLoots.openInventory(player, inventory, block.getLocation(), false);
                }

                break;

            default:
                if (phatLoots == null) {
                    //Return if the Block is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                boolean global = true;
                for (PhatLoot phatLoot : phatLoots) {
                    if (!phatLoot.global) {
                        global = false;
                        break;
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
                    String name = chestName.replace("<name>", phatLoots.getFirst().name.replace('_', ' '));

                    //Create a new Inventory for the Player
                    inventory = PhatLoots.server.createInventory(null,
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

                if (type == Material.ENDER_CHEST) {
                    //Swap the Inventories
                    event.setCancelled(true);
                    PhatLoots.openInventory(player, inventory, block.getLocation(), global);
                }
                player.openInventory(inventory);

                break;
            }

        default: return;
        }

        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : phatLoots) {
            phatLoot.rollForLoot(player, plChest, inventory);
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
        if (!player.hasPermission("phatloots.admin")) {
            player.sendMessage(PhatLootsConfig.permission);
            event.setCancelled(true);
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
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest)) {
                return true;
            }
        }
        return false;
    }
}

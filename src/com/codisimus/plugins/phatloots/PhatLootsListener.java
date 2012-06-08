package com.codisimus.plugins.phatloots;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    private static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>();
    static String chestName;
    
    /**
     * Attempts to load OldChestData for newly loaded Worlds
     * 
     * @param event The WorldLoadEvent that occurred
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        FileInputStream fis = null;
        for (File file: new File(PhatLoots.dataFolder+"/PhatLoots/").listFiles()) {
            String name = file.getName();
            if (name.endsWith(".properties"))
                try {
                    //Load the Properties file for reading
                    Properties p = new Properties();
                    fis = new FileInputStream(file);
                    p.load(fis);
                    
                    if (p.containsKey("OldChestsData"))
                        PhatLoots.getPhatLoot(name.substring(0, name.length() - 11)).setChests(p.getProperty("OldChestsData"));
                }
                catch (Exception loadFailed) {
                }
                finally {
                    try {
                        fis.close();
                    }
                    catch (Exception e) {
                    }
                }
        }
    }

    /**
     * Checks if a Player loots a PhatLootChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Return if the Event was cancelled
        if (event.isCancelled())
            return;
        
        Player player = event.getPlayer();
        Inventory inventory;
        Block block = event.getClickedBlock();
        switch (block.getType()) {
            case DISPENSER:
                //Return if the Dispenser was not punched
                if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    return;

                //Return if the Dispenser is not a PhatLootChest
                if (!isPhatLootChest(block))
                    return;

                //Return if the Player does not have permission to receive loots
                if (!PhatLoots.hasPermission(player, "use")) {
                    player.sendMessage("You do not have permission to receive loots.");
                    return;
                }
                
                Dispenser dispenser = (Dispenser)block.getState();
                inventory = dispenser.getInventory();
                
                break;
                
            case CHEST:
                //Return if the Chest was not opened
                if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                    return;

                Chest chest = (Chest)block.getState();
                inventory = chest.getInventory();
                
                //We only care about the left side because that is the Block that would be linked
                if (inventory instanceof DoubleChestInventory) {
                    chest = (Chest)((DoubleChestInventory)inventory).getLeftSide().getHolder();
                    block = chest.getBlock();
                }
                
                //Return if the Chest is not a PhatLootChest
                LinkedList<PhatLoot> phatLoots = PhatLoots.getPhatLoots(block);
                boolean global = true;
                for (PhatLoot phatLoot: phatLoots)
                    if (!phatLoot.global)
                        global = false;
                
                //Create the custom key using the Player Name and Block location
                final String KEY = (global ? "global" : player.getName())+"@"+block.getLocation().toString();
                
                //Grab the custom Inventory belonging to the Player
                ForgettableInventory fInventory = inventories.get(KEY);
                if (fInventory != null)
                    inventory = fInventory.getInventory();
                else {
                    //Create a new Inventory for the Player
                    String name = chestName;
                    name = name.replace("<name>", phatLoots.getFirst().name);
                        
                    inventory = PhatLoots.server.createInventory(chest, inventory.getSize(), name);
                    fInventory = new ForgettableInventory(PhatLoots.plugin, 600L, inventory) {
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

                //Return if the Player does not have permission to receive loots
                if (!PhatLoots.hasPermission(player, "use")) {
                    player.sendMessage("You do not have permission to receive loots.");
                    return;
                }
                
                break;
                
            default: return;
        }
        
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            
            if (chest != null) {
                phatLoot.getLoot(player, chest, inventory);
                phatLoot.save();
            }
        }
    }
    
    /**
     * Checks if a Player loots a PhatLootChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Chest))
            return;
        
        //Return if it was not a PhatLoots Inventory
        Inventory inventory = event.getInventory();
        if (!inventory.getName().equals(chestName))
            return;
        
        Chest chest = (Chest)holder;
        
        //Create the custom key using the Player Name and Block location
        final String KEY = event.getPlayer().getName()+"@"+chest.getBlock().getLocation().toString();
        
        //Grab the custom Inventory belonging to the Player
        ForgettableInventory fInventory = inventories.get(KEY);
        if (fInventory == null) { //Inventory lost (perhaps due to Server rl or the Player had the Inventory open for over the scheduled timee)
            //Save the Inventory for the Player
            fInventory = new ForgettableInventory(PhatLoots.plugin, 600L, inventory) {
                @Override
                protected void execute() {
                    inventories.remove(KEY);
                }
            };
            inventories.put(KEY, fInventory);
        }

        //Forget the Inventory in the scheduled time
        fInventory.schedule();
    }
    
    /**
     * Prevents non-admins from breaking PhatLootsChests
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        
        Block block = event.getBlock();
        
        //Return if the Material of the Block is not a Chest or Furnace
        switch (block.getType()) {
            case CHEST: break;
            case FURNACE: break;
            default: return;
        }
        
        if (!isPhatLootChest(block))
            return;
        
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
     * Returns true if the given Block is linked to a PhatLoot
     * 
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public boolean isPhatLootChest(Block block) {
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots())
            if (phatLoot.findChest(block) != null)
                return true;
        
        return false;
    }
}
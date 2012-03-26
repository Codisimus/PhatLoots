package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
    static HashMap<Inventory, Player> LastUser = new HashMap<Inventory, Player>();

    /**
     * Checks if a Player loots a PhatLootChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        
        Block block = event.getClickedBlock();
        
        //Return unless a Chest was opened or a Dispenser was punched
        switch (block.getType()) {
            case CHEST:
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                    break;
                
                return;
                
            case DISPENSER:
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    break;
                
                return;
                
            default: return;
        }
        
        if (!isPhatLootChest(block))
            return;
        
        Player player = event.getPlayer();
        boolean clear = false;
        
        //Check if the Block is a Chest
        if (block.getTypeId() == 54) {
            Player chestOpener = event.getPlayer();
            
            InventoryHolder chest = (InventoryHolder)block.getState();
            Inventory inventory = chest.getInventory();
            if (inventory instanceof DoubleChestInventory) {
                if (!((DoubleChestInventory)inventory).getLeftSide().getViewers().isEmpty()) {
                    event.setCancelled(true);
                    chestOpener.sendMessage(PhatLootsMessages.inUse);
                    return;
                }
            }
            else if (!inventory.getViewers().isEmpty()) {
                event.setCancelled(true);
                chestOpener.sendMessage(PhatLootsMessages.inUse);
                return;
            }
            
            //Clear the Chest if a new Player opened it
            //if (!LastUser.containsKey(inventory))
                //block = PhatLoots.getOtherHalf(block);
            if (!LastUser.containsKey(inventory) || !LastUser.get(inventory).equals(player))
                clear = true;
            
            LastUser.put(inventory, player);
        }
        
        //Return if the Player does not have permission to receive loots
        if (!PhatLoots.hasPermission(player, "use")) {
            player.sendMessage("You do not have permission to receive loots.");
            return;
        }
        
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            
            if (chest != null) {
                if (clear) {
                    chest.clear();
                    clear = false;
                }
                
                phatLoot.getLoot(player, chest);
                phatLoot.save();
            }
        }
    }
    
    /**
     * Prevents non-admins from breaking PhatLootsChests
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler
    public void onBlockBreak (BlockBreakEvent event) {
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
            player.sendMessage("You do not have permission to do that");
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
            for (PhatLootChest chest: phatLoot.chests)
                if (chest.isBlock(block))
                    return true;
        
        return false;
    }
}
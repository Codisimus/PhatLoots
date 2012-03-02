package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    private static HashMap<Player, Block> openChests = new HashMap<Player, Block>();
    static HashMap<Block, Player> LastUser = new HashMap<Block, Player>();

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
        
        //Check if the Block is a Chest
        if (block.getTypeId() == 54) {
            //Remove the Player as having the Chest open if they are offline
            for (Player key: openChests.keySet())
                if (!key.isOnline())
                    openChests.remove(key);

            Player chestOpener = event.getPlayer();
            
            Block chestBlock = openChests.containsValue(block) ? block : PhatLoots.getOtherHalf(block);

            //Check if the Chest is already in use
            if (openChests.containsValue(chestBlock)) {
                Block using = openChests.get(chestOpener);
                if (using == null || !using.equals(chestBlock)) {
                    event.setCancelled(true);
                    chestOpener.sendMessage(PhatLootsMessages.inUse);
                    return;
                }
            }

            openChests.put(chestOpener, block);
            
            //Clear the Chest if a new Player opened it
            if (!LastUser.containsKey(block) || !LastUser.get(block).equals(player)) {
                Chest chest = (Chest)block.getState();
                chest.getInventory().clear();
                chest.update();
                LastUser.put(block, player);
            }
        }
        
        //Return if the Player does not have permission to receive loots
        if (!PhatLoots.hasPermission(player, "use")) {
            player.sendMessage("You do not have permission to receive loots.");
            return;
        }
        
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            if (chest != null)
                phatLoot.getLoot(player, chest);
                phatLoot.save();
        }
    }
    
    /**
     * Listens for Players leaving Chests
     * 
     * @param event The PlayerMoveEvent that occurred
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerMove (PlayerMoveEvent event) {
        if (event.isCancelled())
            return;
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        
        if (openChests.containsKey(player))
            if ((int)to.getPitch() != (int)from.getPitch() ||
                    to.distance(openChests.get(player).getLocation()) > 8)
                openChests.remove(player);
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
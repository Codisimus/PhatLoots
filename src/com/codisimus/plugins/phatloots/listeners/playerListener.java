package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsMain;
import com.codisimus.plugins.phatloots.SaveSystem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Listens for Player's attempting to loot PhatLootsChests
 *
 * @author Codisimus
 */
public class playerListener extends PlayerListener {

    /**
     * Checks if a Player loots a PhatLootsChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        
        //Return unless a Chest was opened or a Furnace was punched
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (block.getTypeId() == 54)
                    break;
                
                return;
                
            case LEFT_CLICK_BLOCK:
                if (block.getTypeId() == 23)
                    break;
                
                break;
                
            default: return;
        }
        
        //Return if The Block is not linked to a PhatLoots
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        if (phatLoots == null)
            return;
        
        //Return if the Player does not have permission to receive loots
        Player player = event.getPlayer();
        if (!PhatLootsMain.hasPermission(player, "use")) {
            player.sendMessage("You do not have permission to receive loots.");
            return;
        }
        
        phatLoots.getLoot(player, block);
        SaveSystem.save();
    }
}
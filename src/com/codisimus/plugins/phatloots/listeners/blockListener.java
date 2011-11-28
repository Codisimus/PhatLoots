package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsMain;
import com.codisimus.plugins.phatloots.SaveSystem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

/**
 * Listens for griefing events
 *
 * @author Codisimus
 */
public class blockListener extends BlockListener {

    /**
     * Prevents non-admins from breaking PhatLootsChests
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        
        //Return if the Material of the Block is not a Chest or Furnace
        int id = block.getTypeId();
        if (id != 54 && id != 23)
            return;
        
        //Return if the Block is not linked to a PhatLoots
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        if (phatLoots == null)
            return;
        
        //Cancel if the Block was not broken by a Player
        Player player = event.getPlayer();
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        
        //Cancel if the Block was not broken by an Admin
        if (!PhatLootsMain.hasPermission(player, "admin")) {
            player.sendMessage("You do not have permission to do that");
            event.setCancelled(true);
        }
    }
}

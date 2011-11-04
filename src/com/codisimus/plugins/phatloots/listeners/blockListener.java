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
public class blockListener extends BlockListener{

    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        int id = block.getTypeId();
        if (id != 54 && id != 23)
            return;
        
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        
        if (phatLoots == null)
            return;
        
        Player player = event.getPlayer();
        
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        
        if (!PhatLootsMain.hasPermission(player, "admin")) {
            player.sendMessage("You do not have permission to do that");
            event.setCancelled(true);
        }
    }
}

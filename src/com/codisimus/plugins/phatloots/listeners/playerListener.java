package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsMain;
import com.codisimus.plugins.phatloots.SaveSystem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Listens for Player's attempting to loot PhatLootsChests
 *
 * @author Codisimus
 */
public class playerListener extends PlayerListener {

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (!(action.equals(Action.RIGHT_CLICK_BLOCK) && block.getTypeId() == 54) &&
                !(action.equals(Action.LEFT_CLICK_BLOCK) && block.getTypeId() == 23))
            return;
        
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        if (phatLoots == null && block.getTypeId() == 54) {
            block = PhatLootsMain.getBigChest(block);
            if (block != null)
                phatLoots = SaveSystem.findPhatLoots(block);
        }
        
        Player player = event.getPlayer();
        if (phatLoots == null)
            return;
        
        if (!PhatLootsMain.hasPermission(player, "use")) {
            player.sendMessage("You do not have permission to receive loots.");
            return;
        }
        
        if (event.isCancelled())
            return;
        
        phatLoots.getLoot(player, block);
        SaveSystem.save();
    }
}
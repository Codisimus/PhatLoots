package com.codisimus.plugins.phatloots;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

/**
 * Listens for Votes triggered by Votifier and gives the Voter Loot
 *
 * @author Cody
 */
public class VoteListener implements Listener {
    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        //Player should always be online because votes are not triggered until the user logs on
        Player player = Bukkit.getPlayerExact(vote.getUsername());
        if (player != null) {
            //Check if the Vote PhatLoot has been created
            PhatLoot phatLoot = PhatLoots.getPhatLoot("Vote");
            if (phatLoot == null) {
                PhatLoots.logger.severe("§4PhatLoot §6Vote§4 does not exist");
                return;
            }

            //Set the custom name of the PhatLoot Inventory
            String name = ChatColor.translateAlternateColorCodes('&', phatLoot.name);

            Inventory inventory = Bukkit.createInventory(player, 54, name);
            player.openInventory(inventory);
            phatLoot.rollForLoot(player, PhatLootChest.getTempChest(player.getLocation().getBlock()), inventory);
        }
    }
}

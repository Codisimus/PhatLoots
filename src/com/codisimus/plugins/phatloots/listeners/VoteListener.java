package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for Votes triggered by Votifier and gives the Voter Loot
 *
 * @author Codisimus
 */
public class VoteListener implements Listener {
    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        //Player should always be online because votes are not triggered until the user logs on
        Player player = Bukkit.getPlayerExact(vote.getUsername());
        if (player != null) {
            //Check for a World specific Vote PhatLoot first
            PhatLoot phatLoot = PhatLoots.getPhatLoot("Vote@" + player.getWorld().getName());
            if (phatLoot == null) {
                //Check if the default Vote PhatLoot has been created
                phatLoot = PhatLoots.getPhatLoot("Vote");
            }
            if (phatLoot == null) {
                PhatLoots.logger.severe("§4PhatLoot §6Vote§4 does not exist");
            } else {
                phatLoot.rollForLoot(player);
            }
        }
    }
}

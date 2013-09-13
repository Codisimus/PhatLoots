package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for NPC Deaths and checks if there is a PhatLoot named after them
 *
 * @author Cody
 */
public class CitizensListener implements Listener {
    @EventHandler (ignoreCancelled = true)
    public void onNPCDeath(NPCDeathEvent event) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot("NPC" + event.getNPC().getName());
        if (phatLoot != null) {
            event.setDroppedExp(phatLoot.rollForMobDrops(null, event.getNPC().getBukkitEntity().getKiller(), event.getDrops()));
        }
    }
}

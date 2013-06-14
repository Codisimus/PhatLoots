package com.codisimus.plugins.phatloots;

import me.ThaH3lper.com.Api.BossDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for EpicBoss Deaths and checks if there is a PhatLoot named after them
 *
 * @author Codisimus
 */
public class EBRListener implements Listener {
    @EventHandler (ignoreCancelled = true)
    public void onBossDeath(BossDeathEvent event) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot(event.getBossName());
        if (phatLoot != null) {
            event.setExp(phatLoot.rollForLoot(event.getPlayer(), event.getDrops()));
        }
    }
}

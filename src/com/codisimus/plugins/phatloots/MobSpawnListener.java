package com.codisimus.plugins.phatloots;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Listens for Mob armor and weapon when they spawn
 *
 * @author Codisimus
 */
public class MobSpawnListener extends MobListener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        PhatLoots.server.getScheduler().runTask(PhatLoots.plugin, new Runnable() {
                @Override
                public void run() {
                    LivingEntity entity = event.getEntity();
                    PhatLoot phatLoot = getPhatLoot(entity);
                    if (phatLoot != null) {
                        phatLoot.rollForLoot(entity);
                    }
                }
            });
    }

    @Override
    String getType(Entity entity) {
        return entity.getType().getName() + "Spawn";
    }
}

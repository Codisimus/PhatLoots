package com.codisimus.plugins.phatloots;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for Mob spawning to equip them with armor and a weapon
 *
 * @author Cody
 */
public class MobSpawnListener extends MobListener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        //Schedule this code so that mob spawning plugins have a chance to change the type of the mob
        new BukkitRunnable() {
            @Override
            public void run() {
                LivingEntity entity = event.getEntity();
                PhatLoot phatLoot = getPhatLoot(entity);
                if (phatLoot != null) {
                    //The mob's 'level' gives them a looting bonus to get better equipment
                    double level = entity.hasMetadata("level")
                                   ? entity.getMetadata("level").get(0).asDouble()
                                   : 0;
                    phatLoot.rollForLoot(entity, level);
                }
            }
        }.runTask(PhatLoots.plugin);
    }

    @Override
    String getType(Entity entity) {
        return entity.getType().getName() + "Spawn";
    }
}

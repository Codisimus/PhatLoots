package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Manages Mob drops when Mobs die
 *
 * @author Cody
 */
public class MobDeathListener extends MobListener {
    @EventHandler (ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PhatLoot phatLoot = getPhatLoot(entity);
        if (phatLoot != null) {
            event.setDroppedExp(phatLoot.rollForMobDrops(entity, entity.getKiller(), event.getDrops()));
        }
    }

    @Override
    String getType(Entity entity) {
        return entity.getType().getName();
    }
}

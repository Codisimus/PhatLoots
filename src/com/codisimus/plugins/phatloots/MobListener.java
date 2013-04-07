package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.regionown.Region;
import com.codisimus.plugins.regionown.RegionOwn;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

/**
 * Listens for Mob events that trigger loots
 *
 * @author Codisimus
 */
public abstract class MobListener implements Listener {
    static boolean mobTypes;
    static boolean namedMobs;
    boolean mobWorlds;
    boolean mobRegions;
    abstract String getType(Entity entity);

    /**
     * Returns the PhatLoot for the given Entity if one exists
     *
     * @param entity The given Entity
     * @return The PhatLoot or null if there are none for the Entity
     */
    public PhatLoot getPhatLoot(LivingEntity entity) {
        if (namedMobs && entity.getCustomName() != null) {
            return PhatLoots.getPhatLoot(entity.getCustomName());
        }

        Location location = entity.getLocation();
        String type = getType(entity);

        String specificType = null;
        if (mobTypes) {
            switch (entity.getType()) {
            case ZOMBIE:
                Zombie zombie = (Zombie) entity;
                if (zombie.isBaby()) {
                    specificType = "Baby";
                } else if (zombie.isVillager()) {
                    specificType = "Villager";
                } else {
                    specificType = "Normal";
                }
                break;
            case SKELETON:
                specificType = ((Skeleton) entity).getSkeletonType().toString();
                break;
            case VILLAGER:
                specificType = ((Villager) entity).getProfession().toString();
                break;
            default:
                specificType = null;
                break;
            }
        }

        String regionName = null;
        if (mobRegions) {
            for (Region region : RegionOwn.mobRegions.values()) {
                if (region.contains(location)) {
                    regionName = '@' + region.name;
                    break;
                }
            }
        }

        PhatLoot phatLoot;
        String worldName = mobWorlds ? '@' + location.getWorld().getName() : null;
        if (mobTypes && specificType != null) {
            if (mobRegions && regionName != null){
                phatLoot = PhatLoots.getPhatLoot(specificType + type + regionName);
                if (phatLoot != null) {
                    return phatLoot;
                }
            }
            if (mobWorlds) {
                phatLoot = PhatLoots.getPhatLoot(specificType + type + worldName);
                if (phatLoot != null) {
                    return phatLoot;
                }
            }
        }

        if (mobRegions && regionName != null){
            phatLoot = PhatLoots.getPhatLoot(type + regionName);
            if (phatLoot != null) {
                return phatLoot;
            }
        }
        if (mobWorlds) {
            phatLoot = PhatLoots.getPhatLoot(type + worldName);
            if (phatLoot != null) {
                return phatLoot;
            }
        }

        if (mobTypes && specificType != null) {
            phatLoot = PhatLoots.getPhatLoot(specificType + type);
            if (phatLoot != null) {
                return phatLoot;
            }
        }

        return PhatLoots.getPhatLoot(type);
    }
}

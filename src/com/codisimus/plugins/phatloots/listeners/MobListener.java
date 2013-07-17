package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.regionown.Region;
import com.codisimus.plugins.regionown.RegionOwn;
import org.bukkit.Location;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;

/**
 * Listens for Mob events that trigger loots
 *
 * @author Cody
 */
public abstract class MobListener implements Listener {
    public static boolean mobTypes;
    public static boolean namedMobs;
    public boolean mobWorlds;
    public boolean mobRegions;

    /**
     * Returns a cleaned up string representation of the given Entity's type
     *
     * @param entity The given Entity
     * @return The name of the type of the Entity
     */
    abstract String getType(Entity entity);

    /**
     * Returns the PhatLoot for the given Entity if one exists
     * A PhatLoot for a mob is search for in the following order
     *     With a custom name
     *     Of a specific Type
     *         In a specific Region
     *         In a specific World
     *     Of any type
     *         In a specific Region
     *         In a specific World
     *     Of a specific Type
     *         Anywhere
     *     Of any type
     *         Anywhere
     *
     * @param entity The given Entity
     * @return The PhatLoot or null if there are none for the Entity
     */
    public PhatLoot getPhatLoot(LivingEntity entity) {
        //First check for a PhatLoot matching the mob's custom name
        if (namedMobs && entity.getCustomName() != null) {
            PhatLoot phatLoot = PhatLoots.getPhatLoot(entity.getCustomName());
            if (phatLoot != null) {
                //A PhatLoot for a named mob trumps all others
                return phatLoot;
            }
        }

        //Retrieve the more specific type of the mob if there is one
        //ex. Wither Skeleton as opposed to normal Skeleton
        //    or a Priest rather than a normal Villager
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
                Profession prof = ((Villager) entity).getProfession();
                if (prof != null) {
                    specificType = prof.toString();
                }
                break;
            default:
                specificType = null;
                break;
            }
        }

        //Check if the mob is within a region
        Location location = entity.getLocation();
        String regionName = null;
        if (mobRegions) {
            for (Region region : RegionOwn.mobRegions.values()) {
                if (region.contains(location)) {
                    regionName = '@' + region.name;
                    break;
                }
            }
        }

        //Get the mob type and the name of the world for constructing the PhatLoot name
        String type = getType(entity);
        String worldName = mobWorlds ? '@' + location.getWorld().getName() : null;

        //The order of priority for finding the correct PhatLoot may be found in the documentation of this method
        PhatLoot phatLoot;
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

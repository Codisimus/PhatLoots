package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.regionown.Region;
import com.codisimus.plugins.regionown.RegionOwn;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.Listener;

/**
 * Listens for Mob events that trigger loots
 *
 * @author Cody
 */
public abstract class MobListener implements Listener {
    public static boolean mobTypes;
    public static boolean namedMobs;
    public static boolean regionOwn = Bukkit.getPluginManager().isPluginEnabled("RegionOwn");
    public boolean mobWorlds;
    public boolean mobRegions;

    /**
     * Returns a cleaned up string representation of the given Entity's type
     *
     * @param entity The given Entity
     * @return The name of the type of the Entity
     */
    abstract String getLootType();

    /**
     * Returns the PhatLoot for the given Entity if one exists.
     * A PhatLoot for a mob is searched for in the following order
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
        if (namedMobs) {
            String name = entity instanceof HumanEntity
                          ? ((HumanEntity) entity).getName() //NPC or Player
                          : entity.getCustomName(); //Mob
            if (name != null) {
                PhatLoot phatLoot = PhatLoots.getPhatLoot(name);
                if (phatLoot != null) {
                    //A PhatLoot for a named mob trumps all others
                    return phatLoot;
                }
            }
        }

        //Retrieve the more specific type of the mob if there is one
        //ex. Wither Skeleton as opposed to normal Skeleton
        //    or a Priest rather than a normal Villager
        String type = (entity instanceof Player) ? "Player" : entity.getType().getName();
        String specificType = null;
        if (mobTypes) {
            switch (entity.getType()) {
            case PIG_ZOMBIE:
            case ZOMBIE: //'BabyVillager' | 'Baby' | 'Villager' | 'Normal'
                Zombie zombie = (Zombie) entity;
                if (zombie.isBaby()) {
                    specificType = zombie.isVillager() ? "BabyVillager" : "Baby";
                } else if (zombie.isVillager()) {
                    specificType = "Villager";
                } else {
                    specificType = "Normal";
                }
                break;
            case SKELETON: //'Wither' | 'Normal'
                specificType = ((Skeleton) entity).getSkeletonType().toString();
                break;
            case VILLAGER: //Profession
                Profession prof = ((Villager) entity).getProfession();
                if (prof != null) {
                    specificType = prof.toString();
                }
                break;
            case CREEPER: //'Powered' | 'Normal'
                Creeper creeper = (Creeper) entity;
                specificType = creeper.isPowered() ? "Powered" : "Normal";
                break;
            case HORSE: //Color + Style (type is also determined by variant
                Horse horse = (Horse) entity;
                type = horse.getVariant().name();
                specificType = horse.getColor().name();
                if (horse.getStyle() != Style.NONE) {
                    specificType += horse.getStyle().name();
                }
                break;
            case SHEEP: //Color
                Sheep sheep = (Sheep) entity;
                specificType = sheep.getColor().name();
                break;
            default:
                break;
            }
        }
        //Check if the entity is a baby
        if (entity instanceof Ageable && !((Ageable) entity).isAdult()) {
            //Ammend 'Baby' to the beggining of the specific type
            specificType = specificType == null ? "Baby" : "Baby" + specificType;
        }

        //Check if the mob is within a region
        Location location = entity.getLocation();
        String regionName = null;
        if (mobRegions && regionOwn) {
            for (Region region : RegionOwn.mobRegions.values()) {
                if (region.contains(location)) {
                    regionName = '@' + region.name;
                    break;
                }
            }
        }

        //Get the loot type and the name of the world for constructing the PhatLoot name
        type += getLootType();
        String worldName = mobWorlds ? '@' + location.getWorld().getName() : null;

        //The order of priority for finding the correct PhatLoot may be found in the documentation of this method
        PhatLoot phatLoot;
        if (mobTypes && specificType != null) {
            if (mobRegions && regionName != null) {
                if (regionOwn) {
                    phatLoot = PhatLoots.getPhatLoot(specificType + type + regionName);
                    if (phatLoot != null) {
                        return phatLoot;
                    }
                } else { //WorldGuard support
                    for (ProtectedRegion region : WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location)) {
                        phatLoot = PhatLoots.getPhatLoot(specificType + type + region.getId());
                        if (phatLoot != null) {
                            return phatLoot;
                        }
                    }
                }
            }
            if (mobWorlds) {
                phatLoot = PhatLoots.getPhatLoot(specificType + type + worldName);
                if (phatLoot != null) {
                    return phatLoot;
                }
            }
        }

        if (mobRegions && regionName != null) {
            if (regionOwn) {
                phatLoot = PhatLoots.getPhatLoot(type + regionName);
                if (phatLoot != null) {
                    return phatLoot;
                }
            } else { //WorldGuard support
                for (ProtectedRegion region : WGBukkit.getRegionManager(location.getWorld()).getApplicableRegions(location)) {
                    phatLoot = PhatLoots.getPhatLoot(type + region.getId());
                    if (phatLoot != null) {
                        return phatLoot;
                    }
                }
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

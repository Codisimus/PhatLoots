package com.codisimus.plugins.phatloots.regions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;

/**
 *
 * @author Codisimus
 */
public class WorldGuardRegionHook implements RegionHook {
    @Override
    public String getPluginName() {
        return "WorldGuard";
    }

    @Override
    public List<String> getRegionNames(Location loc) {
        List<String> regionNames = new ArrayList<>(1);
        ApplicableRegionSet applicableRegionSet = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld())).getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        Set<ProtectedRegion> regionSet = applicableRegionSet.getRegions();

        //Eliminate all parent Regions
        Iterator<ProtectedRegion> itr = applicableRegionSet.iterator();
        while (itr.hasNext()) {
            ProtectedRegion region = itr.next().getParent();
            while (region != null) {
                regionSet.remove(region);
                region = region.getParent();
            }
        }

        for (ProtectedRegion region : regionSet) {
            regionNames.add(region.getId());
        }
        return regionNames;
    }
}

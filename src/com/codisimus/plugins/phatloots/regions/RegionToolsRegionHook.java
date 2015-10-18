package com.codisimus.plugins.phatloots.regions;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.regiontools.Region;
import com.codisimus.plugins.regiontools.RegionTools;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

/**
 *
 * @author Codisimus
 */
public class RegionToolsRegionHook implements RegionHook {
    Region regionGroup;

    public RegionToolsRegionHook() {
        String regionName = PhatLoots.plugin.getConfig().getString("RegionGroup");
        if (regionName != null && !regionName.isEmpty()) {
            regionGroup = RegionTools.findRegion(regionName, false);
            if (regionGroup == null) {
                regionGroup = RegionTools.createRegionGroup(regionName);
            }
        }
    }

    @Override
    public String getPluginName() {
        return "RegionTools";
    }

    @Override
    public List<String> getRegionNames(Location loc) {
        List<String> regionNames = new ArrayList<>(1);
        Region region = regionGroup == null
                      ? RegionTools.findRegion(loc, true)
                      : regionGroup.findRegion(loc);
        if (region != null) {
            regionNames.add(region.getName());
        }
        return regionNames;
    }
}

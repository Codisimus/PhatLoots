package com.codisimus.plugins.phatloots.hook.placeholder;

import com.codisimus.plugins.phatloots.PhatLoots;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manager for placeholder replacements for
 * various placeholder plugins.
 *
 * @author Redned
 */
public class PlaceholderManager {

    private Map<String, PlaceholderHandler> placeholderHandlers = new HashMap<>();

    public PlaceholderManager(PhatLoots plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (pluginManager.getPlugin("PlaceholderAPI") != null)
            placeholderHandlers.put("PlaceholderAPI", new PAPIPlaceholderHandler());
    }

    /**
     * Returns the replacement string with the available placeholders
     *
     * @param player the player to get the replacement for
     * @param string the string to do the replacements on
     * @return the replacement string with the available placeholders
     */
    public String getReplacementString(Player player, String string) {
        for (PlaceholderHandler handler : placeholderHandlers.values()) {
            string = handler.getReplacementString(player, string);
        }
        return string;
    }

    /**
     * Returns if any placeholder plugins are present
     *
     * @return if any placeholder plugins are present
     */
    public boolean isPlaceholderPluginPresent() {
        return !placeholderHandlers.isEmpty();
    }

    /**
     * Returns if the given placeholder plugin is present
     *
     * @param pluginName the name of the placeholder plugin
     * @return if the given placeholder plugin is present
     */
    public boolean isPlaceholderPluginPresent(String pluginName) {
        return placeholderHandlers.containsKey(pluginName);
    }

    /**
     * Returns the currently hooked placeholder plugins
     *
     * @return the currently hooked placeholder plugins
     */
    public Set<String> getPlaceholderPlugins() {
        return placeholderHandlers.keySet();
    }
}

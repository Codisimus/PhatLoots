package com.codisimus.plugins.phatloots.hook;

import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.hook.placeholder.PlaceholderManager;

/**
 * Manager for managing certain plugin hooks
 *
 * @author Redned
 */
public class PluginHookManager {

    private PlaceholderManager placeholderManager;

    public PluginHookManager(PhatLoots plugin) {
        placeholderManager = new PlaceholderManager(plugin);
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}

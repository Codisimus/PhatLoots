package com.codisimus.plugins.phatloots.hook.placeholder;

import com.codisimus.plugins.phatloots.PhatLoots;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;

public class PAPIPlaceholderHandler extends PlaceholderExpansion implements PlaceholderHandler {

    @Override
    public String getIdentifier() {
        return "phatloots";
    }

    @Override
    public String getAuthor() {
        return "Redned";
    }

    @Override
    public String getVersion() {
        return PhatLoots.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return super.onPlaceholderRequest(player, params); // TODO
    }

    @Override
    public String getReplacementString(Player player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    @Override
    public String onVariableReplacement(Player player, String params) {
        return this.onPlaceholderRequest(player, params);
    }
}

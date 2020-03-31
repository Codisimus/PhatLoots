package com.codisimus.plugins.phatloots.hook.placeholder;

import org.bukkit.entity.Player;

/**
 * Placeholder handler for retrieving placeholder
 * information.
 *
 * @author Redned
 */
public interface PlaceholderHandler {

    /**
     * Returns the name of this placeholder handler
     *
     * @return the name of this placeholder handler
     */
    String getName();

    /**
     * Returns the replacement string with the available placeholders
     *
     * @param player the player to get the replacement for
     * @param string the string to do the replacements on
     * @return the replacement string with the available placeholders
     */
    String getReplacementString(Player player, String string);

    /**
     * Replaces the given string with placeholders replaced
     *
     * @param player the player to do the replacement on
     * @param string the string to replace
     * @return the given string with placeholders replaced
     */
     String onVariableReplacement(Player player, String string);
}

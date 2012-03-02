package com.codisimus.plugins.phatloots;

/**
 * Holds messages that are displayed to users of this plugin
 *
 * @author Codisimus
 */
public class PhatLootsMessages {
    static String autoLoot;
    static String timeRemaining;
    static String inUse;
    static String overflow;
    
    /**
     * Formats all PhatLoots messages
     * 
     */
    static void formatAll() {
        autoLoot = format(autoLoot);
        timeRemaining = format(timeRemaining);
        inUse = format(inUse);
        overflow = format(overflow);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}
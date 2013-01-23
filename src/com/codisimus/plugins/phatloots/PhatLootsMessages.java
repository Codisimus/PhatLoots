package com.codisimus.plugins.phatloots;

/**
 * Holds messages that are displayed to users of this plugin
 *
 * @author Codisimus
 */
public class PhatLootsMessages {
    static String permission;
    static String moneyLooted;
    static String experienceLooted;
    static String autoLoot;
    static String timeRemaining;
    static String overflow;
    static String mobDropped;

    /**
     * Formats all messages
     */
    static void formatAll() {
        permission = format(permission);
        moneyLooted = format(moneyLooted);
        experienceLooted = format(experienceLooted);
        autoLoot = format(autoLoot);
        timeRemaining = format(timeRemaining);
        overflow = format(overflow);
        mobDropped = format(mobDropped);
    }

    /**
     * Adds various Unicode characters and colors to a string
     *
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replace("&", "§")
                .replace("<ae>", "æ").replace("<AE>", "Æ")
                .replace("<o/>", "ø").replace("<O/>", "Ø")
                .replace("<a>", "å").replace("<A>", "Å");
    }
}

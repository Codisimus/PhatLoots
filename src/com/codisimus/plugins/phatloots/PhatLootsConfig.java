package com.codisimus.plugins.phatloots;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class PhatLootsConfig {
    private static Properties p;

    public static void load() {
        //Load Config settings
        FileInputStream fis = null;
        try {
            //Copy the file from the jar if it is missing
            File file = new File(PhatLoots.dataFolder + "/config.properties");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("config.properties", true);
            }

            //Load config file
            p = new Properties();
            fis = new FileInputStream(file);
            p.load(fis);

            PhatLoots.useRestricted = loadBool("PermissionNeededOnlyForRestrictedPhatLoots", false);
            PhatLoots.restricted.addAll(Arrays.asList(loadString("RestrictedPhatLoots", "Rare, Epic, Donator").split(", ")));

            PhatLoot.onlyDropOnPlayerKill = loadBool("OnlyDropLootWhenKilledByPlayer", false);
            PhatLoot.replaceMobLoot = loadBool("ReplaceMobLoot", true);
            PhatLoots.autoLoot = loadBool("AutoLoot", false);
            PhatLoots.displayTimeRemaining = loadBool("DisplayTimeRemaining", true);

            PhatLootsCommand.setUnlockable = loadBool("SetChestsAsUnlockable", true);

            String[] defaultResetTime = loadString("DefaultResetTime", "1'0'0'0").split("'");
            PhatLoots.defaultDays = Integer.parseInt(defaultResetTime[0]);
            PhatLoots.defaultHours = Integer.parseInt(defaultResetTime[1]);
            PhatLoots.defaultMinutes = Integer.parseInt(defaultResetTime[2]);
            PhatLoots.defaultSeconds = Integer.parseInt(defaultResetTime[3]);

            PhatLoots.defaultGlobal = loadBool("GlobalResetByDefault", false);
            PhatLoots.defaultRound = loadBool("RoundDownTimeByDefault", false);
            PhatLoots.defaultNumberOfLoots = loadInt("DefaultItemsPerColl", 1);

            PhatLootsListener.chestName = loadString("ChestName", "<name>");

            /* Messages */
            String string = "PLUGIN CONFIG MUST BE REGENERATED!";
            PhatLootsMessages.permission = loadString("PermissionMessage", string);
            PhatLootsMessages.experienceLooted = loadString("ExperienceLootedMessage", string);
            PhatLootsMessages.moneyLooted = loadString("MoneyLootedMessage", string);
            PhatLootsMessages.autoLoot = loadString("AutoLootMessage", string);
            PhatLootsMessages.overflow = loadString("OverflowMessage", string);
            PhatLootsMessages.timeRemaining = loadString("TimeRemainingMessage", string);
            PhatLootsMessages.mobDroppedMoney = loadString("MobDroppedMoneyMessage", string);
            PhatLootsMessages.mobDroppedItem = loadString("MobDroppedItemMessage", string);
            PhatLootsMessages.formatAll();
        } catch (Exception missingProp) {
            PhatLoots.logger.severe("Failed to load PhatLoots Config");
            missingProp.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private static String loadString(String key, String defaultString) {
        if (p.containsKey(key)) {
            return p.getProperty(key);
        } else {
            PhatLoots.logger.severe("Missing value for " + key);
            PhatLoots.logger.severe("Please regenerate the config.properties file (delete the old file to allow a new one to be created)");
            PhatLoots.logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultString;
        }
    }

    /**
     * Loads the given key and prints an error if the key is not an Integer
     *
     * @param key The key to be loaded
     * @return The Integer value of the loaded key
     */
    private static int loadInt(String key, int defaultValue) {
        String string = loadString(key, null);
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            PhatLoots.logger.severe("The setting for " + key + " must be a valid integer");
            PhatLoots.logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultValue;
        }
    }

    /**
     * Loads the given key and prints an error if the key is not a boolean
     *
     * @param key The key to be loaded
     * @return The boolean value of the loaded key
     */
    private static boolean loadBool(String key, boolean defaultValue) {
        String string = loadString(key, null);
        try {
            return Boolean.parseBoolean(string);
        } catch (Exception e) {
            PhatLoots.logger.severe("The setting for " + key + " must be 'true' or 'false' ");
            PhatLoots.logger.severe("DO NOT POST A TICKET FOR THIS MESSAGE, IT WILL JUST BE IGNORED");
            return defaultValue;
        }
    }
}

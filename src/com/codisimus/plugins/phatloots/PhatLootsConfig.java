package com.codisimus.plugins.phatloots;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PhatLootsConfig {
    static int defaultDays;
    static int defaultHours;
    static int defaultMinutes;
    static int defaultSeconds;
    static int defaultNumberOfLoots;
    static boolean defaultGlobal;
    static boolean defaultRound;
    static boolean defaultAutoLoot;
    static boolean autoLock;
    static boolean restrictAll;
    static HashSet<String> restricted = new HashSet();
    static String permission;
    static String moneyLooted;
    static String experienceLooted;
    static String autoLoot;
    static String timeRemaining;
    static String overflow;
    static String mobTimeRemaining;
    static String mobDroppedMoney;
    static String mobDroppedItem;
    static String mobDroppedExperience;

    public static void load() {
        PhatLoots.plugin.saveDefaultConfig();
        FileConfiguration config = PhatLoots.plugin.getConfig();


        /* MOB LOOTS */

        PhatLoot.replaceMobLoot = config.getBoolean("ReplaceMobLoot");
        PhatLoot.onlyDropOnPlayerKill = config.getBoolean("OnlyDropLootWhenKilledByPlayer");
        PhatLoot.chanceOfDrop = (float) (config.getDouble("MobLootDropPercentage") / 100.0D);


        /* MESSAGES */

        ConfigurationSection section = config.getConfigurationSection("Messages");
        permission = section.getString("Permission");
        experienceLooted = section.getString("ExperienceLooted");
        moneyLooted = section.getString("MoneyLooted");
        autoLoot = section.getString("AutoLoot");
        overflow = section.getString("Overflow");
        timeRemaining = section.getString("TimeRemaining");
        mobTimeRemaining = section.getString("MobTimeRemaining");
        mobDroppedMoney = section.getString("MobDroppedMoney");
        mobDroppedItem = section.getString("MobDroppedItem");
        mobDroppedExperience = section.getString("MobDroppedExperience");

        PhatLootsListener.chestName = config.getString("ChestName");

        Loot.damageString = config.getString("<dam>");
        Loot.holyString = config.getString("<holy>");
        Loot.fireString = config.getString("<fire>");
        Loot.bugString = config.getString("<bug>");


        /* DEFAULTS */

        section = config.getConfigurationSection("Defaults");
        defaultGlobal = section.getBoolean("GlobalReset");
        defaultRound = section.getBoolean("RoundDownTime");
        defaultNumberOfLoots = section.getInt("ItemsPerColl");
        defaultAutoLoot = section.getBoolean("AutoLoot");

        section = section.getConfigurationSection("ResetTime");
        defaultDays = section.getInt("Days");
        defaultHours = section.getInt("Hours");
        defaultMinutes = section.getInt("Minutes");
        defaultSeconds = section.getInt("Seconds");


        /* OTHER */

        restrictAll = config.getBoolean("RestrictAll");
        restricted.addAll(config.getStringList("RestrictedPhatLoots"));
        Loot.tierNotify = config.getInt("MinimumTierNotification");
        PhatLootsCommand.setUnlockable = config.getBoolean("SetChestsAsUnlockable");


        /* LORES.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "lores.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("lores.yml", true);
            }

            Loot.loreConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load lores.yml");
            ex.printStackTrace();
        }


        /* ENCHANTMENTS.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "enchantments.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("enchantments.yml", true);
            }

            Loot.enchantmentConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load enchantments.yml");
            ex.printStackTrace();
        }
    }
}

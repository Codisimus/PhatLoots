package com.codisimus.plugins.phatloots;

import java.io.File;
import java.util.HashSet;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    static String moneyCharged;
    static String insufficientFunds;
    static String experienceLooted;
    static String autoLoot;
    static String timeRemaining;
    static String overflow;
    static String mobTimeRemaining;
    static String mobDroppedMoney;
    static String mobDroppedItem;
    static String mobDroppedExperience;
    static String lootMessage;
    static String lootBroadcast;

    public static void load() {
        PhatLoots.plugin.saveDefaultConfig();
        FileConfiguration config = PhatLoots.plugin.getConfig();


        /* LINKABLES */

        for (String string : config.getStringList("Blocks")) {
            Material mat = Material.matchMaterial(string);
            if (mat != null) {
                PhatLootsListener.types.put(mat, null);
            }
        }
        ConfigurationSection section = config.getConfigurationSection("AutoLink");
        for (String string : section.getKeys(false)) {
            Material mat = Material.matchMaterial(string);
            if (mat != null) {
                PhatLootsListener.types.put(mat, section.getString(string));
            }
        }


        /* MOB LOOTS */

        PhatLoot.replaceMobLoot = config.getBoolean("ReplaceMobLoot");
        PhatLoot.onlyDropOnPlayerKill = config.getBoolean("OnlyDropLootWhenKilledByPlayer");
        PhatLoot.chanceOfDrop = (float) (config.getDouble("MobLootDropPercentage") / 100.0D);
        PhatLoot.lootingBonusPerLvl = config.getDouble("LootingBonusPerLevel");
        MobListener.mobTypes = config.getBoolean("MobTypes");
        MobListener.namedMobs = config.getBoolean("NamedMobs");


        /* MESSAGES */

        section = config.getConfigurationSection("Messages");
        permission = getString(section, "Permission");
        experienceLooted = getString(section, "ExperienceLooted");
        moneyLooted = getString(section, "MoneyLooted");
        moneyCharged = getString(section, "MoneyCharged");
        insufficientFunds = getString(section, "InsufficientFunds");
        autoLoot = getString(section, "AutoLoot");
        overflow = getString(section, "Overflow");
        timeRemaining = getString(section, "TimeRemaining");
        mobTimeRemaining = getString(section, "MobTimeRemaining");
        mobDroppedMoney = getString(section, "MobDroppedMoney");
        mobDroppedItem = getString(section, "MobDroppedItem");
        mobDroppedExperience = getString(section, "MobDroppedExperience");
        lootMessage = getString(section, "LootMessage");
        lootBroadcast = getString(section, "LootBroadcast");

        PhatLootsListener.chestName = getString(config, "ChestName");

        OldLoot.damageString = getString(config, "<dam>");
        OldLoot.holyString = getString(config, "<holy>");
        OldLoot.fireString = getString(config, "<fire>");
        OldLoot.bugString = getString(config, "<bug>");
        OldLoot.thornsString = getString(config, "<thorns>");
        OldLoot.defenseString = getString(config, "<def>");
        OldLoot.fireDefenseString = getString(config, "<firedef>");
        OldLoot.rangeDefenseString = getString(config, "<rangedef>");
        OldLoot.blastDefenseString = getString(config, "<blastdef>");
        OldLoot.fallDefenseString = getString(config, "<falldef>");


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
        for (String string : restricted) {
            string = ChatColor.translateAlternateColorCodes('&', string);
        }
        OldLoot.tierNotify = config.getInt("MinimumTierNotification");
        PhatLootsCommand.setUnlockable = config.getBoolean("SetChestsAsUnlockable");
        PhatLoot.autoClose = config.getBoolean("AutoCloseOnInsufficientFunds");


        /* LORES.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "lores.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("lores.yml", true);
            }

            OldLoot.loreConfig = YamlConfiguration.loadConfiguration(file);
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

            OldLoot.enchantmentConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load enchantments.yml");
            ex.printStackTrace();
        }
    }

    private static String getString(ConfigurationSection config, String key) {
        String string = ChatColor.translateAlternateColorCodes('&', config.getString(key));
        return string.isEmpty() ? null : string;
    }
}

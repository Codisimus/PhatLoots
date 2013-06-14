package com.codisimus.plugins.phatloots;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PhatLootsConfig {
    static int defaultDays; //Default cooldown time
    static int defaultHours;
    static int defaultMinutes;
    static int defaultSeconds;
    static int defaultLowerNumberOfLoots; //Default collection range
    static int defaultUpperNumberOfLoots;
    static boolean defaultGlobal;
    static boolean defaultRound;
    static boolean defaultAutoLoot;
    static boolean restrictAll; //True if all PhatLoots should require permission
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
        FileConfiguration config = PhatLoots.plugin.getConfig();

        //Check for an outdated config.yml file
        if (config.get("UseDamageTags", null) == null) {
            PhatLoots.logger.warning("Your config.yml file is outdated! To get the most out of this plugin please (re)move the old file so a new one can be generated.");
        }


        /* LINKABLES */

        for (String string : config.getStringList("Blocks")) {
            Material mat = Material.matchMaterial(string);
            if (mat != null) {
                PhatLootsListener.types.put(mat, null);
            }
        }
        ConfigurationSection section = config.getConfigurationSection("AutoLink");
        for (String world : section.getKeys(false)) {
            ConfigurationSection worldSection = section.getConfigurationSection(world);
            for (String string : worldSection.getKeys(false)) {
                Material mat = Material.matchMaterial(string);
                if (mat != null) {
                    if (PhatLootsListener.types.get(mat) == null) {
                        PhatLootsListener.types.put(mat, new HashMap());
                    }
                    PhatLootsListener.types.get(mat).put(world, worldSection.getString(string));
                }
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


        /* TAGS */

        Item.damageTags = section.getBoolean("UseDamageTags");
        if (Item.damageTags) {
            Item.damageString = getString(config, "<dam>");
            Item.holyString = getString(config, "<holy>");
            Item.fireString = getString(config, "<fire>");
            Item.bugString = getString(config, "<bug>");
            Item.thornsString = getString(config, "<thorns>");
            Item.defenseString = getString(config, "<def>");
            Item.fireDefenseString = getString(config, "<firedef>");
            Item.rangeDefenseString = getString(config, "<rangedef>");
            Item.blastDefenseString = getString(config, "<blastdef>");
            Item.fallDefenseString = getString(config, "<falldef>");
        }


        /* DEFAULTS */

        section = config.getConfigurationSection("Defaults");
        defaultGlobal = section.getBoolean("GlobalReset");
        defaultRound = section.getBoolean("RoundDownTime");
        String itemsPerColl = section.getString("ItemsPerColl");
        if (itemsPerColl != null) {
            //This amount may be set as a range
            int index = itemsPerColl.indexOf('-');
            if (index == -1) { //Single number
                int numberOfLoots = Integer.parseInt(itemsPerColl);
                defaultLowerNumberOfLoots = numberOfLoots;
                defaultUpperNumberOfLoots = numberOfLoots;
            } else { //Range
                defaultLowerNumberOfLoots = Integer.parseInt(itemsPerColl.substring(0, index));
                defaultUpperNumberOfLoots = Integer.parseInt(itemsPerColl.substring(index + 1));
            }
        }
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
        Item.tierNotify = config.getInt("MinimumTierNotification");
        PhatLootsCommand.setUnlockable = config.getBoolean("SetChestsAsUnlockable");
        PhatLoot.autoClose = config.getBoolean("AutoCloseOnInsufficientFunds");
        PhatLoot.decimals = config.getBoolean("DivideMoneyAmountBy100");
        PhatLootChest.soundOnAutoLoot = config.getBoolean("PlaySoundOnAutoLoot");
        ForgettableInventory.delay = config.getInt("ForgetInventoryTime") * 20L;
        PhatLoot.unlink = config.getBoolean("UnlinkGlobalChestsThatNeverReset");


        /* LORES.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "lores.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("lores.yml", true);
            }

            Item.loreConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load lores.yml");
            ex.printStackTrace();
        }


        /* TIERS.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "tiers.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("tiers.yml", true);
            }

            Item.tiersConfig = YamlConfiguration.loadConfiguration(file).getConfigurationSection("TIERS");
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load tiers.yml");
            ex.printStackTrace();
        }


        /* ENCHANTMENTS.YML */

        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "enchantments.yml");
            if (!file.exists()) {
                PhatLoots.plugin.saveResource("enchantments.yml", true);
            }

            Item.enchantmentConfig = YamlConfiguration.loadConfiguration(file);
        } catch (Exception ex) {
            PhatLoots.logger.severe("Failed to load enchantments.yml");
            ex.printStackTrace();
        }
    }

    /**
     * Returns the converted string that is loaded from the given configuration
     * & will be converted to § where color codes are used
     *
     * @param config The given ConfigurationSection
     * @param key The key that leads to the requested string
     * @return The String or null if the string was not found or empty
     */
    private static String getString(ConfigurationSection config, String key) {
        String string = ChatColor.translateAlternateColorCodes('&', config.getString(key));
        return string.isEmpty() ? null : string;
    }
}

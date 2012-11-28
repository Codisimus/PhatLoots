package com.codisimus.plugins.phatloots;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class PhatLoots extends JavaPlugin {
    static Server server;
    static Logger logger;
    static PluginManager pm;
    static Random random = new Random();
    static int defaultDays;
    static int defaultHours;
    static int defaultMinutes;
    static int defaultSeconds;
    static int defaultNumberOfLoots;
    static boolean defaultGlobal;
    static boolean defaultRound;
    static boolean autoLock;
    static boolean autoLoot;
    static boolean displayTimeRemaining;
    static JavaPlugin plugin;
    static String dataFolder;
    private static Permission permission;
    private Properties p;
    private static HashMap<String, PhatLoot> phatLoots = new HashMap<String, PhatLoot>();

    /**
     * Calls methods to load this Plugin when it is enabled
     */
    @Override
    public void onEnable () {
        server = getServer();
        logger = getLogger();
        pm = server.getPluginManager();
        plugin = this;

        /* Disable this plugin if Vault is not present */
        if (!pm.isPluginEnabled("Vault")) {
            logger.severe("Please install Vault in order to use this plugin!");
            pm.disablePlugin(this);
            return;
        }

        /* Create data folders */
        File dir = this.getDataFolder();
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dataFolder = dir.getPath();

        dir = new File(dataFolder + "/PhatLoots");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder + "/Item Descriptions");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder + "/Books");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        /* Load Data and Settings */
        loadData();
        loadSettings();

        /* Link Permissions/Economy */
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }

        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            Econ.economy = economyProvider.getProvider();
        }

        /* Register Events */
        pm.registerEvents(new PhatLootsListener(), this);

        /* Register the command found in the plugin.yml */
        PhatLootsCommand.command = (String) getDescription().getCommands().keySet().toArray()[0];
        getCommand(PhatLootsCommand.command).setExecutor(new PhatLootsCommand());

        Properties version = new Properties();
        try {
            version.load(this.getResource("version.properties"));
        } catch (Exception ex) {
            logger.warning("version.properties file not found within jar");
        }
        logger.info("PhatLoots " + getDescription().getVersion() + " (Build "
                    + version.getProperty("Build") + ") is enabled!");
    }

    /**
     * Loads settings from the config.properties file
     */
    public void loadSettings() {
        FileInputStream fis = null;
        try {
            //Copy the file from the jar if it is missing
            File file = new File(dataFolder + "/config.properties");
            if (!file.exists()) {
                this.saveResource("config.properties", true);
            }

            //Load config file
            p = new Properties();
            fis = new FileInputStream(file);
            p.load(fis);

            PhatLoot.onlyDropOnPlayerKill = Boolean.parseBoolean(loadValue("OnlyDropLootWhenKilledByPlayer"));
            PhatLoot.replaceMobLoot = Boolean.parseBoolean(loadValue("ReplaceMobLoot"));
            autoLoot = Boolean.parseBoolean(loadValue("AutoLoot"));
            displayTimeRemaining = Boolean.parseBoolean(loadValue("DisplayTimeRemaining"));

            PhatLootsCommand.setUnlockable = Boolean.parseBoolean(loadValue("SetChestsAsUnlockable"));

            /* Default reset time */
            String[] resetTime = loadValue("DefaultResetTime").split("'");
            defaultDays = Integer.parseInt(resetTime[0]);
            defaultHours = Integer.parseInt(resetTime[1]);
            defaultMinutes = Integer.parseInt(resetTime[2]);
            defaultSeconds = Integer.parseInt(resetTime[3]);

            defaultGlobal = Boolean.parseBoolean(loadValue("GlobalResetByDefault"));
            defaultRound = Boolean.parseBoolean(loadValue("RoundDownTimeByDefault"));
            defaultNumberOfLoots = Integer.parseInt(loadValue("DefaultItemsPerColl"));

            PhatLootsListener.chestName = loadValue("ChestName");

            /* Messages */
            PhatLootsMessages.permission = loadValue(("PermissionMessage"));
            PhatLootsMessages.experienceLooted = loadValue(("ExperienceLootedMessage"));
            PhatLootsMessages.moneyLooted = loadValue(("MoneyLootedMessage"));
            PhatLootsMessages.autoLoot = loadValue(("AutoLootMessage"));
            PhatLootsMessages.overflow = loadValue("OverflowMessage");
            PhatLootsMessages.timeRemaining = loadValue("TimeRemainingMessage");
            PhatLootsMessages.formatAll();
        } catch (Exception missingProp) {
            logger.severe("Failed to load PhatLoots "
                            + this.getDescription().getVersion());
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
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            logger.severe("Missing value for " + key + " in config file");
            logger.severe("Please regenerate config file");
        }

        return p.getProperty(key);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     *
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        return permission.has(player, "phatloots." + type);
    }

    /**
     * Returns true if the given player is allowed to loot the specified PhatLoot
     *
     * @param player The Player who is being checked for permission
     * @param phatLoot The PhatLoot in question
     * @return true if the given player is allowed to loot the PhatLoot
     */
    public static boolean canLoot(Player player, PhatLoot phatLoot) {
        if (hasPermission(player, "loot.*")) {
            return true;
        } else {
            return hasPermission(player, "loot." + phatLoot.name);
        }
    }

    /**
     * Loads properties for each PhatLoot from save files
     */
    public static void loadData() {
        FileInputStream fis = null;
        for (File file: new File(dataFolder + "/PhatLoots/").listFiles()) {
            String name = file.getName();
            if (name.endsWith(".properties")) {
                try {
                    //Load the Properties file for reading
                    Properties p = new Properties();
                    fis = new FileInputStream(file);
                    p.load(fis);
                    fis.close();

                    //Construct a new PhatLoot using the file name
                    PhatLoot phatLoot = new PhatLoot(name.substring(0, name.length() - 11));

                    //Set the reset time
                    String[] resetTime = p.getProperty("ResetTime").split("'");
                    phatLoot.days = Integer.parseInt(resetTime[0]);
                    phatLoot.hours = Integer.parseInt(resetTime[1]);
                    phatLoot.minutes = Integer.parseInt(resetTime[2]);
                    phatLoot.seconds = Integer.parseInt(resetTime[3]);

                    //Set the reset type
                    phatLoot.global = Boolean.parseBoolean(p.getProperty("GlobalReset"));
                    phatLoot.round = Boolean.parseBoolean(p.getProperty("RoundDownTime"));

                    //Set the money range
                    String[] moneyRange = p.getProperty("MoneyRange").split("-");
                    phatLoot.moneyLower = Integer.parseInt(moneyRange[0]);
                    phatLoot.moneyUpper = Integer.parseInt(moneyRange[moneyRange.length == 2 ? 1 : 0]);

                    //Set the experience range
                    if (p.containsKey("ExpRange")) {
                        String[] expRange = p.getProperty("ExpRange").split("-");
                        phatLoot.expLower = Integer.parseInt(expRange[0]);
                        phatLoot.expUpper = Integer.parseInt(expRange[moneyRange.length == 2 ? 1 : 0]);
                    }

                    //Set the commands
                    if (p.containsKey("Commands")) {
                        String value = p.getProperty("Commands");
                        if (!value.isEmpty()) {
                            for (String string: value.split(", ")) {
                                if (string.startsWith("/")) {
                                    string = string.substring(1);
                                }

                                phatLoot.commands.add(string);
                            }
                        }
                    }

                    //Load the data of all the Individual and Collective Loots
                    phatLoot.setLoots(0, p.getProperty("IndividualLoots"));
                    phatLoot.setLoots(1, p.getProperty("Coll1"));
                    phatLoot.setLoots(2, p.getProperty("Coll2"));
                    phatLoot.setLoots(3, p.getProperty("Coll3"));
                    phatLoot.setLoots(4, p.getProperty("Coll4"));
                    phatLoot.setLoots(5, p.getProperty("Coll5"));

                    phatLoot.numberCollectiveLoots = Integer.parseInt(p.getProperty("ItemsPerColl"));

                    //Load the data of all the PhatLootsChests
                    String chestData = p.getProperty("ChestsData");
                    if (chestData.contains("@")) {
                        phatLoot.setOldChests(chestData);
                    } else {
                        phatLoot.setChests(chestData);
                    }

                    //Try to Load old data (incase the old Worlds are now present)
                    if (p.containsKey("OldChestsData")) {
                        chestData = p.getProperty("OldChestsData");
                        if (chestData.contains("@")) {
                            phatLoot.setOldChests(chestData);
                        } else {
                            phatLoot.setChests(chestData);
                        }
                    }

                    phatLoots.put(phatLoot.name, phatLoot);

                    file = new File(dataFolder + "/PhatLoots/"
                                    + phatLoot.name + ".loottimes");
                    if (file.exists()) {
                        fis = new FileInputStream(file);
                        phatLoot.lootTimes.load(fis);
                    } else {
                        phatLoot.save();
                    }
                } catch (Exception loadFailed) {
                    logger.severe("Failed to load " + name);
                    loadFailed.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
     * Invokes save() method for each PhatLoot
     * Also invokes the saveSigns() method
     */
    public static void saveAll() {
        for (PhatLoot phatLoot: phatLoots.values()) {
            savePhatLoot(phatLoot);
        }
    }

    /**
     * Writes the given PhatLoot to its save file
     * If the file already exists, it is overwritten
     *
     * @param phatLoot The given PhatLoot
     */
    static void savePhatLoot(PhatLoot phatLoot) {
        FileOutputStream fos = null;
        try {
            Properties p = new Properties();

            p.setProperty("ResetTime", phatLoot.days+"'" + phatLoot.hours + "'"
                            + phatLoot.minutes + "'" + phatLoot.seconds);
            p.setProperty("GlobalReset", String.valueOf(phatLoot.global));
            p.setProperty("RoundDownTime", String.valueOf(phatLoot.round));
            p.setProperty("MoneyRange", phatLoot.moneyLower + "-"
                                        + phatLoot.moneyUpper);
            p.setProperty("ExpRange", phatLoot.expLower + "-"
                                        + phatLoot.expUpper);

            String value = "";
            for (String cmd: phatLoot.commands) {
                value = value.concat(", /" + cmd);
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Commands", value);

            value = "";
            for (Loot loot: phatLoot.loots[0]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("IndividualLoots", value);

            value = "";
            for (Loot loot: phatLoot.loots[1]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Coll1", value);

            value = "";
            for (Loot loot: phatLoot.loots[2]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Coll2", value);

            value = "";
            for (Loot loot: phatLoot.loots[3]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Coll3", value);

            value = "";
            for (Loot loot: phatLoot.loots[4]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Coll4", value);

            value = "";
            for (Loot loot: phatLoot.loots[5]) {
                value = value.concat(", " + loot.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("Coll5", value);

            p.setProperty("ItemsPerColl",
                            Integer.toString(phatLoot.numberCollectiveLoots));

            value = "";
            for (PhatLootChest chest: phatLoot.chests) {
                value = value.concat(", " + chest.toString());
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("ChestsData", value);

            value = "";
            for (String chest: phatLoot.oldChests) {
                value = value.concat(", " + chest);
            }
            if (!value.isEmpty()) {
                value = value.substring(2);
            }
            p.setProperty("OldChestsData", value);

            //Write the PhatLoot Properties to file
            fos = new FileOutputStream(dataFolder + "/PhatLoots/"
                                        + phatLoot.name + ".properties");
            p.store(fos, null);
            fos.close();

            //Write the PhatLoot Loot times to file
            fos = new FileOutputStream(dataFolder + "/PhatLoots/"
                                        + phatLoot.name + ".loottimes");
            phatLoot.lootTimes.store(fos, null);
        } catch (Exception saveFailed) {
            logger.severe("Save Failed!");
            saveFailed.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns true if a PhatLoot by the given name exists
     *
     * @return True if a PhatLoot by the given name exists
     */
    public static Boolean hasPhatLoot(String name) {
        return phatLoots.containsKey(name);
    }

    /**
     * Returns the Collection of all PhatLoot
     *
     * @return The Collection of all PhatLoot
     */
    public static Collection<PhatLoot> getPhatLoots() {
        return phatLoots.values();
    }

    /**
     * Adds the given PhatLoot to the collection of PhatLoot
     *
     * @param phatLoots The given PhatLoot
     */
    public static void addPhatLoot(PhatLoot phatLoot) {
        phatLoots.put(phatLoot.name, phatLoot);
        phatLoot.save();
    }

    /**
     * Removes the given PhatLoot from the collection of PhatLoot
     *
     * @param PhatLoot The given PhatLoot
     */
    public static void removePhatLoot(PhatLoot phatLoot) {
        phatLoots.remove(phatLoot.name);
        File trash = new File(dataFolder + "/PhatLoots/"
                                + phatLoot.name + ".properties");
        trash.delete();
        trash = new File(dataFolder + "/PhatLoots/"
                        + phatLoot.name + ".lootTimes");
        trash.delete();
    }

    /**
     * Returns the PhatLoot that contains the given name
     *
     * @param name The name of the PhatLoot
     * @return The PhatLoot with the given name or null if not found
     */
    public static PhatLoot getPhatLoot(String name) {
        return phatLoots.get(name);
    }

    /**
     * Iterates through every PhatLoot to find PhatLoots linked with the given Block
     *
     * @param block The given Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<PhatLoot>();

        for (PhatLoot phatLoot: phatLoots.values()) {
            for (PhatLootChest chest: phatLoot.chests) {
                if (chest.isBlock(block)) {
                    phatLootList.add(phatLoot);
                }
            }
        }

        return phatLootList;
    }

    /**
     * Reloads PhatLoot data
     */
    public static void rl() {
        rl(null);
    }

    /**
     * Reloads PhatLoot data
     *
     * @param player The Player reloading the data
     */
    public static void rl(Player player) {
        phatLoots.clear();
        loadData();

        logger.info("PhatLoots reloaded");
        if (player != null) {
            player.sendMessage("§5PhatLoots reloaded");
        }
    }
}

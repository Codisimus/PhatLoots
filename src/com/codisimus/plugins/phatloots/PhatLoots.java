package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.commands.*;
import com.codisimus.plugins.phatloots.events.ChestRespawnEvent.RespawnReason;
import com.codisimus.plugins.phatloots.gui.InventoryListener;
import com.codisimus.plugins.phatloots.listeners.*;
import com.codisimus.plugins.phatloots.loot.*;
import com.google.common.io.Files;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Listeners/etc.
 *
 * @author Cody
 */
public class PhatLoots extends JavaPlugin {
    public static JavaPlugin plugin;
    public static Logger logger;
    public static Economy econ = null;
    public static String dataFolder;
    public static boolean mythicDropsSupport;
    private static CommandHandler handler;
    private static HashMap<String, PhatLoot> phatLoots = new HashMap<String, PhatLoot>(); //PhatLoot Name -> PhatLoot

    @Override
    public void onDisable() {
        saveLootTimes();

        //Respawn all chests
        for (PhatLootChest chest : (Collection<PhatLootChest>) PhatLootChest.chestsToRespawn.clone()) {
            chest.respawn(RespawnReason.PLUGIN_DISABLED);
        }
    }

    @Override
    public void onEnable () {
        mythicDropsSupport = Bukkit.getPluginManager().isPluginEnabled("MythicDrops");

        //Register ConfigurationSerializable classes
        ConfigurationSerialization.registerClass(PhatLoot.class, "PhatLoot");
        ConfigurationSerialization.registerClass(LootCollection.class, "LootCollection");
        ConfigurationSerialization.registerClass(Item.class, "Item");
        ConfigurationSerialization.registerClass(CommandLoot.class, "Command");
        ConfigurationSerialization.registerClass(Message.class, "Message");
        ConfigurationSerialization.registerClass(Experience.class, "Experience");
        ConfigurationSerialization.registerClass(Money.class, "Money");
        if (mythicDropsSupport) {
            ConfigurationSerialization.registerClass(MythicDropsItem.class, "MythicDropsItem");
        }

        logger = getLogger();
        plugin = this;

        /* Create data folders */
        File dir = this.getDataFolder();
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dataFolder = dir.getPath();

        dir = new File(dataFolder + File.separator + "LootTables");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder + File.separator + "Chests");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder + File.separator + "LootTimes");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        //Save SampleLoot.yml if it does not exist
        File file = new File(PhatLoots.dataFolder + File.separator + "LootTables/SampleLoot.yml");
        if (!file.exists()) {
            InputStream inputStream = this.getResource("SampleLoot.yml");
            OutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(dataFolder + File.separator + "LootTables/SampleLoot.yml");

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not save resource: SampleLoot.yml", ex);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        load();

        /* Register Events */
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PhatLootsListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        if (pm.isPluginEnabled("Citizens")) {
            logger.info("Listening for Citizens NPC deaths");
            pm.registerEvents(new CitizensListener(), this);
        }
        if (getConfig().getBoolean("LootBags")) {
            logger.info("Listening for Loot bags");
            pm.registerEvents(new LootBagListener(), this);
        }
        if (getConfig().getBoolean("DispenserLoot")) {
            logger.info("Listening for Dispensers");
            pm.registerEvents(new DispenserListener(), this);
        }
        if (getConfig().getBoolean("MobDropLoot")) {
            StringBuilder sb = new StringBuilder();
            sb.append("Listening for Mob deaths");
            MobDeathListener listener = new MobDeathListener();
            listener.mobWorlds = getConfig().getBoolean("WorldMobDropLoot");
            listener.mobRegions = getConfig().getBoolean("RegionMobDropLoot")
                                  && (pm.isPluginEnabled("RegionOwn")
                                      || pm.isPluginEnabled("WorldGuard"));
            if (listener.mobWorlds) {
                sb.append(" w/ MultiWorld support");
            }
            if (listener.mobRegions) {
                sb.append(listener.mobWorlds ? " and " : " w/ ");
                sb.append(pm.isPluginEnabled("RegionOwn") ? "RegionOwn" : "WorldGuard");
                sb.append(" Regions");
            }
            logger.info(sb.toString());
            pm.registerEvents(listener, this);
        }
        if (getConfig().getBoolean("MobSpawnLoot")) {
            StringBuilder sb = new StringBuilder();
            sb.append("Listening for Mob spawns");
            MobSpawnListener listener = new MobSpawnListener();
            listener.mobWorlds = getConfig().getBoolean("WorldMobSpawnLoot");
            listener.mobRegions = getConfig().getBoolean("RegionMobSpawnLoot")
                                  && (pm.isPluginEnabled("RegionOwn")
                                      || pm.isPluginEnabled("WorldGuard"));
            if (listener.mobWorlds) {
                sb.append(" w/ MultiWorld support");
            }
            if (listener.mobRegions) {
                sb.append(listener.mobWorlds ? " and " : " w/ ");
                sb.append(pm.isPluginEnabled("RegionOwn") ? "RegionOwn" : "WorldGuard");
                sb.append(" support");
            }
            logger.info(sb.toString());
            pm.registerEvents(listener, this);
        }
        if (getConfig().getBoolean("FishingLoot")) {
            logger.info("Listening for Players fishing");
            pm.registerEvents(new FishingListener(), this);
        }
        if (getConfig().getBoolean("VotifierLoot")) {
            logger.info("Listening for Votifier votes");
            pm.registerEvents(new VoteListener(), this);
        }

        /* Register the command found in the plugin.yml */
        //PhatLootsCommand.command = (String) getDescription().getCommands().keySet().toArray()[0];
        //getCommand(PhatLootsCommand.command).setExecutor(new PhatLootsCommand());
        handler = new CommandHandler(this, (String) getDescription().getCommands().keySet().toArray()[0]);
        handler.registerCommands(LootCommand.class);
        handler.registerCommands(ManageLootCommand.class);
        handler.registerCommands(VariableLootCommand.class);
        if (PhatLoots.econ != null) {
            handler.registerCommands(ManageMoneyLootCommand.class);
        }
        if (mythicDropsSupport) {
            handler.registerCommands(ManageMythicDropsLootCommand.class);
        }

        /* Register Buttons */
        LootCollection.registerButton();
        Experience.registerButton();
        if (PhatLoots.econ != null) {
            Money.registerButton();
        }
        if (mythicDropsSupport) {
            MythicDropsItem.registerButtonAndTool();
        }

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
     * Reloads the config from the config.yml file.
     * Loads values from the newly loaded config.
     * This method is automatically called when the plugin is enabled
     */
    @Override
    public void reloadConfig() {
        //Save the config file if it does not already exist
        PhatLoots.plugin.saveDefaultConfig();

        //Reload the config as this method would normally do if not overriden
        super.reloadConfig();

        //Load values from the config now that it has been reloaded
        PhatLootsConfig.load();

        setupEconomy();
    }

    /**
     * Loads each PhatLoot that has a LootTable file
     */
    public static void load() {
        //Load each YAML file in the LootTables folder
        File dir = new File(dataFolder + File.separator + "LootTables");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".yml");
            }
        });
        for (File file : files) {
            try {
                String name = file.getName();
                name = name.substring(0, name.length() - 4);
                YamlConfiguration config = loadConfig(file);

                //Ensure the PhatLoot name matches the file name
                PhatLoot phatLoot = (PhatLoot) config.get(config.contains(name)
                                                          ? name
                                                          : config.getKeys(false).iterator().next());
                if (!phatLoot.name.equals(name)) {
                    phatLoot.name = name;
                }
                phatLoots.put(name, phatLoot);
                phatLoot.save();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to load " + file.getName(), ex);
            }
        }
    }

    /**
     * Saves all data for each PhatLoot
     */
    public static void saveAll() {
        for (PhatLoot phatLoot : phatLoots.values()) {
            phatLoot.saveAll();
        }
    }

    /**
     * Returns true if a PhatLoot by the given name exists
     *
     * @return true if a PhatLoot by the given name exists
     */
    public static boolean hasPhatLoot(String name) {
        return phatLoots.containsKey(name);
    }

    /**
     * Returns the Collection of all PhatLoots
     *
     * @return The Collection of all PhatLoots
     */
    public static Collection<PhatLoot> getPhatLoots() {
        return phatLoots.values();
    }

    /**
     * Adds the given PhatLoot to the collection of PhatLoots
     *
     * @param phatLoots The given PhatLoot
     */
    public static void addPhatLoot(PhatLoot phatLoot) {
        phatLoots.put(phatLoot.name, phatLoot);
        phatLoot.save();
    }

    /**
     * Removes the given PhatLoot from the collection of PhatLoots.
     * PhatLoot files are also deleted
     *
     * @param PhatLoot The given PhatLoot
     */
    public static void removePhatLoot(PhatLoot phatLoot) {
        phatLoots.remove(phatLoot.name);
        new File(dataFolder + File.separator + "LootTables" + File.separator + phatLoot.name + ".yml").delete();
        new File(dataFolder + File.separator + "Chests" + File.separator + phatLoot.name + ".txt").delete();
        new File(dataFolder + File.separator + "LootTimes" + File.separator + phatLoot.name + ".properties").delete();
    }

    /**
     * Returns the PhatLoot of the given name
     *
     * @param name The name of the PhatLoot
     * @return The PhatLoot with the given name or null if not found
     */
    public static PhatLoot getPhatLoot(String name) {
        return name == null ? null : phatLoots.get(name);
    }

    /**
     * Iterates through every PhatLoot to find PhatLoots linked with the given Block
     *
     * @param block The given Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<PhatLoot>();
        PhatLootChest chest = PhatLootChest.getChest(block);
        for (PhatLoot phatLoot : phatLoots.values()) {
            if (phatLoot.containsChest(chest)) {
                phatLootList.add(phatLoot);
            }
        }
        return phatLootList;
    }

    /**
     * Iterates through every PhatLoot to find PhatLoots linked with the given Block.
     * PhatLoots are only added to the List if the Player has permission to loot them
     *
     * @param block The given Block
     * @param player The given Player
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block, Player player) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<PhatLoot>();
        PhatLootChest chest = PhatLootChest.getChest(block);
        for (PhatLoot phatLoot : phatLoots.values()) {
            if (phatLoot.containsChest(chest) && PhatLootsUtil.canLoot(player, phatLoot)) {
                phatLootList.add(phatLoot);
            }
        }
        return phatLootList;
    }

    /**
     * Returns the CommandHandler used to execute the /loot command
     *
     * @return The CommandHandler of PhatLoots
     */
    public static CommandHandler getCommandHandler() {
        return handler;
    }

    /**
     * Retrieves the registered Economy plugin
     *
     * @return true if an Economy plugin has been found
     */
    private boolean setupEconomy() {
        //Return if Vault is not enabled
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = (Economy) rsp.getProvider();
        return econ != null;
    }

    /**
     * Saves Loot times of each PhatLoot to file
     */
    public static void saveLootTimes() {
        for (PhatLoot phatLoot : getPhatLoots()) {
            //Clean up the loot times before writing to file
            phatLoot.clean(null);
            phatLoot.saveLootTimes();
        }
    }

    /**
     * Reloads PhatLoot data
     */
    public static void rl() {
        rl(null);
    }

    /**
     * Reloads PhatLoot data and settings
     *
     * @param sender The CommandSender reloading the plugin
     */
    public static void rl(CommandSender sender) {
        saveLootTimes();

        phatLoots.clear();
        plugin.reloadConfig();
        load();

        logger.info("PhatLoots reloaded");
        if (sender instanceof Player) {
            sender.sendMessage("§5PhatLoots reloaded");
        }
    }

    /**
     * Does the same as getConfig() but for the given config file.
     *
     * @param file The file to load
     * @return The YamlConfiguration loaded
     */
    private static YamlConfiguration loadConfig(File file) {
        YamlConfiguration fileConfiguration = new YamlConfiguration();
        try {
            if (isValidUTF8(Files.toByteArray(file))) {
                fileConfiguration.loadFromString(Files.toString(file, Charset.forName("UTF-8")));
            } else {
                fileConfiguration.load(file);
            }
        } catch (Exception ex) {
            PhatLoots.logger.log(Level.SEVERE, "§4Could not load data from " + file, ex);
        }
        return fileConfiguration;
    }

    /**
     * Checks if the given byte array is UTF-8 encoded.
     *
     * @param bytes The array of bytes to check for validity
     * @return true when validly UTF8 encoded
     */
    private static boolean isValidUTF8(byte[] bytes) {
        try {
            Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}

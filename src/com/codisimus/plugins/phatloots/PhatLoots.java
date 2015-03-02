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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Loads Plugin and manages Data/Listeners/etc.
 *
 * @author Codisimus
 */
public class PhatLoots extends JavaPlugin {
    public static JavaPlugin plugin;
    public static Logger logger;
    public static Economy econ = null;
    public static String dataFolder;
    public static boolean mythicDropsSupport;
    public static long autoSavePeriod;
    public static CommandHandler handler;
    public static final EnumMap<Material, HashMap<String, String>> types = new EnumMap(Material.class); //Material -> World Name -> PhatLoot Name
    private static final HashMap<String, PhatLoot> phatLoots = new HashMap<>(); //PhatLoot Name -> PhatLoot

    public static void main(String[] args) {
        //Do Nothing - For debugging within NetBeans IDE
    }

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

        logger = getLogger();
        plugin = this;

        /* Create data folders */
        File dir = this.getDataFolder();
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dataFolder = dir.getPath();

        dir = new File(dataFolder, "LootTables");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder, "Chests");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder, "LootTimes");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        dir = new File(dataFolder, "Addons");
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        //Save SampleLoot.yml if it does not exist
        File file = new File(dataFolder, "LootTables" + File.separator + "SampleLoot.yml");
        if (!file.exists()) {
            try (InputStream inputStream = this.getResource("SampleLoot.yml")) {
                try (OutputStream outputStream = new FileOutputStream(file)) {
                    int read;
                    byte[] bytes = new byte[1024];

                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not save resource: SampleLoot.yml", ex);
            }
        }

        /* Register Events */
        registerEvents();

        /* Register Buttons */
        LootCollection.registerButton();
        Experience.registerButton();
        if (PhatLoots.econ != null) {
            Money.registerButton();
        }
        if (mythicDropsSupport) {
            MythicDropsItem.registerButtonAndTool();
        }

        /* Register the command found in the plugin.yml */
        String command = (String) getDescription().getCommands().keySet().toArray()[0];
        handler = new CommandHandler(this, command);
        if (mythicDropsSupport) {
            handler.registerCommands(ManageMythicDropsLootCommand.class);
        }
        handler.registerCommands(LootCommand.class);
        handler.registerCommands(ManageLootCommand.class);
        handler.registerCommands(VariableLootCommand.class);
        if (PhatLoots.econ != null) {
            handler.registerCommands(ManageMoneyLootCommand.class);
        }

        /* Load External PhatLoots Addons */
        //Buttons, Tools, CodCommands, and ConfigurationSerializable classes should be registered from within the Addon
        for (Plugin addon : Bukkit.getPluginManager().loadPlugins(dir)) {
            Bukkit.getPluginManager().enablePlugin(addon);
        }

        /* Load PhatLoot/Chest data */
        load();

        /* Start save repeating task */
        if (autoSavePeriod > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveLootTimes();
                }
            }.runTaskTimer(this, autoSavePeriod, autoSavePeriod);
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
        saveDefaultConfig();

        //Reload the config as this method would normally do if not overriden
        super.reloadConfig();

        //Load values from the config now that it has been reloaded
        PhatLootsConfig.load();

        setupEconomy();
    }

    /**
     * Registers all Event Listeners that PhatLoots uses
     * Most of these Listeners may be turned of from the config
     * Enabled Listeners are logged at the INFO level
     */
    private void registerEvents() {
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
    }

    /**
     * Loads each PhatLoot that has a LootTable file
     */
    public static void load() {
        //Load each YAML file in the LootTables folder
        File dir = new File(dataFolder, "LootTables");
        for (File file : dir.listFiles(PhatLootsUtil.YAML_FILTER)) {
            try {
                String name = file.getName();
                name = name.substring(0, name.length() - PhatLootsUtil.YAML_EXTENSION.length());
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
     * @param name The given name
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
     * @param phatLoot The given PhatLoot
     */
    public static void addPhatLoot(PhatLoot phatLoot) {
        phatLoots.put(phatLoot.name, phatLoot);
        phatLoot.save();
    }

    /**
     * Removes the given PhatLoot from the collection of PhatLoots.
     * PhatLoot files are also deleted
     *
     * @param phatLoot The given PhatLoot
     */
    public static void removePhatLoot(PhatLoot phatLoot) {
        phatLoots.remove(phatLoot.name);
        new File(dataFolder, "LootTables" + File.separator + phatLoot.name + PhatLootsUtil.YAML_EXTENSION).delete();
        new File(dataFolder, "Chests" + File.separator + phatLoot.name + PhatLootsUtil.TEXT_EXTENSION).delete();
        new File(dataFolder, "LootTimes" + File.separator + phatLoot.name + PhatLootsUtil.PROPERTIES_EXTENSION).delete();
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
     * Returns a List of all PhatLoots that are linked to the given Block
     * PhatLoots which the given Player does not have permission to loot are not returned
     *
     * @param block The given Block
     * @param player The given Player
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block, Player player) {
        LinkedList<PhatLoot> phatLootList = getPhatLoots(block);
        Iterator<PhatLoot> itr = phatLootList.iterator();
        while (itr.hasNext()) {
            PhatLoot phatLoot = itr.next();
            if (!PhatLootsUtil.canLoot(player, phatLoot)) {
                phatLootList.remove(phatLoot);
            }
        }
        return phatLootList;
    }

    /**
     * Returns a List of all PhatLoots that are linked to the given Block
     * AutoLinking is first checked and then explicit linking if AutoLinking returns 0 results
     *
     * @param block The given Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block) {
        LinkedList<PhatLoot> phatLootList = getAutoLinkedPhatLoots(block);

        if (phatLootList.isEmpty()) {
            phatLootList = getExplicitlyLinkedPhatLoots(block);
        }

        return phatLootList;
    }

    /**
     * Returns a List of all PhatLoots that are automatically linked to the given Block
     *
     * @param block The given Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getAutoLinkedPhatLoots(Block block) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<>();

        if (PhatLootsUtil.isLinkableType(block)) {
            HashMap<String, String> map = types.get(block.getType());
            if (map != null) {
                String world = block.getWorld().getName();
                String pNameList = map.containsKey(world)
                                 ? map.get(world)
                                 : map.get("all");
                if (pNameList != null) {
                    for (String pName : pNameList.split("; ")) {
                        PhatLoot phatLoot = PhatLoots.getPhatLoot(pName);
                        if (phatLoot == null) {
                            PhatLoots.logger.warning("PhatLoot " + pName + " does not exist.");
                            PhatLoots.logger.warning("Please adjust your config or create the PhatLoot");
                        }

                        phatLootList.add(phatLoot);
                    }
                }
            }
        }

        return phatLootList;
    }

    /**
     * Returns a List of all PhatLoots that are explicitly linked to the given Block
     *
     * @param block The given Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getExplicitlyLinkedPhatLoots(Block block) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<>();

        if (PhatLootsUtil.isLinkableType(block)) {
            block = PhatLootsUtil.getLeftSide(block);
            if (PhatLootChest.isPhatLootChest(block)) {
                PhatLootChest chest = PhatLootChest.getChest(block);
                for (PhatLoot phatLoot : phatLoots.values()) {
                    if (phatLoot.containsChest(chest)) {
                        phatLootList.add(phatLoot);
                    }
                }
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
            logger.log(Level.SEVERE, "§4Could not load data from " + file, ex);
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

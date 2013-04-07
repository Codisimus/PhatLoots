package com.codisimus.plugins.phatloots;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
    static Economy econ = null;
    static JavaPlugin plugin;
    static String dataFolder;
    private static HashMap<String, PhatLoot> phatLoots = new HashMap<String, PhatLoot>();

    @Override
    public void onDisable() {
        for (PhatLoot phatLoot : getPhatLoots()) {
            phatLoot.clean(null);
            phatLoot.saveLootTimes();
        }
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     */
    @Override
    public void onEnable () {
        //Metrics hook
        try { new Metrics(this).start(); } catch (IOException e) {}

        ConfigurationSerialization.registerClass(PhatLoot.class, "PhatLoot");
        ConfigurationSerialization.registerClass(OldLoot.class, "Loot");

        server = getServer();
        logger = getLogger();
        pm = server.getPluginManager();
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

        load();
        PhatLootsConfig.load();

        setupEconomy();

        /* Register Events */
        pm.registerEvents(new PhatLootsListener(), this);
        if (pm.isPluginEnabled("EpicBossRecoded")) {
            pm.registerEvents(new EBRListener(), this);
        }
        if (getConfig().getBoolean("DispenserLoot")) {
            pm.registerEvents(new DispenserListener(), this);
        }
        if (getConfig().getBoolean("MobDropLoot")) {
            MobDeathListener listener = new MobDeathListener();
            listener.mobWorlds = getConfig().getBoolean("WorldMobDropLoot");
            listener.mobRegions = getConfig().getBoolean("RegionMobDropLoot")
                                  && pm.isPluginEnabled("RegionOwn");
            pm.registerEvents(listener, this);
        }
        if (getConfig().getBoolean("MobSpawnLoot")) {
            MobSpawnListener listener = new MobSpawnListener();
            listener.mobWorlds = getConfig().getBoolean("WorldMobSpawnLoot");
            listener.mobRegions = getConfig().getBoolean("RegionMobSpawnLoot")
                                  && pm.isPluginEnabled("RegionOwn");
            pm.registerEvents(listener, this);
        }

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
     * Returns true if the given player is allowed to loot the specified PhatLoot
     *
     * @param player The Player who is being checked for permission
     * @param phatLoot The PhatLoot in question
     * @return true if the given player is allowed to loot the PhatLoot
     */
    public static boolean canLoot(Player player, PhatLoot phatLoot) {
        if (PhatLootsConfig.restrictAll || PhatLootsConfig.restricted.contains(phatLoot.name)) {
            if (player.hasPermission("phatloots.loot.*")) { //Check for loot all permission
                return true;
            } else {
                return player.hasPermission("phatloots.loot." + phatLoot.name); //Check if the Player has the specific loot permission
            }
        } else {
            return true;
        }
    }

    public static void load() {
        File dir = new File(dataFolder + File.separator + "LootTables");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".yml");
                }
            });
        if (files.length == 0) {
            loadOld();
        }
        for (File file : files) {
            try {
                String name = file.getName();
                name = name.substring(0, name.length() - 4);

                YamlConfiguration config = new YamlConfiguration();
                config.load(file);
                PhatLoot phatLoot = (PhatLoot)config.get(name);
                phatLoots.put(name, phatLoot);
            } catch (Exception ex) {
                logger.severe("Failed to load " + file.getName());
                ex.printStackTrace();
            }
        }
    }

    public static void saveAll() {
        for (PhatLoot phatLoot : phatLoots.values()) {
            phatLoot.saveAll();
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
     * Removes the given PhatLoot from the collection of PhatLoots
     * PhatLoot files are also deleted
     *
     * @param PhatLoot The given PhatLoot
     */
    public static void removePhatLoot(PhatLoot phatLoot) {
        phatLoots.remove(phatLoot.name);
        File trash = new File(dataFolder + File.separator + "LootTables" + File.separator + phatLoot.name + ".yml");

        trash.delete();
        trash = new File(dataFolder + File.separator + "Chests" + File.separator + phatLoot.name + ".txt");

        trash.delete();
        trash = new File(dataFolder + File.separator + "LootTimes" + File.separator + phatLoot.name + ".properties");

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

        PhatLootChest chest = new PhatLootChest(block);
        for (PhatLoot phatLoot : phatLoots.values()) {
            if (phatLoot.containsChest(chest)) {
                phatLootList.add(phatLoot);
            }
        }

        return phatLootList;
    }

    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public static boolean isPhatLootChest(Block block) {
        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterates through every PhatLoot to find PhatLoots linked with the given Block
     * PhatLoots are only added to the List if the Player has permission to loot them
     *
     * @param block The given Block
     * @param player The given Player
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Block block, Player player) {
        LinkedList<PhatLoot> phatLootList = new LinkedList<PhatLoot>();

        PhatLootChest chest = new PhatLootChest(block);
        for (PhatLoot phatLoot : phatLoots.values()) {
            if (phatLoot.containsChest(chest) && canLoot(player, phatLoot)) {
                phatLootList.add(phatLoot);
            }
        }

        return phatLootList;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = (Economy) rsp.getProvider();
        return econ != null;
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
    public static void rl(CommandSender sender) {
        phatLoots.clear();
        load();

        logger.info("PhatLoots reloaded");
        if (sender instanceof Player) {
            sender.sendMessage("§5PhatLoots reloaded");
        }
    }

    /**
     * Plays animation/sound for opening a virtual Inventory
     *
     * @param player The Player opening the Inventory
     * @param inv The virtual Inventory being opened
     * @param loc The Location of the Chest which we want to be animation
     * @param global Whether the animation should be sent to everyone (true) or just the Player (false)
     */
    public static void openInventory(Player player, Inventory inv, Location loc, Boolean global) {
        if (global) {
            if (inv.getViewers().size() <= 1) { //First viewer
                //Play for each Player in the World
                for (Player p: player.getWorld().getPlayers()) {
                    p.playSound(loc, Sound.CHEST_OPEN, 0.75F, 0.95F);
                    p.playNote(loc, (byte) 1, (byte) 1); //Open animation
                }
            }
        } else {
            //Play for only the individual Player
            player.playSound(loc, Sound.CHEST_OPEN, 0.75F, 0.95F);
            player.playNote(loc, (byte) 1, (byte) 1); //Open animation
        }
    }

    /**
     * Plays animation/sound for closing a virtual Inventory
     *
     * @param player The Player closing the Inventory
     * @param inv The virtual Inventory being closed
     * @param loc The Location of the Chest which we want to be animation
     * @param global Whether the animation should be sent to everyone (true) or just the Player (false)
     */
    public static void closeInventory(Player player, Inventory inv, Location loc, Boolean global) {
        if (global) {
            if (inv.getViewers().size() <= 1) { //Last viewer
                //Play for each Player in the World
                for (Player p: player.getWorld().getPlayers()) {
                    p.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                    p.playNote(loc, (byte) 1, (byte) 0); //Close animation
                }
            }
        } else {
            //Play for only the individual Player
            player.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
            player.playNote(loc, (byte) 1, (byte) 0); //Close animation
        }
    }

    /* OLD */

public static void loadOld()
  {
    FileInputStream fis = null;
    File dir = new File(dataFolder + File.separator + "PhatLoots");
    if (!dir.exists()) {
      return;
    }
    for (File file : dir.listFiles()) {
      String name = file.getName();
      if (!name.endsWith(".properties"))
        continue;
      try {
        Properties p = new Properties();
        fis = new FileInputStream(file);
        p.load(fis);
        fis.close();

        PhatLoot phatLoot = new PhatLoot(name.substring(0, name.length() - 11));

        String[] resetTime = p.getProperty("ResetTime").split("'");
        phatLoot.days = Integer.parseInt(resetTime[0]);
        phatLoot.hours = Integer.parseInt(resetTime[1]);
        phatLoot.minutes = Integer.parseInt(resetTime[2]);
        phatLoot.seconds = Integer.parseInt(resetTime[3]);

        phatLoot.global = Boolean.parseBoolean(p.getProperty("GlobalReset"));
        phatLoot.round = Boolean.parseBoolean(p.getProperty("RoundDownTime"));

        String[] moneyRange = p.getProperty("MoneyRange").split("-");
        phatLoot.moneyLower = Integer.parseInt(moneyRange[0]);
        phatLoot.moneyUpper = Integer.parseInt(moneyRange[0]);

        if (p.containsKey("ExpRange")) {
          String[] expRange = p.getProperty("ExpRange").split("-");
          phatLoot.expLower = Integer.parseInt(expRange[0]);
          phatLoot.expUpper = Integer.parseInt(expRange[0]);
        }

        if (p.containsKey("Commands")) {
          String value = p.getProperty("Commands");
          if (!value.isEmpty()) {
            for (String string : value.split(", ")) {
              if (string.startsWith("/")) {
                string = string.substring(1);
              }

              phatLoot.commands.add(string);
            }
          }

        }

        phatLoot.setLoots(0, p.getProperty("IndividualLoots"));
        phatLoot.setLoots(1, p.getProperty("Coll1"));
        phatLoot.setLoots(2, p.getProperty("Coll2"));
        phatLoot.setLoots(3, p.getProperty("Coll3"));
        phatLoot.setLoots(4, p.getProperty("Coll4"));
        phatLoot.setLoots(5, p.getProperty("Coll5"));

        phatLoot.numberCollectiveLoots = Integer.parseInt(p.getProperty("ItemsPerColl"));

        phatLoot.setChests(p.getProperty("ChestsData"));

        phatLoots.put(phatLoot.name, phatLoot);

        fis.close();

        file = new File(dataFolder + File.separator + "PhatLoots" + File.separator + phatLoot.name + ".loottimes");

        if (file.exists()) {
          fis = new FileInputStream(file);
          phatLoot.lootTimes.load(fis);
          if (phatLoot.lootTimes.values().toString().contains("'"))
            phatLoot.convertLootTimes();
        }
      }
      catch (Exception loadFailed) {
        logger.severe("Failed to load " + name);
        loadFailed.printStackTrace();
      } finally {
        try {
          fis.close();
        }
        catch (Exception e) {
        }
      }
    }
    saveAll();
  }
}

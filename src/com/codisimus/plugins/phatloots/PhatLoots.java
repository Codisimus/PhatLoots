package com.codisimus.plugins.phatloots;

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
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Listeners/etc.
 *
 * @author Codisimus
 */
public class PhatLoots extends JavaPlugin {
    static JavaPlugin plugin;
    static Logger logger;
    static Economy econ = null;
    static String dataFolder;
    private static Random random = new Random();
    private static HashMap<String, PhatLoot> phatLoots = new HashMap<String, PhatLoot>();

    @Override
    public void onDisable() {
        //Save the Loot times for each PhatLoot
        for (PhatLoot phatLoot : getPhatLoots()) {
            //Clean up the loot times before writing to file
            phatLoot.clean(null);
            phatLoot.saveLootTimes();
        }
    }

    @Override
    public void onEnable () {
        //Metrics hook
        try { new Metrics(this).start(); } catch (IOException e) {}

        //Register ConfigurationSerializable classes
        ConfigurationSerialization.registerClass(OldLoot.class, "Loot");
        ConfigurationSerialization.registerClass(PhatLoot.class, "PhatLoot");
        ConfigurationSerialization.registerClass(LootCollection.class, "LootCollection");
        ConfigurationSerialization.registerClass(Item.class, "Item");
        ConfigurationSerialization.registerClass(CommandLoot.class, "Command");

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

        load();

        /* Register Events */
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PhatLootsListener(), this);
        pm.registerEvents(new PhatLootInfoListener(), this);
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
        if (getConfig().getBoolean("FishingLoot")) {
            pm.registerEvents(new FishingListener(), this);
        }
        if (getConfig().getBoolean("VotifierLoot")) {
            pm.registerEvents(new VoteListener(), this);
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
     * Reloads the config from the config.yml file
     * Loads values from the newly loaded config
     * This method is automatically called when the plugin is enabled
     */
    @Override
    public void reloadConfig() {
        //Reload the config as this method would normally do if not overriden
        super.reloadConfig();

        //Save the config file if it does not already exist
        PhatLoots.plugin.saveDefaultConfig();

        //Load values from the config now that it has been reloaded
        PhatLootsConfig.load();

        setupEconomy();
    }

    /**
     * Returns true if the given player is allowed to loot the specified PhatLoot
     *
     * @param player The Player who is being checked for permission
     * @param phatLoot The PhatLoot in question
     * @return true if the player is allowed to loot the PhatLoot
     */
    public static boolean canLoot(Player player, PhatLoot phatLoot) {
        //Check if the PhatLoot is restricted
        if (PhatLootsConfig.restrictAll || PhatLootsConfig.restricted.contains(phatLoot.name)) {
            return player.hasPermission("phatloots.loot.*") //Check for the loot all permission
                   ? true
                   : player.hasPermission("phatloots.loot." + phatLoot.name); //Check if the Player has the specific loot permission
        } else {
            return true;
        }
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
        if (files.length == 0) {
            loadOld();
        }
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
     * Removes the given PhatLoot from the collection of PhatLoots
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
     * Returns true if the given Block is a type that is able to be linked
     *
     * @param block the given Block
     * @return true if the given Block is able to be linked
     */
    public static boolean isLinkableType(Block block) {
        return PhatLootsListener.types.containsKey(block.getType());
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
        PhatLootChest chest = PhatLootChest.getChest(block);
        for (PhatLoot phatLoot : phatLoots.values()) {
            if (phatLoot.containsChest(chest) && canLoot(player, phatLoot)) {
                phatLootList.add(phatLoot);
            }
        }
        return phatLootList;
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
        phatLoots.clear();
        plugin.reloadConfig();
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
     * @param chest The Chest which we want to be animation
     * @param global Whether the animation should be sent to everyone (true) or just the Player (false)
     */
    public static void closeInventory(Player player, Inventory inv, PhatLootChest chest, Boolean global) {
        player.closeInventory();
        Block block = chest.getBlock();
        switch (block.getType()) {
        case CHEST:
        case TRAPPED_CHEST:
        case ENDER_CHEST:
            Location loc = block.getLocation();
            if (global) {
                if (inv.getViewers().size() <= 1) { //Last viewer
                    //Play for each Player in the World
                    for (Player p: player.getWorld().getPlayers()) {
                        p.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                        p.playNote(loc, (byte) 1, (byte) 0); //Close animation
                    }

                    for (ItemStack item : inv.getContents()) {
                        if (item != null && item.getTypeId() != 0) {
                            break;
                        }
                    }

                    long time = -1;
                    for (PhatLoot phatLoot : phatLoots.values()) {
                        if (phatLoot.containsChest(chest) && phatLoot.breakAndRespawn) {
                            if (phatLoot.global) {
                                long temp = phatLoot.getTimeRemaining(chest);
                                if (temp < 1) {
                                    continue;
                                }
                                if (time < 0 || temp < time) {
                                    time = temp;
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if (time > 1) {
                        chest.breakChest(time);
                    }
                }
            } else {
                //Play for only the individual Player
                player.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                player.playNote(loc, (byte) 1, (byte) 0); //Close animation
            }
            break;
        default: break;
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
        Block block = loc.getBlock();
        switch (block.getType()) {
        case CHEST:
        case TRAPPED_CHEST:
        case ENDER_CHEST:
            if (global) {
                if (inv.getViewers().size() <= 1) { //Last viewer
                    //Play for each Player in the World
                    for (Player p: player.getWorld().getPlayers()) {
                        p.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                        p.playNote(loc, (byte) 1, (byte) 0); //Close animation
                    }

                    for (ItemStack item : inv.getContents()) {
                        if (item != null && item.getTypeId() != 0) {
                            break;
                        }
                    }

                    PhatLootChest chest = PhatLootChest.getChest(loc.getBlock());
                    long time = -1;
                    for (PhatLoot phatLoot : phatLoots.values()) {
                        if (phatLoot.containsChest(chest) && phatLoot.breakAndRespawn) {
                            if (phatLoot.global) {
                                long temp = phatLoot.getTimeRemaining(chest);
                                if (temp < 1) {
                                    continue;
                                }
                                if (time < 0 || temp < time) {
                                    time = temp;
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if (time > 1) {
                        chest.breakChest(time);
                    }
                }
            } else {
                //Play for only the individual Player
                player.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                player.playNote(loc, (byte) 1, (byte) 0); //Close animation
            }
            break;
        default: break;
        }
    }

    /**
     * Returns a random int between 0 (inclusive) and y (inclusive)
     *
     * @param upper y
     * @return a random int between 0 and y
     */
    public static int rollForInt(int upper) {
        return random.nextInt(upper + 1); //+1 is needed to make it inclusive
    }

    /**
     * Returns a random int between x (inclusive) and y (inclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random int between x and y
     */
    public static int rollForInt(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Returns a random double between 0 (inclusive) and y (exclusive)
     *
     * @param upper y
     * @return a random double between 0 and y
     */
    public static double rollForDouble(double upper) {
        return random.nextDouble() * upper;
    }

    /**
     * Returns a random double between x (inclusive) and y (exclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random double between x and y
     */
    public static double rollForDouble(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Does the same as getConfig() but for the given config file.
     *
     * @param file The file to load
     * @return The YamlConfiguration loaded
     */
    public static YamlConfiguration loadConfig(File file) {
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
    public static boolean isValidUTF8(byte[] bytes) {
        try {
            Charset.availableCharsets().get("UTF-8").newDecoder().decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    /* OLD */
@Deprecated
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

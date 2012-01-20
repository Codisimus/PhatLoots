package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.listeners.BlockEventListener;
import com.codisimus.plugins.phatloots.listeners.CommadListener;
import com.codisimus.plugins.phatloots.listeners.PlayerEventListener;
import java.io.*;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Permissions
 *
 * @author Codisimus
 */
public class PhatLootsMain extends JavaPlugin {
    private static Permission permission;
    public static PluginManager pm;
    public static Server server;
    static int defaultDays;
    static int defaultHours;
    static int defaultMinutes;
    static int defaultSeconds;
    static int defaultNumberOfLoots;
    static boolean defaultGlobal;
    static boolean defaultRound;
    static boolean autoLock;
    private Properties p;
    static boolean autoLoot;
    static String autoLootMsg;
    static boolean displayTimeRemaining;
    static String timeRemainingMsg;
    public static Random random = new Random();
    public static LinkedList<PhatLoots> phatLootsList = new LinkedList<PhatLoots>();
    public static boolean save = true;

    @Override
    public void onDisable () {
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        server = getServer();
        pm = server.getPluginManager();
        
        //Load PhatLoots Data
        loadData();
        
        //Load Config settings
        loadSettings();
        
        //Find Permissions
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        
        //Find Economy
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            Econ.economy = economyProvider.getProvider();
        
        //Register Events
        pm.registerEvent(Type.PLAYER_INTERACT, new PlayerEventListener(), Priority.Monitor, this);
        pm.registerEvent(Type.BLOCK_DAMAGE, new BlockEventListener(), Priority.Normal, this);
        getCommand("loot").setExecutor(new CommadListener());
        
        System.out.println("PhatLoots "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Moves file from PhatLoots.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/PhatLoots.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/PhatLoots/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            
            //Copy the file
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception moveFailed) {
            System.err.println("[PhatLoots] File Move Failed!");
            moveFailed.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        p = new Properties();
        try {
            //Copy the file from the jar if it is missing
            if (!new File("plugins/PhatLoots/config.properties").exists())
                moveFile("config.properties");
            
            FileInputStream fis = new FileInputStream("plugins/PhatLoots/config.properties");
            p.load(fis);
            
            autoLoot = Boolean.parseBoolean(loadValue("AutoLoot"));
            autoLootMsg = format(loadValue(("AutoLootMessage")));
            displayTimeRemaining = Boolean.parseBoolean(loadValue("DisplayTimeRemaining"));
            timeRemainingMsg = format(loadValue("TimeRemainingMessage"));

            //Load default reset time
            String[] resetTime = loadValue("DefaultResetTime").split("'");
            defaultDays = Integer.parseInt(resetTime[0]);
            defaultHours = Integer.parseInt(resetTime[1]);
            defaultMinutes = Integer.parseInt(resetTime[2]);
            defaultSeconds = Integer.parseInt(resetTime[3]);

            defaultGlobal = Boolean.parseBoolean(loadValue("GlobalResetByDefault"));
            defaultRound = Boolean.parseBoolean(loadValue("RoundDownTimeByDefault"));
            defaultNumberOfLoots = Integer.parseInt(loadValue("DefaultItemsPerColl"));
            
            fis.close();
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load PhatLoots "+this.getDescription().getVersion());
            missingProp.printStackTrace();
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
            System.err.println("[PhatLoots] Missing value for "+key+" in config file");
            System.err.println("[PhatLoots] Please regenerate config file");
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
        return permission.has(player, "phatloots."+type);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
    
    /**
     * Loads properties for each PhatLoots from save files
     * Disables saving if an error occurs
     */
    public static void loadData() {
        try {
            File[] files = new File("plugins/PhatLoots").listFiles();
            Properties p = new Properties();
            
            boolean update = false;

            //Load each .dat file
            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".dat")) {
                    FileInputStream fis = new FileInputStream(file);
                    p.load(fis);
                    
                    //Construct a new Warp using the file name
                    PhatLoots phatLoots = new PhatLoots(name.substring(0, name.length() - 4));
                    
                    //Set the reset time
                    String[] resetTime = p.getProperty("ResetTime").split("'");
                    phatLoots.days = Integer.parseInt(resetTime[0]);
                    phatLoots.hours = Integer.parseInt(resetTime[1]);
                    phatLoots.minutes = Integer.parseInt(resetTime[2]);
                    phatLoots.seconds = Integer.parseInt(resetTime[3]);
                    
                    //Set the reset type
                    try {
                        phatLoots.global = Boolean.parseBoolean(p.getProperty("GlobalReset"));
                        phatLoots.round = Boolean.parseBoolean(p.getProperty("RoundDownTime"));
                    }
                    catch (Exception oldFile) {
                        String resetType = p.getProperty("ResetType");
                        if (resetType.equals("player"))
                            phatLoots.global = false;
                        else if (resetType.equals("global"))
                            phatLoots.global = true;
                        
                        update = true;
                    }
                    
                    //Set the money range
                    String[] moneyRange = p.getProperty("MoneyRange").split("-");
                    phatLoots.rangeLow = Integer.parseInt(moneyRange[0]);
                    phatLoots.rangeHigh = Integer.parseInt(moneyRange[1]);
                    
                    //Load the data of all the Individual and Collective Loots
                    phatLoots.setLoots(0, p.getProperty("IndividualLoots"));
                    phatLoots.setLoots(1, p.getProperty("Coll1"));
                    phatLoots.setLoots(2, p.getProperty("Coll2"));
                    phatLoots.setLoots(3, p.getProperty("Coll3"));
                    phatLoots.setLoots(4, p.getProperty("Coll4"));
                    phatLoots.setLoots(5, p.getProperty("Coll5"));
                    
                    phatLoots.numberCollectiveLoots = Integer.parseInt(p.getProperty("ItemsPerColl"));
                    
                    //Load the data of all the PhatLootsChests
                    phatLoots.setChests(p.getProperty("ChestsData"));
                
                    phatLootsList.add(phatLoots);
                    fis.close();
                }
            }
            
            if (update) {
                System.out.println("[PhatLoots] Updated old .dat file");
                save();
            }
            
            //End loading if at least one Warp was loaded
            if (!phatLootsList.isEmpty())
                return;
            
            System.out.println("[PhatLoots] Loading outdated save files");
            
            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".properties") && !name.equals("config.properties")) {
                    p.load(new FileInputStream(file));
                    
                    PhatLoots phatLoots = new PhatLoots(name.substring(0, name.length() - 11));
                    
                    String[] resetTime = p.getProperty("ResetTime").split("'");
                    if (resetTime[0].equals("never")) {
                        phatLoots.days = -1;
                        phatLoots.hours = -1;
                        phatLoots.minutes = -1;
                        phatLoots.seconds = -1;
                    }
                    else {
                        phatLoots.days = Integer.parseInt(resetTime[0]);
                        phatLoots.hours = Integer.parseInt(resetTime[1]);
                        phatLoots.minutes = Integer.parseInt(resetTime[2]);
                        phatLoots.seconds = Integer.parseInt(resetTime[3]);
                    }
                    
                    String resetType = p.getProperty("ResetType");
                    if (resetType.equals("player"))
                        phatLoots.global = false;
                    else if (resetType.equals("global"))
                        phatLoots.global = true;
                    
                    phatLoots.setOldLoots(0, p.getProperty("IndividualLoots"));
                    phatLoots.setOldLoots(1, p.getProperty("Coll1"));
                    phatLoots.setOldLoots(2, p.getProperty("Coll2"));
                    phatLoots.setOldLoots(3, p.getProperty("Coll3"));
                    phatLoots.setOldLoots(4, p.getProperty("Coll4"));
                    phatLoots.setOldLoots(5, p.getProperty("Coll5"));
                    
                    if (p.containsKey("NumberOfCollectiveLootItemsReceived"))
                        phatLoots.numberCollectiveLoots = Integer.parseInt(p.getProperty("NumberOfCollectiveLootItemsReceived"));
                    else
                        phatLoots.numberCollectiveLoots = Integer.parseInt(p.getProperty("NumberOfCollectiveLootItemsRecieved"));
                    
                    phatLoots.setOldChests(p.getProperty("Chests(RestrictedUsers)"));
                
                    phatLootsList.add(phatLoots);
                }
            }
            
            save();
        }
        catch (Exception loadFailed) {
            save = false;
            System.out.println("[PhatLoots] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }

    /**
     * Saves properties of each PhatLoots
     * Old file is overwritten
     */
    public static void save() {
        //Cancel if saving is turned off
        if (!save) {
            System.out.println("[PhatLoots] Warning! Data is not being saved.");
            return;
        }
        
        try {
            Properties p = new Properties();
            for (PhatLoots phatLoots: phatLootsList) {
                p.setProperty("ResetTime", phatLoots.days+"'"+phatLoots.hours+"'"+phatLoots.minutes+"'"+phatLoots.seconds);
                p.setProperty("GlobalReset", String.valueOf(phatLoots.global));
                p.setProperty("RoundDownTime", String.valueOf(phatLoots.round));
                p.setProperty("MoneyRange", phatLoots.rangeLow+"-"+phatLoots.rangeHigh);
                
                String value = "";
                for (Loot loot: phatLoots.loots[0])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("IndividualLoots", value);
                
                value = "";
                for (Loot loot: phatLoots.loots[1])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("Coll1", value);
                
                value = "";
                for (Loot loot: phatLoots.loots[2])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("Coll2", value);
                
                value = "";
                for (Loot loot: phatLoots.loots[3])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("Coll3", value);
                
                value = "";
                for (Loot loot: phatLoots.loots[4])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("Coll4", value);
                
                value = "";
                for (Loot loot: phatLoots.loots[5])
                    value = value.concat(", "+loot.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("Coll5", value);
                
                p.setProperty("ItemsPerColl", Integer.toString(phatLoots.numberCollectiveLoots));
                
                value = "";
                for (PhatLootsChest chest: phatLoots.chests)
                    value = value.concat("; "+chest.toString());
                if (!value.isEmpty())
                    value = value.substring(2);
                p.setProperty("ChestsData", value);
                
                FileOutputStream fos = new FileOutputStream("plugins/PhatLoots/"+phatLoots.name+".dat");
                
                p.store(new FileOutputStream("plugins/PhatLoots/"+phatLoots.name+".dat"), null);
                fos.close();
            }
        }
        catch (Exception saveFailed) {
            System.err.println("[PhatLoots] Save Failed!");
            saveFailed.printStackTrace();
        }
    }

    /**
     * Returns the PhatLoots that contains the given name
     * 
     * @param name The name of the PhatLoots
     * @return The PhatLootswith the given name or null if not found
     */
    public static PhatLoots findPhatLoots(String name) {
        //Iterate through every PhatLoots to find the one with the given Name
        for (PhatLoots PhatLoots: phatLootsList)
            if (PhatLoots.name.equals(name))
                return PhatLoots;
        
        //Return null because the PhatLoots does not exist
        return null;
    }
    
    /**
     * Returns the PhatLoots that contains the given Block
     * Before returning null, neighboring blocks are checked first
     * 
     * @param chest The Block that is part of the PhatLoots
     * @return The PhatLoots that contains the given Block or null if not found
     */
    public static PhatLoots findPhatLoots(Block block) {
        //Iterate through every PhatLoots to find the one with the given Block
        for (PhatLoots phatLoots: phatLootsList)
            for (PhatLootsChest chest: phatLoots.chests)
                if (chest.isBlock(block))
                    return phatLoots;
        
        //Return null because the PhatLoots does not exist
        return null;
    }
}

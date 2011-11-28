package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.listeners.blockListener;
import com.codisimus.plugins.phatloots.listeners.commandListener;
import com.codisimus.plugins.phatloots.listeners.playerListener;
import com.codisimus.plugins.phatloots.listeners.pluginListener;
import com.griefcraft.lwc.LWC;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Permissions
 *
 * @author Codisimus
 */
public class PhatLootsMain extends JavaPlugin {
    public static PermissionManager permissions;
    public static PluginManager pm;
    public static LWC lwc;
    public static Server server;
    public static int defaultDays;
    public static int defaultHours;
    public static int defaultMinutes;
    public static int defaultSeconds;
    public static int defaultNumberOfLoots;
    public static boolean defaultGlobal;
    public static boolean autoLock;
    public Properties p;
    public static boolean autoLoot;
    public static String autoLootMsg;
    public static boolean displayTimeRemaining;
    public static String timeRemainingMsg;
    public static String canOnlyLootOnceMsg;
    public static Random random = new Random();

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
        checkFiles();
        SaveSystem.load();
        loadConfig();
        registerEvents();
        getCommand("loot").setExecutor(new commandListener());
        System.out.println("PhatLoots "+this.getDescription().getVersion()+" is enabled!");
    }

    /**
     * Makes sure all needed files exist
     *
     */
    public void checkFiles() {
        if (!new File("plugins/PhatLoots/config.properties").exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from PhatLoots.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    public void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/PhatLoots.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/PhatLoots/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            //Copy the file
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/PhatLoots/config.properties"));
        }
        catch (Exception e) {
        }
        Register.economy = loadValue("Economy");
        pluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
        autoLoot = Boolean.parseBoolean(loadValue("AutoLoot"));
        autoLootMsg = format(("AutoLootMessage"));
        displayTimeRemaining = Boolean.parseBoolean(loadValue("DisplayTimeRemaining"));
        timeRemainingMsg = format(loadValue("TimeRemainingMessage"));
        canOnlyLootOnceMsg = format(loadValue("CanOnlyLootOnceMessage"));
        
        //Load default reset time
        String[] resetTime = loadValue("DefaultResetTime").split("'");
        defaultDays = Integer.parseInt(resetTime[0]);
        defaultHours = Integer.parseInt(resetTime[1]);
        defaultMinutes = Integer.parseInt(resetTime[2]);
        defaultSeconds = Integer.parseInt(resetTime[3]);

        //Load default reset type
        String resetType = loadValue("DefaultResetType");
        if (resetType.equals("player"))
            defaultGlobal = false;
        else if (resetType.equals("global"))
            defaultGlobal = true;
        else
            System.err.println("[PhatLoots] '"+resetType+"' is not a valid DefaultResetType");
        
        defaultNumberOfLoots = Integer.parseInt(loadValue("DefaultItemsPerColl"));
        autoLock = Boolean.parseBoolean(loadValue("AutoLockPhatLootChestsWithLWC"));
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
        //Print an error if key is not found
        if (!p.containsKey(key)) {
            System.err.println("[PhatLoots] Missing value for "+key+" in config file");
            System.err.println("[PhatLoots] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }
    
    /**
     * Registers events for the PhatLoots Plugin
     *
     */
    public void registerEvents() {
        playerListener playerListener = new playerListener();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new pluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_DAMAGE, new blockListener(), Priority.Normal, this);
    }
    
    /**
     * Returns boolean value of whether the given player has the specific permission
     * 
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(player, "phatloots."+type);
        
        //Return Bukkit Permission value
        return player.hasPermission("phatloots."+type);
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}

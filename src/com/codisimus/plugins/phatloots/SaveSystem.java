package com.codisimus.plugins.phatloots;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Properties;
import org.bukkit.block.Block;

/**
 * Holds PhatLoots data and is used to load/save data
 *
 * @author Codisimus
 */
public class SaveSystem {
    public static LinkedList<PhatLoots> phatLootsList = new LinkedList<PhatLoots>();
    public static boolean save = true;

    /**
     * Loads properties for each PhatLoots from save files
     * Creates the file if it doesn't exist
     * Disables saving if an error occurs
     */
    public static void load() {
        try {
            File[] files = new File("plugins/PhatLoots").listFiles();
            Properties p = new Properties();
            
            for (File file: files) {
                String name = file.getName();
                if (name.endsWith(".dat")) {
                    p.load(new FileInputStream(file));
                    
                    PhatLoots phatLoots = new PhatLoots(name.substring(0, name.length() - 4));
                    
                    String[] resetTime = p.getProperty("ResetTime").split("'");
                    phatLoots.days = Integer.parseInt(resetTime[0]);
                    phatLoots.hours = Integer.parseInt(resetTime[1]);
                    phatLoots.minutes = Integer.parseInt(resetTime[2]);
                    phatLoots.seconds = Integer.parseInt(resetTime[3]);
                    
                    String resetType = p.getProperty("ResetType");
                    if (resetType.equals("player"))
                        phatLoots.global = false;
                    else if (resetType.equals("global"))
                        phatLoots.global = true;
                    
                    phatLoots.setLoots(0, p.getProperty("IndividualLoots"));
                    phatLoots.setLoots(1, p.getProperty("Coll1"));
                    phatLoots.setLoots(2, p.getProperty("Coll2"));
                    phatLoots.setLoots(3, p.getProperty("Coll3"));
                    phatLoots.setLoots(4, p.getProperty("Coll4"));
                    phatLoots.setLoots(5, p.getProperty("Coll5"));
                    
                    phatLoots.numberCollectiveLoots = Integer.parseInt(p.getProperty("ItemsPerColl"));
                    
                    phatLoots.setChests(p.getProperty("ChestsData"));
                
                    phatLootsList.add(phatLoots);
                }
            }
            
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
                    
                    phatLoots.numberCollectiveLoots = Integer.parseInt(p.getProperty("NumberOfCollectiveLootItemsReceived"));
                    
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
                
                if (phatLoots.global)
                    p.setProperty("ResetType", "global");
                else
                    p.setProperty("ResetType", "player");
                
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
                
                p.store(new FileOutputStream("plugins/PhatLoots/"+phatLoots.name+".dat"), null);
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
        for (PhatLoots PhatLoots : phatLootsList)
            if (PhatLoots.name.equals(name))
                return PhatLoots;
        
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
        for (PhatLoots phatLoots : phatLootsList)
            for (PhatLootsChest chest: phatLoots.chests)
                if (chest.isBlock(block))
                    return phatLoots;
        
        return null;
    }
    
    /**
     * Removes the PhatLoots from the LinkedList of saved PhatLoots
     * 
     * @param phatLoots The PhatLoots to be removed
     */
    public static void removePhatLoots(PhatLoots phatLoots){
        phatLootsList.remove(phatLoots);
        File trash = new File("plugins/PhatLoots/"+phatLoots.name+".dat");
        trash.delete();
    }
}

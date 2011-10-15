
package PhatLoots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Properties;
import org.bukkit.block.Block;

/**
 *
 * @author Codisimus
 */
public class SaveSystem {
    private static LinkedList<PhatLoots> phatLootsList = new LinkedList<PhatLoots>();
    private static boolean save = true;

    /**
     * Loads properties for each PhatLoots from save files
     * Creates the file if it doesn't exist
     * Disables saving if an error occurs
     */
    protected static void loadFromFile() {
        BufferedReader bReader = null;
        try {
            new File("plugins/PhatLoots").mkdir();
            new File("plugins/PhatLoots/phatloots.save").createNewFile();
            bReader = new BufferedReader(new FileReader("plugins/PhatLoots/phatloots.save"));
            String line = "";
            while ((line = bReader.readLine()) != null) {
                PhatLoots phatLoot = new PhatLoots(line);
                phatLootsList.add(phatLoot);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (PhatLoots phatLoot : phatLootsList) {
            Properties p = new Properties();
            File file = new File("plugins/PhatLoots/"+phatLoot.name+".properties");
            try {
                if (file.createNewFile())
                    try {
                        p.setProperty("ResetTime", phatLoot.resetTime);
                        p.setProperty("ResetType", phatLoot.resetType);
                        p.setProperty("IndividualLoots", phatLoot.individualLoots);
                        p.setProperty("Coll1", phatLoot.getCollectiveLoots(1));
                        p.setProperty("Coll2", phatLoot.getCollectiveLoots(2));
                        p.setProperty("Coll3", phatLoot.getCollectiveLoots(3));
                        p.setProperty("Coll4", phatLoot.getCollectiveLoots(4));
                        p.setProperty("Coll5", phatLoot.getCollectiveLoots(5));
                        p.setProperty("NumberOfCollectiveLootItemsReceived", Integer.toString(phatLoot.numberCollectiveLoots));
                        p.setProperty("Chests(RestrictedUsers)", phatLoot.restrictedUsers);
                        p.store(new FileOutputStream("plugins/PhatLoots/"+phatLoot.name+".properties"), null);
                    }
                    catch (Exception e) {
                        save = false;
                        System.out.println("[PhatLoots] Load failed, saving turned off to prevent loss of data");
                        e.printStackTrace();
                    }
            }
            catch (Exception e) {
            }
            try {
                p.load(new FileInputStream("plugins/PhatLoots/"+phatLoot.name+".properties"));
                phatLoot.resetTime = p.getProperty("ResetTime");
                phatLoot.resetType = p.getProperty("ResetType");
                phatLoot.individualLoots = p.getProperty("IndividualLoots");
                phatLoot.setCollectiveLoots(1, p.getProperty("Coll1"));
                phatLoot.setCollectiveLoots(2, p.getProperty("Coll2"));
                phatLoot.setCollectiveLoots(3, p.getProperty("Coll3"));
                phatLoot.setCollectiveLoots(4, p.getProperty("Coll4"));
                phatLoot.setCollectiveLoots(5, p.getProperty("Coll5"));
                try {
                    phatLoot.numberCollectiveLoots = Integer.parseInt(p.getProperty("NumberOfCollectiveLootItemsReceived"));
                }
                catch (Exception iCantSpell) {
                    phatLoot.numberCollectiveLoots = Integer.parseInt(p.getProperty("NumberOfCollectiveLootItemsRecieved"));
                }
                phatLoot.restrictedUsers = p.getProperty("Chests(RestrictedUsers)");
            }
            catch (Exception ex) {
                save = false;
                System.out.println("[PhatLoots] Load failed, saving turned off to prevent loss of data");
                ex.printStackTrace();
            }

        }
    }

    /**
     * Saves properties of each PhatLoots
     * Old file is overwritten
     */
    protected static void save() {
        //cancels if saving is turned off
        if (!save)
            return;
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new FileWriter("plugins/PhatLoots/phatloots.save"));
            for(PhatLoots phatLoot : phatLootsList) {
                bWriter.write(phatLoot.name);
                bWriter.newLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                bWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (PhatLoots phatLoot : phatLootsList) {
            Properties p = new Properties();
            try {
                p.setProperty("ResetTime", phatLoot.resetTime);
                p.setProperty("ResetType", phatLoot.resetType);
                p.setProperty("IndividualLoots", phatLoot.individualLoots);
                p.setProperty("Coll1", phatLoot.getCollectiveLoots(1));
                p.setProperty("Coll2", phatLoot.getCollectiveLoots(2));
                p.setProperty("Coll3", phatLoot.getCollectiveLoots(3));
                p.setProperty("Coll4", phatLoot.getCollectiveLoots(4));
                p.setProperty("Coll5", phatLoot.getCollectiveLoots(5));
                p.setProperty("NumberOfCollectiveLootItemsReceived", Integer.toString(phatLoot.numberCollectiveLoots));
                p.setProperty("Chests(RestrictedUsers)", phatLoot.restrictedUsers);
                p.store(new FileOutputStream("plugins/PhatLoots/"+phatLoot.name+".properties"), null);
            }
            catch (Exception e) {
            }
        }
    }
    
    /**
     * Returns the LinkedList of saved PhatLoots
     * 
     * @return the LinkedList of saved PhatLoots
     */
    public static LinkedList<PhatLoots> getPhatLootsList() {
        return phatLootsList;
    }

    /**
     * Returns the PhatLoots that contains the given name
     * 
     * @param name The name of the PhatLoots
     * @return The PhatLootswith the given name or null if not found
     */
    public static PhatLoots findPhatLoots(String name) {
        for(PhatLoots PhatLoots : phatLootsList)
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
    public static PhatLoots findPhatLoots(Block chest) {
        String block = chest.getWorld().getName()+","+chest.getX();
        block = block.concat(","+chest.getY()+","+chest.getZ()+",");
        for(PhatLoots phatLoots : phatLootsList)
            if (phatLoots.restrictedUsers.contains(block))
                return phatLoots;
        return null;
    }

    /**
     * Adds the PhatLoots to the LinkedList of saved PhatLoots
     * 
     * @param phatLoots The PhatLoots to be added
     */
    protected static void addPhatLoots(PhatLoots phatLoots) {
        phatLootsList.add(phatLoots);
    }

    /**
     * Removes the PhatLoots from the LinkedList of saved PhatLoots
     * 
     * @param phatLoots The PhatLoots to be removed
     */
    protected static void removePhatLoots(PhatLoots phatLoots){
        phatLootsList.remove(phatLoots);
        File trash = new File("plugins/PhatLoots/"+phatLoots.name+".properties");
        trash.delete();
    }
}

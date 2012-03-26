package com.codisimus.plugins.phatloots;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * A PhatLoot is a reward made up of money and items
 * 
 * @author Codisimus
 */
public class PhatLoot {
    public String name; //A unique name for the Warp
    public int numberCollectiveLoots = PhatLoots.defaultNumberOfLoots; //Amount of loots received from each collective loot
    
    public int moneyLower; //Range of money that may be given
    public int moneyUpper;
    
    public int expLower; //Range of experience gained when looting
    public int expUpper;
    
    public LinkedList<String> commands = new LinkedList<String>(); //Commands that will be run upon looting the Chest
    
    public LinkedList<Loot>[] loots = (LinkedList<Loot>[])new LinkedList[6]; //List of items that may be given

    public int days = PhatLoots.defaultDays; //Reset time (will never reset if any are negative) 
    public int hours = PhatLoots.defaultHours;
    public int minutes = PhatLoots.defaultMinutes;
    public int seconds = PhatLoots.defaultSeconds;

    public boolean global = PhatLoots.defaultGlobal; //Reset Type
    public boolean round = PhatLoots.defaultRound;
    
    public LinkedList<PhatLootChest> chests = new LinkedList<PhatLootChest>(); //List of PhatLootChests that activate the Warp
    LinkedList<String> oldChests = new LinkedList<String>(); //List of PhatLootChests from unloaded Worlds
    
    PhatLootsCommandSender cs = new PhatLootsCommandSender();

    /**
     * Constructs a new PhatLoot
     * 
     * @param name The name of the PhatLoot which will be created
     */
    public PhatLoot (String name) {
        this.name = name;
        
        //Initialize lits of loots
        for (int i = 0; i < 6; i++)
            loots[i] = new LinkedList<Loot>();
    }

    /**
     * Activates the PhatLoot by checking for remaining time and receiving loots
     * 
     * @param player The Player who is looting
     * @param block The Block being looted
     */
    public void getLoot(Player player, PhatLootChest chest) {
        //Get the user to be looked up for last time of use
        String user = player.getName();
        if (global)
            user = "global";
        
        //Find out how much time remains
        String timeRemaining = getTimeRemaining(chest.getTime(user));
        
        //User can never loot the Chest again if timeRemaining is null
        if (timeRemaining == null)
            return;
        
        //Display remaining time if it is not 
        if (!timeRemaining.equals("0")) {
            if (PhatLoots.displayTimeRemaining)
                player.sendMessage(PhatLootsMessages.timeRemaining.replaceAll("<time>", timeRemaining));
            
            return;
        }
        
        //Roll for money amount if the range is above 0
        if (moneyUpper > 0) {
            int amount = PhatLoots.random.nextInt((moneyUpper + 1) - moneyLower);
            amount = amount + moneyLower;
            
            //Give money to the Player if there is money to give
            if (amount > 0) {
                String money = Econ.reward(player.getName(), amount);
                player.sendMessage(money+" added to your account!");
            }
        }
        
        //Roll for exp amount if the range is above 0
        if (expUpper > 0) {
            int amount = PhatLoots.random.nextInt((expUpper + 1) - expLower);
            amount = amount + expLower;
            
            //Give exp to the Player if there is exp to give
            if (amount > 0) {
                player.giveExp(amount);
                player.sendMessage("You gained "+amount+" experience from looting the Chest");
            }
        }
        
        //Execute each command
        for (String cmd: commands)
            PhatLoots.server.dispatchCommand(cs, cmd.replace("<player>", player.getName()));
        
        //Give individual loots
        lootIndividual(player, chest);
        
        //Give collective loots
        lootCollective(player, chest);
        
        //Set the new time for the User and return true
        setTime(chest, user);
    }

    /**
     * Returns the remaining time until the PhatLootChest resets
     * Returns null if the PhatLootChest never resets
     * 
     * @param time The given time
     * @return the remaining time until the PhatLootChest resets
     */
    public String getTimeRemaining(int[] time) {
        //Return 0 if a time was not given
        if (time == null)
            return "0";
        
        //Return null if the reset time is set to never
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0)
            return null;
        
        //Calculate the time that the Warp will reset
        int resetYear = time[0];
        int resetDay = time[1] + days;
        int resetHour = time[2] + hours;
        int resetMinute = time[3] + minutes;
        int resetSecond = time[4] + seconds;
        
        //Update time values into the correct format
        while (resetSecond >= 60) {
            resetMinute++;
            resetSecond = resetSecond - 60;
        }
        while (resetMinute >= 60) {
            resetHour++;
            resetMinute = resetMinute - 60;
        }
        while (resetHour >= 24) {
            resetDay++;
            resetHour = resetHour - 24;
        }
        while (resetDay >= 366) {
            resetYear++;
            resetDay = resetDay - 365;
        }
        
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        
        String msg = "";
        
        //Return 0 if the current time is later than the reset time
        if (year > resetYear)
            return "0";
        
        if (year < resetYear) {
            msg = msg.concat((resetDay - day - 1)+" years, ");
            resetDay = resetDay + 365;
        }
        
        if (day > resetDay)
            return "0";
        
        if (day < resetDay) {
            msg = msg.concat((resetDay - day - 1)+" days, ");
            resetHour = resetHour + 24;
        }
        
        if (hour > resetHour)
            return "0";
        
        if (hour < resetHour) {
            msg = msg.concat((resetHour - hour - 1)+" hours, ");
            resetMinute = resetMinute + 60;
        }
        
        if (minute > resetMinute)
            return "0";
        
        if (minute < resetMinute) {
            msg = msg.concat((resetMinute - minute - 1)+" minutes, ");
            resetSecond = resetSecond + 60;
        }
        
        if (second >= resetSecond)
            return "0";
        
        return msg.concat((resetSecond - second)+" seconds");
    }
    
    /**
     * Fills the Chest (Block) with loot
     * Each item is rolled for to determine if it will by added to the Chest
     * Money is rolled for to determine how much will be given within the range
     * 
     * @param player The Player looting
     * @param block The Block being looted
     */
    public void lootIndividual(Player player, PhatLootChest phatLootChest) {
        for (Loot loot: loots[0])
            //Roll for item
            if (PhatLoots.random.nextInt(100) + PhatLoots.random.nextDouble() < loot.getProbability())
                lootItem(loot.getItem(), player, phatLootChest);
    }

    /**
     * Fills the Chest (Block) with loot
     * Items are rolled for in order until the maximum number is added to the Chest
     * 
     * @param collectiveLoots The String that contains the items and percentages
     * @param block The Block being looted
     */
    public void lootCollective(Player player, PhatLootChest phatLootChest) {
        //Loot from each of the 5 collective loots
        for (int i = 1 ; i <= 5; i++) {
            
            //Make sure there are items that will be looted before entering the loop
            if (!loots[i].isEmpty()) {
                //Do not loot if the probability does not add up to 100
                if (getPercentRemaining(i) != 0)
                    player.sendMessage("Cannot loot Coll"+i+" because the probability does not equal 100%");
                else {
                    //Create an array of 100 Loots
                    Loot[] collLoots = new Loot[100];
                    int j = 0;

                    //Add each loot to the array of Loots
                    for (Loot loot: loots[i])
                        //The amount of times the Loot is added is determined by the probability
                        for (int k = 0; k < loot.getProbability(); k++) {
                            collLoots[j] = loot;
                            j++;
                        }
                    
                    //Loot the specified number of items
                    for (int numberLooted = 0; numberLooted < numberCollectiveLoots; numberLooted++)
                        //Generate a random int to determine the index of the array that holds the Loot
                        lootItem(collLoots[PhatLoots.random.nextInt(100)].getItem(), player, phatLootChest);
                }
            }
                
        }
    }

    /**
     * Fills the Chest (Block) with loot
     * Items are rolled for in order until the maximum number is added to the Chest
     * 
     * @param collectiveLoots The String that contains the items and percentages
     * @param block The Block being looted
     */
    public void lootItem(ItemStack item, Player player, PhatLootChest phatLootChest) {
        //Make sure loots do not excede the stack size
        if (item.getAmount() > item.getMaxStackSize()) {
            int id = item.getTypeId();
            short durability = item.getDurability();
            byte data = item.getData().getData();
            
            lootItem(new ItemStack(id, item.getMaxStackSize(), durability, data), player, phatLootChest);
            lootItem(new ItemStack(id, item.getAmount() - item.getMaxStackSize(), durability, data), player, phatLootChest);
        }
        
        PlayerInventory sack = player.getInventory();

        if (phatLootChest.isDispenser) {
            //Add the item to the Dispenser inventory
            Dispenser dispenser = phatLootChest.getDispenser();
            phatLootChest.clear();
            phatLootChest.inventory.addItem(item);

            //Dispense until the Dispenser is empty
            while (phatLootChest.inventory.firstEmpty() > 0)
                dispenser.dispense();
        }
        else if (PhatLoots.autoLoot && sack.firstEmpty() != -1) {
            //Add the Loot to the Player's Inventory
            player.sendMessage(PhatLootsMessages.autoLoot.replaceAll("<item>", item.getType().name()));
            sack.addItem(item);
        }
        else {
            //Add the Loot to the Chest's Inventory
            if (phatLootChest.inventory.firstEmpty() != -1)
                phatLootChest.inventory.addItem(item);
            else
                phatLootChest.overFlow(item, player);
        }
    }
    
    /**
     * Updates the Player's time value in the Map with the current time
     * The time is saved as an array with DAY, HOUR, MINUTE, SECOND
     * 
     * @param player The Player whose time is to be updated
     */
    public void setTime(PhatLootChest chest, String player) {
        int[] time = new int[5];
        Calendar calendar = Calendar.getInstance();
        
        if (round) {
            if (seconds != 0) {
                time[4] = calendar.get(Calendar.SECOND);
                time[3] = calendar.get(Calendar.MINUTE);
                time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            }
            else if (minutes != 0) {
                time[3] = calendar.get(Calendar.MINUTE);
                time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            }
            else if (hours != 0)
                time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            
            time[1] = calendar.get(Calendar.DAY_OF_YEAR);
            time[0] = calendar.get(Calendar.YEAR);
        }
        else {
            time[0] = calendar.get(Calendar.YEAR);
            time[1] = calendar.get(Calendar.DAY_OF_YEAR);
            time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            time[3] = calendar.get(Calendar.MINUTE);
            time[4] = calendar.get(Calendar.SECOND);
        }
        
        chest.users.put(player, time);
    }
    
    /**
     * Returns the Remaining Percent of the given collective Loots
     * 
     * @param id The id of the collective Loots
     * @return Total probability of all Loots in the collective Loots subtracted from 100
     */
    public double getPercentRemaining(int id) {
        //Subtract the probabilty of each loot from 100
        double total = 100;
        for (Loot loot: loots[id])
            total = total - loot.getProbability();
        
        return total;
    }

    /**
     * Loads data from the save file
     * 
     * @param id The id of the Loots (0 for individual loots)
     * @param string The data of the Loots
     */
    public void setLoots(int id, String data) {
        //Cancel if no data was provided
        if (data.isEmpty())
            return;
        
        while (data.endsWith(",") || data.endsWith(" "))
            data = data.substring(0, data.length() - 1);
        
        //Load data for each loot
        for (String loot: data.split(", "))
            try {
                //Construct a new loot with the item data and probability
                String[] lootData = loot.split("'");
                int lower = PhatLootsCommand.getLowerBound(null, lootData[2]);
                int upper = PhatLootsCommand.getUpperBound(null, lootData[2]);
                
                if (lower == -1 || upper == -1)
                    throw new Exception();
                
                Map<Enchantment, Integer> enchantments = PhatLootsCommand.getEnchantments(null, ":"+lootData[1]);
                
                if (enchantments == null)
                    loots[id].add(new Loot(Integer.parseInt(lootData[0]), Short.parseShort(lootData[1]),
                            lower, upper, Double.parseDouble(lootData[3])));
                else
                    loots[id].add(new Loot(Integer.parseInt(lootData[0]), enchantments,
                            lower, upper, Double.parseDouble(lootData[3])));
            }
            catch (Exception invalidLoot) {
                System.out.println("[PhatLoots] Error occured while loading PhatLoots "+'"'+name+'"'+", "+'"'+loot+'"'+" is not a valid Loot");
                invalidLoot.printStackTrace();
            }
    }
    
    /**
     * Loads data from the save file
     * 
     * @param string The data of the Chests
     */
    public void setChests(String data) {
        //Cancel if no data was provided
        if (data.isEmpty())
            return;
        
        int index;
        
        //Load data for each PhatLootChest
        for (String chest: data.split("; "))
            try {
                String[] chestData = chest.split("\\{", 2);

                //Load the Block Location data of the Chest
                String[] blockData = chestData[0].split("'");
                
                //Check if the World is not loaded
                if (PhatLoots.server.getWorld(blockData[0]) == null) {
                    oldChests.add(chest);
                    continue;
                }
                
                //Construct a a new PhatLootChest with the Location data
                PhatLootChest phatLootChest = new PhatLootChest(blockData[0], Integer.parseInt(blockData[1]),
                        Integer.parseInt(blockData[2]), Integer.parseInt(blockData[3]));

                //Load the HashMap of Users of the Chest
                for (String user: chestData[1].substring(0, chestData[1].length() - 1).split(", "))
                    //Don't load if the data if it is corrupt or empty
                    if ((index = user.indexOf('@')) != -1) {
                        String[] timeData = user.substring(index + 1).split("'");
                        int[] time = new int[5];

                        if (timeData.length == 4) {
                            time[0] = 2011;
                            for (int j = 0; j < 4; j++)
                                time[j+1] = Integer.parseInt(timeData[j]);
                        }
                        else
                            for (int j = 0; j < 5; j++)
                                time[j] = Integer.parseInt(timeData[j]);

                        phatLootChest.users.put(user.substring(0, index), time);
                    }

                chests.add(phatLootChest);
            }
            catch (Exception invalidChest) {
                System.out.println("[PhatLoots] Error occured while loading PhatLoot "+
                        '"'+name+'"'+", "+'"'+chest+'"'+" is not a valid PhatLootChest");
                invalidChest.printStackTrace();
            }
    }
    
    /**
     * Loads data from the outdated save file
     * 
     * @param id The id of the Loots (0 for individual loots)
     * @param string The data of the Loots
     */
    public void setOldLoots(int id, String data) {
        //Cancel if no data was provided
        if (data.isEmpty())
            return;
        
        //Load data for each loot
        for (String loot: data.split("~")) {
            try {
                String[] lootData = loot.split(",");
                
                if (lootData[0].equals("money")) {
                    //Set the money range
                    moneyLower = Integer.parseInt(lootData[1]);
                    moneyUpper = Integer.parseInt(lootData[2]);
                }
                else {
                    int itemID;
                    Short durability = -1;
                    if (lootData[0].contains(".")) {
                        String[] itemData = lootData[0].split(".");
                        itemID = Integer.parseInt(itemData[0]);
                        durability = Short.parseShort(itemData[1]);
                    }
                    else
                        itemID = Integer.parseInt(lootData[0]);

                    Loot lootObject = new Loot(itemID, durability, Integer.parseInt(lootData[1]), Integer.parseInt(lootData[1]), Double.parseDouble(lootData[2]));
                    loots[id].add(lootObject);
                }
            }
            catch (Exception invalidLoot) {
                System.out.println("[PhatLoots] Error occured while loading PhatLoot "+'"'+name+'"'+", "+'"'+loot+'"'+" is not a valid Loot");
                invalidLoot.printStackTrace();
            }
        }
    }
    
    /**
     * Loads data from the outdated save file
     * 
     * @param string The data of the Chests
     */
    public void setOldChests(String data) {
        //Cancel if no data was provided
        if (data.isEmpty())
            return;
        
        //Load data for each PhatLootChest
        for (String chest: data.split("~")) {
            try {
                String[] chestData = chest.split(",");
                
                //Construct a a new PhatLootChest with the Location data
                PhatLootChest phatLootChest = new PhatLootChest(chestData[0], Integer.parseInt(chestData[1]),
                        Integer.parseInt(chestData[2]), Integer.parseInt(chestData[3]));
                
                //Load the HashMap of Users of the Chest
                for (int i = 4; i < chestData.length; i = i + 2) {
                    String[] timeData = chestData[i+1].split("'");
                    int[] time = new int[4];

                    for (int j = 0; j < 4; j++)
                        time[j] = Integer.parseInt(timeData[j]);

                    phatLootChest.users.put(chestData[i], time);
                }
                
                chests.add(phatLootChest);
            }
            catch (Exception invalidChest) {
                System.out.println("[PhatLoots] Error occured while loading PhatLoot "+
                        '"'+name+'"'+", "+'"'+chest+'"'+" is not a valid PhatLootChest");
                invalidChest.printStackTrace();
            }
        }
    }
    
    /**
     * Returns the List of Loots as a String
     * 
     * @param id The id of the Loots
     * @return The List of Loots as a String
     */
    public String getLoots(int id) {
        String list = "";
        
        //Concat each Loot onto the list
        for (Loot loot: loots[id])
            list = list.concat(loot.toInfoString());
        
        if (!list.isEmpty())
            list = list.substring(2);
        
        return list;
    }
    
    /**
     * Returns the Button that is associated with the given Block
     * 
     * @param block The given Block
     * @return The Button that is associated with the given Block
     */
    public PhatLootChest findChest(Block block) {
        //Iterate through chests to find the PhatLootChest of the given Block
        for (PhatLootChest chest: chests)
            if (chest.isBlock(block))
                return chest;
        
        //Return null because the Button does not exist
        return null;
    }

    /**
     * Resets the user times for all PhatLootChests of this PhatLoot
     * If a Block is given then only reset that PhatLootChest
     * 
     * @param block The given Block
     */
    public void reset(Block block) {
        if (block == null)
            //Reset all PhatLootChests
            for (PhatLootChest phatLootChest: chests)
                phatLootChest.users.clear();
        else
            //Find the PhatLootChest of the given Block and reset it
            for (PhatLootChest phatLootChest: chests)
                if (phatLootChest.isBlock(block))
                    phatLootChest.users.clear();
        
        save();
    }
    
    /**
     * Writes the PhatLoot data to file
     * 
     */
    public void save() {
        PhatLoots.savePhatLoot(this);
    }
}
package com.codisimus.plugins.phatloots;

import java.util.*;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A PhatLoot is a reward made up of money and items
 *
 * @author Codisimus
 */
public class PhatLoot {
    static boolean onlyDropOnPlayerKill;
    static boolean replaceMobLoot;

    private static PhatLootsCommandSender cs = new PhatLootsCommandSender();

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

    LinkedList<PhatLootChest> chests = new LinkedList<PhatLootChest>(); //List of PhatLootChests that activate the Warp
    LinkedList<String> oldChests = new LinkedList<String>(); //List of PhatLootChests from unloaded Worlds

    Properties lootTimes = new Properties(); //PhatLootChest'PlayerName=Year'Day'Hour'Minute'Second

    /**
     * Constructs a new PhatLoot
     *
     * @param name The name of the PhatLoot which will be created
     */
    public PhatLoot (String name) {
        this.name = name;

        //Initialize lits of loots
        for (int i = 0; i < 6; i++) {
            loots[i] = new LinkedList<Loot>();
        }
    }

    /**
     * Activates the PhatLoot by checking for remaining time and receiving loots
     *
     * @param player The Player who is looting
     * @param block The Block being looted
     */
    public void getLoot(Player player, PhatLootChest chest, Inventory inventory) {
        //Get the user to be looked up for last time of use
        String user = player.getName();
        if (global) {
            user = "global";
        }

        //Find out how much time remains
        String timeRemaining = getTimeRemaining(getTime(chest, user));

        //User can never loot the Chest again if timeRemaining is null
        if (timeRemaining == null) {
            return;
        }

        //Display remaining time if it is not
        if (!timeRemaining.equals("0")) {
            if (PhatLoots.displayTimeRemaining) {
                player.sendMessage(PhatLootsMessages.timeRemaining.replace("<time>", timeRemaining));
            }

            return;
        }

        //Reset(Clear) the Inventory
        inventory.clear();

        //Roll for money amount if the range is above 0
        if (moneyUpper > 0) {
            int amount = PhatLoots.random.nextInt((moneyUpper + 1) - moneyLower);
            amount = amount + moneyLower;

            //Give money to the Player if there is money to give
            if (amount > 0) {
                String money = Econ.reward(player.getName(), amount);
                player.sendMessage(PhatLootsMessages.moneyLooted
                        .replace("<amount>", money));
            }
        }

        //Roll for exp amount if the range is above 0
        if (expUpper > 0) {
            int amount = PhatLoots.random.nextInt((expUpper + 1) - expLower);
            amount = amount + expLower;

            //Give exp to the Player if there is exp to give
            if (amount > 0) {
                player.giveExp(amount);
                player.sendMessage(PhatLootsMessages.experienceLooted
                        .replace("<amount>", String.valueOf(amount)));
            }
        }

        //Execute each command
        for (String cmd: commands) {
            PhatLoots.server.dispatchCommand(cs, cmd.replace("<player>", player.getName()));
        }

        //Give individual loots
        boolean itemsInChest = chest.addLoots(lootIndividual(), player, inventory);

        //Give collective loots
        if (chest.addLoots(lootCollective(), player, inventory)) {
            itemsInChest = true;
        }

        //Update the Inventory View
        if (!chest.isDispenser) {
            player.updateInventory();
        }

        if (PhatLoots.autoLoot && !itemsInChest) {
            player.closeInventory();
            PhatLoots.closeInventory(player, inventory, chest.getBlock().getLocation(), global);
        }

        //Set the new time for the User and return true
        setTime(chest, user);
    }

    public int getLoot(Player player, List<ItemStack> drops) {
        if (onlyDropOnPlayerKill && player == null) {
            drops.clear();
            return 0;
        }
        if (replaceMobLoot) {
            drops.clear();
        }

        List<ItemStack> loot = lootIndividual();
        loot.addAll(lootCollective());
        if (player != null && !PhatLootsMessages.mobDropped.isEmpty()) {
            for (ItemStack item : loot) {
                player.sendMessage(PhatLootsMessages.mobDropped.replace("<item>", getItemName(item)));
            }
        }
        drops.addAll(loot);

        //Roll for money amount if the range is above 0
        if (moneyUpper > 0 && player != null) {
            int amount = PhatLoots.random.nextInt((moneyUpper + 1) - moneyLower);
            amount = amount + moneyLower;

            //Give money to the Player if there is money to give
            if (amount > 0 && !player.getGameMode().equals(GameMode.CREATIVE)
                    && PhatLoots.hasPermission(player, "moneyfrommobs")) {
                String money = Econ.reward(player.getName(), amount);
                player.sendMessage(money + " added to your account!");
            }
        }

        //Execute each command
        for (String cmd: commands) {
            PhatLoots.server.dispatchCommand(cs, cmd.replace("<player>", player.getName()));
        }

        //Roll for exp amount if the range is above 0
        if (expUpper > 0) {
            int amount = PhatLoots.random.nextInt((expUpper + 1) - expLower);
            return amount + expLower;
        }

        return 0;
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
        if (time == null) {
            return "0";
        }

        //Return null if the reset time is set to never
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
            return null;
        }

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
        if (year > resetYear) {
            return "0";
        }

        if (year < resetYear) {
            msg = msg.concat((resetYear - year - 1) + " years, ");
            resetDay = resetDay + 365;
        }

        if (day > resetDay) {
            return "0";
        }

        if (day < resetDay) {
            msg = msg.concat((resetDay - day - 1) + " days, ");
            resetHour = resetHour + 24;
        }

        if (hour > resetHour) {
            return "0";
        }

        if (hour < resetHour) {
            msg = msg.concat((resetHour - hour - 1) + " hours, ");
            resetMinute = resetMinute + 60;
        }

        if (minute > resetMinute) {
            return "0";
        }

        if (minute < resetMinute) {
            msg = msg.concat((resetMinute - minute - 1) + " minutes, ");
            resetSecond = resetSecond + 60;
        }

        if (second >= resetSecond) {
            return "0";
        }

        return msg.concat((resetSecond - second) + " seconds");
    }

    /**
     * Fills the Chest (Block) with loot
     * Each item is rolled for to determine if it will by added to the Chest
     * Money is rolled for to determine how much will be given within the range
     */
    public List<ItemStack> lootIndividual() {
        List<ItemStack> itemList = new LinkedList<ItemStack>();
        for (Loot loot: loots[0]) {
            //Roll for item
            if (PhatLoots.random.nextInt(100) + PhatLoots.random.nextDouble()
                    < loot.getProbability()) {
                itemList.add(loot.getItem());
            }
        }
        return itemList;
    }

    /**
     * Fills the Chest (Block) with loot
     * Items are rolled for in order until the maximum number is added to the Chest
     */
    public List<ItemStack> lootCollective() {
        List<ItemStack> itemList = new LinkedList<ItemStack>();

        //Loot from each of the 5 collective loots
        for (int i = 1; i <= 5; i++) {
            //Make sure there are items that will be looted before entering the loop
            if (!loots[i].isEmpty()) {
                //Do not loot if the probability does not add up to 100
                if (getPercentRemaining(i) != 0) {
                    PhatLoots.logger.warning("Cannot loot Coll" + i + " of "
                            + name + " because the probability does not equal 100%");
                } else {
                    //Create an array of 100 Loots
                    Loot[] collLoots = new Loot[100];
                    int j = 0;

                    //Add each loot to the array of Loots
                    for (Loot loot: loots[i]) {
                        //The amount of times the Loot is added is determined by the probability
                        for (int k = 0; k < loot.getProbability(); k++) {
                            try {
                                collLoots[j] = loot;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                PhatLoots.logger.warning("Cannot loot Coll" + i
                                        + " of " + name + " because the probability does not equal 100%");
                            }
                            j++;
                        }
                    }

                    if (j < 100) {
                        PhatLoots.logger.warning("Cannot loot Coll" + i + " of "
                                + name + " because the probability does not equal 100%");
                    }

                    //Loot the specified number of items
                    for (int numberLooted = 0; numberLooted < numberCollectiveLoots; numberLooted++) {
                        //Generate a random int to determine the index of the array that holds the Loot
                        itemList.add(collLoots[PhatLoots.random.nextInt(100)].getItem());
                    }
                }
            }
        }

        return itemList;
    }

    /**
     * Updates the Player's time value in the Map with the current time
     * The time is saved as an array with YEAR, DAY, HOUR, MINUTE, SECOND
     *
     * @param chest The PhatLootChest to set the time for
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
            } else if (minutes != 0) {
                time[3] = calendar.get(Calendar.MINUTE);
                time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            } else if (hours != 0) {
                time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            }

            time[1] = calendar.get(Calendar.DAY_OF_YEAR);
            time[0] = calendar.get(Calendar.YEAR);
        } else {
            time[0] = calendar.get(Calendar.YEAR);
            time[1] = calendar.get(Calendar.DAY_OF_YEAR);
            time[2] = calendar.get(Calendar.HOUR_OF_DAY);
            time[3] = calendar.get(Calendar.MINUTE);
            time[4] = calendar.get(Calendar.SECOND);
        }

        String timeString = time[0] + "'" + time[1] + "'" + time[2]
                            + "'" + time[3] + "'" + time[4];
        lootTimes.setProperty(chest.toString() + "'" + player, timeString);
    }

    /**
     * Retrieves the time for the given Player
     *
     * @param chest The PhatLootChest to set the time for
     * @param player The Player whose time is requested
     * @return The time as an array of ints
     */
    public int[] getTime(PhatLootChest chest, String player) {
        int[] time = new int[5];
        String key = chest.toString() + "'" + player;

        String string = lootTimes.getProperty(key);
        if (string == null) {
            return null;
        }

        String[] timeString = string.split("'");
        for (int i = 0; i < 5; i++) {
            try {
                time[i] = Integer.parseInt(timeString[i]);
            } catch (Exception corruptData) {
                PhatLoots.logger.severe("Fixed corrupted time value!");
            }
        }

        return time;
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
        for (Loot loot: loots[id]) {
            total = total - loot.getProbability();
        }

        return total;
    }

    /**
     * Loads data from the save file
     *
     * @param id The id of the Loots (0 for individual loots)
     * @param string The data of the Loots
     */
    public void setLoots(int id, String lootsString) {
        //Cancel if no data was provided
        if (lootsString.isEmpty()) {
            return;
        }

        while (lootsString.endsWith(",") || lootsString.endsWith(" ")) {
            lootsString = lootsString.substring(0, lootsString.length() - 1);
        }

        //Load data for each loot
        for (String lootString: lootsString.split(", ")) {
            try {
                String[] lootData = lootString.split("'");

                String item = lootData[0];
                int itemID;
                //Check for Dyed Color
                Color color = null;
                if (item.startsWith("(")) {
                    color = Color.fromRGB(Integer.parseInt(item.substring(1, 8)));
                    item = item.substring(9);
                }
                //Check for Name of Item Description
                if (item.contains("+")) {
                    int index = item.indexOf('+');
                    itemID = Integer.parseInt(item.substring(0, index));
                    item = item.substring(index + 1);
                } else {
                    itemID = Integer.parseInt(item);
                    item = "";
                }

                String data = lootData[1];
                Map<Enchantment, Integer> enchantments = null;
                //Check for Enchantments
                if (data.contains("+")) {
                    int index = data.indexOf('+');
                    enchantments = PhatLootsCommand.getEnchantments(
                            data.substring(index + 1));
                    data = data.substring(0, index);
                }

                String amount = lootData[2];
                int lower = PhatLootsCommand.getLowerBound(amount);
                int upper = PhatLootsCommand.getUpperBound(amount);

                if (lower == -1 || upper == -1) {
                    throw new RuntimeException();
                }

                Loot loot = new Loot(itemID, lower, upper);
                if (color != null) {
                    loot.setColor(color);
                }
                loot.setProbability(Double.parseDouble(lootData[3]));

                try {
                    loot.setDurability(Short.parseShort(data));
                } catch (Exception notDurability) {
                    enchantments = PhatLootsCommand.getEnchantments(data);
                }
                loot.setEnchantments(enchantments);

                loot.name = item;
                loots[id].add(loot);
            } catch (Exception invalidLoot) {
                PhatLoots.logger.info("Error occured while loading PhatLoot "
                                        + '"' + name + '"' + ", " + '"' + lootString
                                        + '"' + " is not a valid Loot");
                invalidLoot.printStackTrace();
            }
        }
    }

    /**
     * Loads data from the save file
     *
     * @param string The data of the Chests
     */
    public void setChests(String data) {
        //Cancel if no data was provided
        if (data.isEmpty()) {
            return;
        }

        for (String chest: data.split(", ")) {
            try {
                String[] chestData = chest.split("'");

                //Check if the World is not loaded
                if (PhatLoots.server.getWorld(chestData[0]) == null) {
                    oldChests.add(chest);
                    continue;
                }

                //Construct a new PhatLootChest with the Location data
                PhatLootChest phatLootChest = new PhatLootChest(chestData[0], Integer.parseInt(chestData[1]),
                        Integer.parseInt(chestData[2]), Integer.parseInt(chestData[3]));

                chests.add(phatLootChest);
            } catch (Exception invalidChest) {
                PhatLoots.logger.info("Error occured while loading PhatLoot "
                                        + '"' + name + '"' + ", " + '"' + chest
                                        + '"' + " is not a valid PhatLootChest");
                invalidChest.printStackTrace();
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
        if (data.isEmpty()) {
            return;
        }

        int index;

        //Load data for each PhatLootChest
        for (String chest: data.split("; ")) {
            try {
                String[] chestData = chest.split("\\{", 2);

                //Load the Block Location data of the Chest
                String[] blockData = chestData[0].split("'");

                //Construct a a new PhatLootChest with the Location data
                PhatLootChest phatLootChest = new PhatLootChest(blockData[0], Integer.parseInt(blockData[1]),
                        Integer.parseInt(blockData[2]), Integer.parseInt(blockData[3]));

                //Load the HashMap of loot times of the Chest
                for (String user: chestData[1].substring(0, chestData[1].length() - 1).split(", ")) {
                    //Don't load if the data if it is corrupt or empty
                    if ((index = user.indexOf('@')) != -1) {
                        String timeString = user.substring(index + 1);
                        lootTimes.setProperty(phatLootChest.toString() + "'"
                                        + user.substring(0, index), timeString);
                    }
                }

                //Check if the World is not loaded
                if (PhatLoots.server.getWorld(blockData[0]) == null) {
                    oldChests.add(chestData[0]);
                } else {
                    chests.add(phatLootChest);
                }
            } catch (Exception invalidChest) {
                PhatLoots.logger.info("Error occured while loading PhatLoot "
                                        + '"' + name + '"' + ", " + '"' + chest
                                        + '"' + " is not a valid PhatLootChest");
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
        for (Loot loot: loots[id]) {
            list = list.concat(loot.toInfoString());
        }

        if (!list.isEmpty()) {
            list = list.substring(2);
        }

        return list;
    }

    /**
     * Creates a PhatLootChest for the given Block and links it to this PhatLoot
     *
     * @param block The given Block
     */
    public void addChest(Block block) {
        PhatLootChest chest = findChest(block);
        if (chest == null) {
            chests.add(new PhatLootChest(block));
        }
        save();
    }

    /**
     * removes the PhatLootChest for the given Block and links it to this PhatLoot
     *
     * @param block The given Block
     */
    public void removeChest(Block block) {
        PhatLootChest chest = findChest(block);
        if (chest != null) {
            reset(block);
            chests.remove(chest);
        }
    }

    /**
     * Returns the PhatLootChest that is associated with the given Block
     *
     * @param block The given Block
     * @return The PhatLootChest that is associated with the given Block
     */
    public PhatLootChest findChest(Block block) {
        //Iterate through chests to find the PhatLootChest of the given Block
        for (PhatLootChest chest: chests) {
            if (chest.isBlock(block)) {
                return chest;
            }
        }

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
        if (block == null) {
            //Reset all PhatLootChests
            lootTimes.clear();
        } else {
            //Find the PhatLootChest of the given Block and reset it
            String chest = findChest(block).toString() + "'";
            for (String key: lootTimes.stringPropertyNames()) {
                if (key.startsWith(chest)) {
                    lootTimes.remove(key);
                }
            }
        }
        save();
    }

    /**
     * Writes the PhatLoot data to file
     */
    public void save() {
        PhatLoots.savePhatLoot(this);
    }

    private String getItemName(ItemStack item) {
        if (!item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        return item.getType().toString();
    }
}

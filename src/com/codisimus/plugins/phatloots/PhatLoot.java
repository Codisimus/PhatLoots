package com.codisimus.plugins.phatloots;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.milkbowl.vault.economy.EconomyResponse;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A PhatLoot is a reward made up of money and items
 *
 * @author Cody
 */
@SerializableAs("PhatLoot")
public class PhatLoot implements ConfigurationSerializable {
    static String current; //The currently loadeding PhatLoot (used for debugging)
    static String last; //The last successfully loaded PhatLoot (used for debugging)
    static boolean onlyDropOnPlayerKill; //True if mobs should drop loot when dying of natural causes
    static boolean replaceMobLoot; //False if default mob loot should still be present
    static float chanceOfDrop; //The chance of mobs dropping their loot armor
    static double lootingBonusPerLvl;
    static boolean autoClose; //True if inventories should automatically close if the Player does not have sufficient funds
    static boolean decimals; //True if money values should include decimals
    static boolean unlink; //True if global chests that never reset should be unlinked after looting

    public String name; //A unique name for the PhatLoot
    public int moneyLower; //Range of money that may be given
    public int moneyUpper;
    public int expLower; //Range of experience gained when looting
    public int expUpper;
    public ArrayList<Loot> lootList; //List of Loot

    public int days = PhatLootsConfig.defaultDays; //Reset time (will never reset if any are negative)
    public int hours = PhatLootsConfig.defaultHours;
    public int minutes = PhatLootsConfig.defaultMinutes;
    public int seconds = PhatLootsConfig.defaultSeconds;
    public boolean global = PhatLootsConfig.defaultGlobal; //Reset Type
    public boolean round = PhatLootsConfig.defaultRound;
    public boolean autoLoot = PhatLootsConfig.defaultAutoLoot;
    public boolean breakAndRespawn = PhatLootsConfig.defaultBreakAndRespawn;
    private HashSet<PhatLootChest> chests = new HashSet<PhatLootChest>(); //Set of Chests linked to this PhatLoot
    Properties lootTimes = new Properties(); //PhatLootChest'PlayerName=Year'Day'Hour'Minute'Second

    /**
     * Constructs a new PhatLoot
     *
     * @param name The name of the PhatLoot which will be created
     */
    public PhatLoot(String name) {
        this.name = name;
        lootList = new ArrayList<Loot>();
    }

    /**
     * Constructs a new PhatLoot from a Configuration Serialized phase
     *
     * @param map The map of data values
     */
    public PhatLoot(Map<String, Object> map) {
        String currentLine = null; //The value that is about to be loaded (used for debugging)
        try {
            current = name = (String) map.get(currentLine = "Name");

            Map nestedMap = (Map) map.get(currentLine = "Reset");
            days = (Integer) nestedMap.get(currentLine = "Days");
            hours = (Integer) nestedMap.get(currentLine = "Hours");
            minutes = (Integer) nestedMap.get(currentLine = "Minutes");
            seconds = (Integer) nestedMap.get(currentLine = "Seconds");

            global = (Boolean) map.get(currentLine = "Global");
            round = (Boolean) map.get(currentLine = "RoundDownTime");
            autoLoot = (Boolean) map.get(currentLine = "AutoLoot");
            if (map.containsKey("BreakAndRespawn")) {
                breakAndRespawn = (Boolean) map.get(currentLine = "BreakAndRespawn");
            }

            nestedMap = (Map) map.get(currentLine = "Money");
            moneyUpper = (Integer) nestedMap.get(currentLine = "Upper");
            moneyLower = (Integer) nestedMap.get(currentLine = "Lower");

            nestedMap = (Map) map.get(currentLine = "Exp");
            expUpper = (Integer) nestedMap.get(currentLine = "Upper");
            expLower = (Integer) nestedMap.get(currentLine = "Lower");

            //Check which version the file is
            if (map.containsKey(currentLine = "LootList")) { //3.1+
                lootList = (ArrayList) map.get(currentLine = "LootList");
            } else { //pre-3.1
                PhatLoots.logger.warning("Your save files are outdated, please use version 3.1-3.2 to update them");
            }
        } catch (Exception ex) {
            //Print debug messages
            PhatLoots.logger.severe("Failed to load line: " + currentLine);
            PhatLoots.logger.severe("of PhatLoot: " + (current == null ? "unknown" : current));
            if (current == null) {
                PhatLoots.logger.severe("Last successfull load was...");
                PhatLoots.logger.severe("PhatLoot: " + (last == null ? "unknown" : last));
            }
        }
        last = current;
        current = null;

        loadChests();
        loadLootTimes();
    }

    /**
     * Rolls for loot of a given PhatLootChest
     *
     * @param player The Player who is looting
     * @param chest The given PhatLootChest
     * @param inventory  The Inventory to place the items in
     */
    public void rollForLoot(Player player, PhatLootChest chest, Inventory inventory) {
        //Retrieve the last time the chest was looted by the user
        String user = global ? "global" : player.getName();
        long time = getTime(chest, user);
        if (time > 0L) {
            String timeRemaining = getTimeRemaining(time);
            if (timeRemaining == null) { //The PhatLoot never resets
                return;
            }

            if (!timeRemaining.equals("0")) { //There is time remaining
                if (PhatLootsConfig.timeRemaining != null) {
                    player.sendMessage(PhatLootsConfig.timeRemaining.replace("<time>", timeRemaining));
                }
                return;
            }
        }

        //Reset the Inventory, old loot is thrown away
        inventory.clear();

        //Check if there is money to loot
        if (moneyLower != 0 && moneyUpper != 0) {
            //Roll for the amount of money
            double amount = PhatLoots.rollForInt(moneyLower, moneyUpper);
            if (decimals) {
                amount = amount / 100;
            }

            if (amount > 0) { //Reward
                if (PhatLoots.econ != null) {
                    EconomyResponse r = PhatLoots.econ.depositPlayer(player.getName(), amount);
                    if (r.transactionSuccess() && PhatLootsConfig.moneyLooted != null) {
                        String money = PhatLoots.econ.format(amount).replace(".00", "");
                        player.sendMessage(PhatLootsConfig.moneyLooted.replace("<amount>", money));
                    }
                } else {
                    player.sendMessage("§6Vault §4is not enabled, so no money can be processed.");
                }
            } else if (amount < 0) { //Cost
                amount *= -1;
                if (PhatLoots.econ != null) {
                    EconomyResponse r = PhatLoots.econ.withdrawPlayer(player.getName(), amount);
                    String money = PhatLoots.econ.format(amount).replace(".00", "");
                    if (r.transactionSuccess()) {
                        if (PhatLootsConfig.moneyCharged != null) {
                            player.sendMessage(PhatLootsConfig.moneyCharged.replace("<amount>", money));
                        }
                    } else {
                        if (PhatLootsConfig.insufficientFunds != null) {
                            player.sendMessage(PhatLootsConfig.insufficientFunds.replace("<amount>", money));
                        }
                        if (autoClose) {
                            player.closeInventory();
                        }
                        return;
                    }
                } else {
                    //Don't let them loot without paying
                    player.sendMessage("§6Vault §4is not enabled, so no money can be processed.");
                    if (autoClose) {
                        player.closeInventory();
                    }
                    return;
                }
            }

        }

        //Check if there is experience to be looted
        if (expUpper > 0) {
            int amount = PhatLoots.rollForInt(expLower, expUpper);
            if (amount > 0) {
                player.giveExp(amount);
                if (PhatLootsConfig.experienceLooted != null) {
                    player.sendMessage(PhatLootsConfig.experienceLooted.replace("<amount>", String.valueOf(amount)));
                }
            }
        }

        if (PhatLootsConfig.lootMessage != null) {
            player.sendMessage(PhatLootsConfig.lootMessage.replace("<phatloot>", name));
        }
        if (PhatLootsConfig.lootBroadcast != null) {
            Bukkit.broadcastMessage(PhatLootsConfig.lootBroadcast
                                                .replace("<name>", player.getName())
                                                .replace("<phatloot>", name));
        }

        //Loot all the loot!
        //Add the loot to the chest (or player's inventory if autoloot is true)
        //The returned value is whether there are still items in the chest
        boolean itemsInChest = chest.addLoots(lootAll(player, 0), player, inventory, autoLoot);

        //Solves some inventory issues
        if (!chest.isDispenser) {
            player.updateInventory();
        }

        //Close the chest if it was autolooted and nothing is left in the chest
        if (autoLoot && !itemsInChest) {
            player.closeInventory();
        }

        //Unlink the chest if it is global and never resets
        if (global && unlink && (days < 0 || hours < 0 || minutes < 0 || seconds < 0)) {
            chests.remove(chest);
            saveChests();
        } else {
            //Update the time that the user looted the chest
            setTime(chest, user);
        }
    }

    /**
     * Rolls for loot to add to the given mob drops
     *
     * @param mob The LivingEntity that was killed
     * @param player The player who killed the mob or null if the mob died of natural causes
     * @param drops The list of items that the mob drops
     * @return The amount of experience that the mob should drop
     */
    public int rollForMobDrops(LivingEntity mob, Player player, List<ItemStack> drops) {
        if (onlyDropOnPlayerKill && player == null) {
            drops.clear();
            return 0;
        }

        //Get the weapon that caused the final blow
        ItemStack weapon = player == null ? null : player.getItemInHand();
        //The looting bonus is determined by the LOOT_BONUS_MOBS enchantment on the weapon
        double lootingBonus = weapon == null
                              ? 0
                              : lootingBonusPerLvl * weapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

        if (replaceMobLoot) {
            Iterator itr = drops.iterator();
            //Remove each item from the drops
            while (itr.hasNext()) {
                ItemStack item = (ItemStack) itr.next();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    //If the item has a custom name then it is most likely a PhatLoots item
                    continue;
                }
                itr.remove();
            }
        }

        if (player != null) {
            //Check if the PhatLoot has timed out
            long time = getTime(null, player.getName());
            if (time > 0) {
                String timeRemaining = getTimeRemaining(time);
                if (timeRemaining == null) {
                    return 0;
                }

                if (!timeRemaining.equals("0")) {
                    if (PhatLootsConfig.mobTimeRemaining != null) {
                        player.sendMessage(PhatLootsConfig.mobTimeRemaining.replace("<time>", timeRemaining));
                    }
                    return 0;
                }
            }
        }

        //Get the list of looted items
        drops.addAll(lootAll(player, lootingBonus));

        //Get the amount of money to be looted
        double money = 0;
        //Check if there is money to be dropped
        if (moneyUpper > 0 && player != null) {
            money = PhatLoots.rollForInt(moneyLower, moneyUpper);
            if (decimals) {
                money /= 100;
            }
            //Check if the player is allowed to loot money
            if (money > 0 && (player.getGameMode().equals(GameMode.CREATIVE) || !player.hasPermission("phatloots.moneyfrommobs"))) {
                money = 0;
            }
        }

        //Get the amount of experience to be looted
        int exp = 0;
        //Check if there is experience to be dropped
        if (expUpper > 0) {
            //Roll for the amount
            exp = PhatLoots.rollForInt(expLower, expUpper);
        }

        //Call the event to be modified
        MobDropLootEvent event = new MobDropLootEvent(mob, player, drops, money, exp);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            drops.clear();
            return 0;
        } else {
            money = event.getMoney();
            exp = event.getExp();
        }

        //Send a message for each item looted
        if (player != null && PhatLootsConfig.mobDroppedItem != null) {
            for (ItemStack item : drops) {
                String msg = PhatLootsConfig.mobDroppedItem.replace("<item>", getItemName(item));
                int amount = item.getAmount();
                msg = amount > 1
                      ? msg.replace("<amount>", String.valueOf(amount))
                      : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(amount));
                player.sendMessage(msg);
            }
        }

        //Give the player the looted money
        if (PhatLoots.econ != null) {
            EconomyResponse r = PhatLoots.econ.depositPlayer(player.getName(), money);
            if (r.transactionSuccess() && PhatLootsConfig.mobDroppedMoney != null) {
                String amount = PhatLoots.econ.format(money).replace(".00", "");
                player.sendMessage(PhatLootsConfig.mobDroppedMoney.replace("<amount>", amount));
            }
        } else {
            player.sendMessage("§6Vault §4is not enabled, so no money can be processed.");
        }

        //Send the experienced dropped message if it is present
        if (exp > 0 && player != null && PhatLootsConfig.mobDroppedExperience != null) {
            player.sendMessage(PhatLootsConfig.mobDroppedExperience.replace("<amount>", String.valueOf(exp)));
        }

        return exp;
    }

    /**
     * Rolls for loot that becomes the given mob's equipment
     *
     * @param entity The given LivingEntity
     * @param level The 'level' of the mob
     */
    public void rollForLoot(LivingEntity entity, double level) {
        //Roll for all loot
        LinkedList<ItemStack> loot = lootAll(null, level);
        //Ensure there are 5 items (even if some are air)
        if (loot.size() != 5) {
            PhatLoots.logger.warning("Cannot add loot to " + entity.getType().getName() + " because the amount of loot was not equal to 5");
        }

        //The order of equipment should be Hand, Helm, Plate, Legs, Boots
        EntityEquipment eqp = entity.getEquipment();
        eqp.setItemInHand(loot.removeFirst());
        eqp.setHelmet(loot.removeFirst());
        eqp.setChestplate(loot.removeFirst());
        eqp.setLeggings(loot.removeFirst());
        eqp.setBoots(loot.removeFirst());

        //Set the drop chance of each item
        eqp.setItemInHandDropChance(chanceOfDrop);
        eqp.setHelmetDropChance(chanceOfDrop);
        eqp.setChestplateDropChance(chanceOfDrop);
        eqp.setLeggingsDropChance(chanceOfDrop);
        eqp.setBootsDropChance(chanceOfDrop);

        /*
         * This is another way that equipment could be handled
         * It may make more sense to the user but is very limiting
         *
        EntityEquipment eqp = entity.getEquipment();
        LinkedList<ItemStack> loot = new LinkedList();
        LootCollection coll = getCollection("hand");
        if (coll != null) {
            coll.getLoot(null, level, loot);
            eqp.setItemInHand(loot.removeLast());
            eqp.setItemInHandDropChance(chanceOfDrop);
        }
        coll = getCollection("helmet");
        if (coll != null) {
            coll.getLoot(null, level, loot);
            eqp.setHelmet(loot.removeLast());
            eqp.setHelmetDropChance(chanceOfDrop);
        }
        coll = getCollection("chestplate");
        if (coll != null) {
            coll.getLoot(null, level, loot);
            eqp.setChestplate(loot.removeLast());
            eqp.setChestplateDropChance(chanceOfDrop);
        }
        coll = getCollection("leggings");
        if (coll != null) {
            coll.getLoot(null, level, loot);
            eqp.setLeggings(loot.removeLast());
            eqp.setLeggingsDropChance(chanceOfDrop);
        }
        coll = getCollection("boots");
        if (coll != null) {
            coll.getLoot(null, level, loot);
            eqp.setBoots(loot.removeLast());
            eqp.setBootsDropChance(chanceOfDrop);
        }
         *
         */
    }

    /**
     * Rolls for all loot
     */
    public LinkedList<ItemStack> rollForLoot() {
        return lootAll(null, 0);
    }

    /**
     * Returns the remaining time until the PhatLootChest resets
     * Returns null if the PhatLootChest never resets
     *
     * @param time The given time
     * @return the remaining time until the PhatLootChest resets
     */
    public String getTimeRemaining(long time) {
        //Return null if the reset time is set to never
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
            return null;
        }

        //Calculate the time that the chest will reset
        time += days * DateUtils.MILLIS_PER_DAY
                + hours * DateUtils.MILLIS_PER_HOUR
                + minutes * DateUtils.MILLIS_PER_MINUTE
                + seconds * DateUtils.MILLIS_PER_SECOND;

        long timeRemaining = time - System.currentTimeMillis();

        //Find the appropriate unit of time and return that amount
        if (timeRemaining > DateUtils.MILLIS_PER_DAY) {
            return (int) timeRemaining / DateUtils.MILLIS_PER_DAY + " day(s)";
        } else {
            if (timeRemaining > DateUtils.MILLIS_PER_HOUR) {
                return (int) timeRemaining / DateUtils.MILLIS_PER_HOUR + " hour(s)";
            } else {
                if (timeRemaining > DateUtils.MILLIS_PER_MINUTE) {
                    return (int) timeRemaining / DateUtils.MILLIS_PER_MINUTE + " minute(s)";
                } else {
                    if (timeRemaining > DateUtils.MILLIS_PER_SECOND) {
                        return (int) timeRemaining / DateUtils.MILLIS_PER_SECOND + " second(s)";
                    } else {
                        return "0";
                    }
                }
            }
        }
    }

    /**
     * Returns the remaining time until the global PhatLootChest resets
     * Returns -1 if the PhatLootChest never resets
     *
     * @param chest The given PhatLootChest
     * @return the remaining time until the PhatLootChest resets
     */
    public long getTimeRemaining(PhatLootChest chest) {
        //Return null if the reset time is set to never
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
            return -1;
        }

        String string = lootTimes.getProperty(chest.toString() + "'global");
        long time = 0;
        if (string != null) {
            try {
                time = Long.parseLong(string);
            } catch (NumberFormatException notLong) {
                PhatLoots.logger.severe("Fixed corrupted time value!");
            }
        }

        //Calculate the time that the chest will reset
        time += days * DateUtils.MILLIS_PER_DAY
                + hours * DateUtils.MILLIS_PER_HOUR
                + minutes * DateUtils.MILLIS_PER_MINUTE
                + seconds * DateUtils.MILLIS_PER_SECOND;

        return time - System.currentTimeMillis();
    }

    /**
     * Fills the Chest (Block) with loot
     * Each item is rolled for to determine if it will by added to the Chest
     * Money is rolled for to determine how much will be given within the range
     */
    public LinkedList<ItemStack> lootAll(Player player, double lootingBonus) {
        LinkedList<ItemStack> itemList = new LinkedList<ItemStack>();
        for (Loot loot : lootList) {
            if (loot.rollForLoot(lootingBonus)) {
                loot.getLoot(player, lootingBonus, itemList);
            }
        }
        return itemList;
    }

    /**
     * Updates the Player's time value in the Map with the current time
     *
     * @param chest The PhatLootChest to set the time for
     * @param player The Player whose time is to be updated
     */
    public void setTime(PhatLootChest chest, String player) {
        Calendar calendar = Calendar.getInstance();

        if (round) {
            //Don't worry about the lower unset time values
            if (seconds == 0) {
                calendar.clear(Calendar.SECOND);
                if (minutes == 0) {
                    calendar.clear(Calendar.MINUTE);
                    if (hours == 0) {
                        calendar.clear(Calendar.HOUR_OF_DAY);
                    }
                }
            }
        }

        lootTimes.setProperty(chest.toString() + "'" + player, String.valueOf(calendar.getTimeInMillis()));
    }

    /**
     * Retrieves the time for the given Player
     *
     * @param chest The PhatLootChest to set the time for or null if it is for a mob
     * @param player The Player whose time is requested
     * @return The time as an array of ints
     */
    public long getTime(PhatLootChest chest, String player) {
        String string = lootTimes.getProperty(chest == null
                                              ? player
                                              : chest.toString() + "'" + player);
        long time = 0;
        if (string != null) {
            try {
                time = Long.parseLong(string);
            } catch (NumberFormatException notLong) {
                PhatLoots.logger.severe("Fixed corrupted time value!");
            }
        }
        return time;
    }

    /**
     * Creates a PhatLootChest for the given Block and links it to this PhatLoot
     *
     * @param block The given Block
     */
    public void addChest(Block block) {
        chests.add(PhatLootChest.getChest(block));
    }

    /**
     * Removes the PhatLootChest for the given Block from this PhatLoot
     *
     * @param block The given Block
     */
    public void removeChest(Block block) {
        chests.remove(PhatLootChest.getChest(block));
    }

    /**
     * Removes all PhatLootChests that are linked to this PhatLoot
     */
    public void removeChests() {
        chests.clear();
    }

    /**
     * Returns whether the given PhatLootChest is linked to this PhatLoot
     *
     * @param chest The given PhatLootChest
     * @return true if the PhatLoot chest is linked
     */
    public boolean containsChest(PhatLootChest chest) {
        return chests.contains(chest);
    }

    /**
     * Returns a Collection of PhatLootChests linked to this PhatLoot
     *
     * @return a Collection of linked chests
     */
    public Collection<PhatLootChest> getChests() {
        return chests;
    }

    /**
     * Adds the given loot to this PhatLoot
     *
     * @param target the given Loot
     * @return true if it was successfully added
     */
    public boolean addLoot(Loot target) {
        //Scan for the loot to ensure it is not there
        for (Loot loot : lootList) {
            if (loot.equals(target)) {
                return false;
            }
        }
        lootList.add(target);
        return true;
    }

    /**
     * Removes the given loot from this PhatLoot
     *
     * @param target the given Loot
     * @return false if it was not found
     */
    public boolean removeLoot(Loot target) {
        Iterator<Loot> itr = lootList.iterator();
        //Scan for the loot to see if it is there
        while (itr.hasNext()) {
            if (itr.next().equals(target)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the collection of the given name
     *
     * @param target The name of the collection to search for
     * @return The LootCollection or null if it was not found
     */
    public LootCollection findCollection(String target) {
        //Scan each loot object
        for (Loot loot : lootList) {
            if (loot instanceof LootCollection) {
                //Recursively look for the collection
                LootCollection coll = ((LootCollection) loot).findCollection(target);
                if (coll != null) {
                    return coll;
                }
            }
        }
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
            String chest = block.getWorld().getName() + "'" + block.getX() + "'" + block.getY() + "'" + block.getZ() + "'";
            for (String key : lootTimes.stringPropertyNames()) {
                if (key.startsWith(chest)) {
                    lootTimes.remove(key);
                }
            }
        }
    }

    /**
     * Removes all loot times for the given block that have fully cooled down
     *
     * @param block The given Block or null to signify all blocks
     */
    public void clean(Block block) {
        //Check if the reset time is 0 seconds
        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            //Reset the PhatLoot because all times have cooled down
            reset(block);
            return;
        }

        Set<String> keys; //The Set of all keys to check

        if (block == null) { //Add all keys
            keys = lootTimes.stringPropertyNames();
        } else { //Add each key that starts with the chest location
            keys = new HashSet();
            String chest = block.getWorld().getName() + "'" + block.getX() + "'" + block.getY() + "'" + block.getZ() + "'";
            for (String key : lootTimes.stringPropertyNames()) {
                if (key.startsWith(chest)) {
                    keys.add(key);
                }
            }
        }

        //Calculate the latest timestamp that would have reset by now
        long time = System.currentTimeMillis()
                    - days * DateUtils.MILLIS_PER_DAY
                    - hours * DateUtils.MILLIS_PER_HOUR
                    - minutes * DateUtils.MILLIS_PER_MINUTE
                    - seconds * DateUtils.MILLIS_PER_SECOND;

        //Remove each key whose value is less than the calculated time
        for (String key : keys) {
            if (Long.parseLong(lootTimes.getProperty(key)) < time) {
                lootTimes.remove(key);
            }
        }
    }

    /**
     * Saves all data of the PhatLoot
     */
    public void saveAll() {
        save();
        saveLootTimes();
        saveChests();
    }

    /**
     * Writes the Loot Tables of the PhatLoot to file
     * if there is an old file it is over written
     */
    public void save() {
        OutputStreamWriter out = null;
        try {
            //Create a new config and populate it with this PhatLoot's information
            YamlConfiguration config = new YamlConfiguration();
            config.set(name, this);

            //Save the config with UTF-8 encoding
            File file = new File(PhatLoots.dataFolder + File.separator + "LootTables" + File.separator + name + ".yml");
            String data = config.saveToString();
            out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            out.write(data, 0, data.length());
        } catch (IOException ex) {
            PhatLoots.logger.log(Level.SEVERE, "Could not save PhatLoot " + name, ex);
        } finally {
            try {
                out.flush();
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Writes the Loot times of the PhatLoot to file
     * if there is an old file it is over written
     */
    public void saveLootTimes() {
        //Don't save an empty file
        if (lootTimes.isEmpty()) {
            return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(PhatLoots.dataFolder + File.separator + "LootTimes" + File.separator + name + ".properties");
            lootTimes.store(fos, null);
        } catch (IOException ex) {
            PhatLoots.logger.log(Level.SEVERE, "Save Failed!", ex);
        } finally { //Close stream (this would be replaced with 'try with resources' in a perfect world)
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * Reads Loot times of the PhatLoot from file
     */
    public void loadLootTimes() {
        FileInputStream fis = null;
        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "LootTimes" + File.separator + name + ".properties");
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            lootTimes.load(fis);
        } catch (IOException ex) {
            PhatLoots.logger.log(Level.SEVERE, "Load Failed!", ex);
        } finally { //Close stream (this would be replaced with 'try with resources' in a perfect world)
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
     * Writes the Chest Locations of the PhatLoot to file
     * if there is an old file it is over written
     */
    public void saveChests() {
        //Don't save an empty file
        if (chests.isEmpty()) {
            return;
        }

        FileWriter fWriter = null;
        PrintWriter pWriter = null;
        try {
            fWriter = new FileWriter(PhatLoots.dataFolder + File.separator + "Chests" + File.separator + name + ".txt");
            pWriter = new PrintWriter(fWriter);
            for (PhatLootChest chest : getChests()) {
                pWriter.println(chest.toString());
            }
        } catch (IOException ex) {
            PhatLoots.logger.log(Level.SEVERE, "Save Failed!", ex);
        } finally { //Close writers (this would be replaced with 'try with resources' in a perfect world)
            try {
                if (fWriter != null) {
                    fWriter.close();
                }
                if (pWriter != null) {
                    pWriter.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Reads Chest Locations of the PhatLoot from file
     */
    public void loadChests() {
        Scanner scanner = null;
        try {
            File file = new File(PhatLoots.dataFolder + File.separator + "Chests" + File.separator + name + ".txt");
            if (!file.exists()) {
                return;
            }

            //Delete empty files
            if (file.length() == 0) {
                file.delete();
                return;
            }

            //Each line of the file is a new PhatLootChest
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                chests.add(PhatLootChest.getChest(scanner.next().split("'")));
            }
        } catch (IOException ex) {
            PhatLoots.logger.log(Level.SEVERE, "Load Failed!", ex);
        } finally { //Close scanner (this would be replaced with 'try with resources' in a perfect world)
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Returns a user friendly String of the given ItemStack's name
     *
     * @param item The given ItemStack
     * @return The name of the item
     */
    public static String getItemName(ItemStack item) {
        //Return the Display name of the item if there is one
        if (item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        //A display name was not found so use a cleaned up version of the Material name
        return WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));
    }

    @Override
    public Map<String, Object> serialize() {
        Map map = new TreeMap();
        map.put("Name", name);

        Map nestedMap = new HashMap();
        nestedMap.put("Days", days);
        nestedMap.put("Hours", hours);
        nestedMap.put("Minutes", minutes);
        nestedMap.put("Seconds", seconds);
        map.put("Reset", nestedMap);

        map.put("Global", global);
        map.put("RoundDownTime", round);
        map.put("AutoLoot", autoLoot);
        map.put("BreakAndRespawn", breakAndRespawn);

        nestedMap = new HashMap();
        nestedMap.put("Upper", moneyUpper);
        nestedMap.put("Lower", moneyLower);
        map.put("Money", nestedMap);

        nestedMap = new HashMap();
        nestedMap.put("Upper", expUpper);
        nestedMap.put("Lower", expLower);
        map.put("Exp", nestedMap);

        map.put("LootList", lootList);
        return map;
    }
}

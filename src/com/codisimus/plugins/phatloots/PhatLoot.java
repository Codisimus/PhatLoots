package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.events.MobDropLootEvent;
import com.codisimus.plugins.phatloots.events.MobEquipEvent;
import com.codisimus.plugins.phatloots.events.PlayerLootEvent;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.Loot;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import com.codisimus.plugins.phatloots.loot.LootCollection;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import net.milkbowl.vault.economy.EconomyResponse;
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
    public static String current; //The currently loadeding PhatLoot (used for debugging)
    public static String last; //The last successfully loaded PhatLoot (used for debugging)
    static boolean onlyDropOnPlayerKill; //True if mobs should drop loot when dying of natural causes
    static boolean replaceMobLoot; //False if default mob loot should still be present
    static float chanceOfDrop; //The chance of mobs dropping their loot armor
    public static double lootingBonusPerLvl;
    static boolean decimals; //True if money values should include decimals
    static boolean unlink; //True if global chests that never reset should be unlinked after looting
    static boolean soundOnAutoLoot;

    public String name; //A unique name for the PhatLoot
    public int moneyLower; //Range of money that may be given
    public int moneyUpper;
    public int expLower; //Range of experience gained when looting
    public int expUpper;
    public ArrayList<Loot> lootList; //List of Loot

    public int days; //Reset time (will never reset if any are negative)
    public int hours;
    public int minutes;
    public int seconds;
    public boolean global; //Reset Type
    public boolean round;
    public boolean autoLoot;
    public boolean breakAndRespawn;
    private HashSet<PhatLootChest> chests = new HashSet<PhatLootChest>(); //Set of Chests linked to this PhatLoot
    private Properties lootTimes = new Properties(); //PhatLootChest'PlayerName=Year'Day'Hour'Minute'Second

    /**
     * Constructs a new PhatLoot
     *
     * @param name The name of the PhatLoot which will be created
     */
    public PhatLoot(String name) {
        this.name = name;
        lootList = new ArrayList<Loot>();
        days = PhatLootsConfig.defaultDays;
        hours = PhatLootsConfig.defaultHours;
        minutes = PhatLootsConfig.defaultMinutes;
        seconds = PhatLootsConfig.defaultSeconds;
        global = PhatLootsConfig.defaultGlobal;
        round = PhatLootsConfig.defaultRound;
        autoLoot = PhatLootsConfig.defaultAutoLoot;
        breakAndRespawn = PhatLootsConfig.defaultBreakAndRespawn;
    }

    /**
     * Convenience method for getTimeRemaining(player, chest) where chest == null
     *
     * @param player The given Player
     * @return the remaining time until the player resets
     */
    public long getTimeRemaining(Player player) {
        return getTimeRemaining(player, null);
    }

    /**
     * Convenience method for getTimeRemaining(player, chest) where player == null
     *
     * @param chest The given PhatLootChest
     * @return the remaining time until the global PhatLootChest resets
     */
    public long getTimeRemaining(PhatLootChest chest) {
        return getTimeRemaining(null, chest);
    }

    /**
     * Returns the remaining time until the PhatLootChest resets for the given Player.
     * Returns -1 if the PhatLootChest never resets
     *
     * @param player The given Player
     * @param chest The given PhatLootChest
     * @return the remaining time until the PhatLootChest resets
     */
    public long getTimeRemaining(Player player, PhatLootChest chest) {
        //Return -1 if the reset time is set to never
        if (days < 0 || hours < 0 || minutes < 0 || seconds < 0) {
            return -1;
        }

        //Return 0 if the reset time is set to 0
        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
            return 0;
        }

        //Get the correct timestamp
        String timeStamp = lootTimes.getProperty(getKey(player, chest));
        long time = 0;
        if (timeStamp != null) {
            try {
                time = Long.parseLong(timeStamp);
            } catch (NumberFormatException notLong) {
                PhatLoots.logger.severe("Fixed corrupted time value!");
            }
        }

        //Calculate the time that the chest will reset
        time += days * DateUtils.MILLIS_PER_DAY
                + hours * DateUtils.MILLIS_PER_HOUR
                + minutes * DateUtils.MILLIS_PER_MINUTE
                + seconds * DateUtils.MILLIS_PER_SECOND;

        //Return the remaining time or 0 if the time has already passed
        return Math.max(time - System.currentTimeMillis(), 0);
    }

    /**
     * Returns a human friendly String of the remaining time until the PhatLootChest resets
     *
     * @param time The given time
     * @return the remaining time until the PhatLootChest resets
     */
    public String timeToString(long time) {
        if (time < 0) {
            return "forever";
        }

        //Find the appropriate unit of time and return that amount
        if (time > DateUtils.MILLIS_PER_DAY) {
            return (int) time / DateUtils.MILLIS_PER_DAY + " day(s)";
        } else {
            if (time > DateUtils.MILLIS_PER_HOUR) {
                return (int) time / DateUtils.MILLIS_PER_HOUR + " hour(s)";
            } else {
                if (time > DateUtils.MILLIS_PER_MINUTE) {
                    return (int) time / DateUtils.MILLIS_PER_MINUTE + " minute(s)";
                } else {
                    return (int) time / DateUtils.MILLIS_PER_SECOND + " second(s)";
                }
            }
        }
    }

    /**
     * Updates the Player's time value in the Map with the current time
     *
     * @param player The Player whose time is to be updated
     * @param chest The PhatLootChest to set the time for
     */
    public void setTime(Player player, PhatLootChest chest) {
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

        lootTimes.setProperty(getKey(player, chest), String.valueOf(calendar.getTimeInMillis()));
    }

    /**
     * Rolls for loot to give to the specified player
     *
     * @param player The Player who is looting
     */
    public void rollForLoot(Player player) {
        rollForLoot(player, null);
    }

    /**
     * Rolls for loot to give to the specified player
     *
     * @param player The Player who is looting
     * @param title The title of the Inventory
     */
    public void rollForLoot(Player player, String title) {
        rollForChestLoot(player, null, title);
    }

    /**
     * Rolls for loot to place in the given PhatLootChest
     *
     * @param player The Player who is looting
     * @param chest The PhatLootChest that is being looted
     * @return true if the PhatLootChest should be manually broken
     */
    public boolean rollForChestLoot(Player player, PhatLootChest chest) {
        return rollForChestLoot(player, chest, null);
    }

    /**
     * Rolls for loot to place in the given PhatLootChest
     *
     * @param player The Player who is looting
     * @param chest The PhatLootChest that is being looted
     * @param title The title of the Inventory
     * @return true if the PhatLootChest should be manually broken
     */
    public boolean rollForChestLoot(Player player, PhatLootChest chest, String title) {
        boolean flagToBreak = false;

        if (title == null) {
            title = name;
        }

        //Check if the PhatLoot has timed out
        long time = getTimeRemaining(player, chest);
        if (time > 0) {
            if (PhatLootsConfig.timeRemaining != null) {
                player.sendMessage(PhatLootsConfig.timeRemaining.replace("<time>", timeToString(time)));
            }
            if (chest != null) {
                //Open the Inventory if it is not already open
                Inventory inv = chest.getInventory(getUser(player), title);
                if (player.getOpenInventory().getTopInventory() != inv) {
                    chest.openInventory(player, inv, global);
                }
            }
            return flagToBreak;
        }

        //Roll for the all the Loot
        LootBundle lootBundle = rollForLoot();

        //Call the event to be modified
        PlayerLootEvent event = new PlayerLootEvent(player, this, chest, lootBundle);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return flagToBreak;
        }

        //Do money transactions
        double money = lootBundle.getMoney();
        if (money > 0) { //Reward
            if (PhatLoots.econ != null) {
                EconomyResponse r = PhatLoots.econ.depositPlayer(player.getName(), money);
                if (r.transactionSuccess() && PhatLootsConfig.moneyLooted != null) {
                    String amount = PhatLoots.econ.format(money).replace(".00", "");
                    player.sendMessage(PhatLootsConfig.moneyLooted.replace("<amount>", amount));
                }
            } else {
                player.sendMessage("§6Vault §4is not enabled, so no money can be processed.");
            }
        } else if (money < 0) { //Cost
            money *= -1;
            if (PhatLoots.econ != null) {
                EconomyResponse r = PhatLoots.econ.withdrawPlayer(player.getName(), money);
                String amount = PhatLoots.econ.format(money).replace(".00", "");
                if (r.transactionSuccess()) {
                    if (PhatLootsConfig.moneyCharged != null) {
                        player.sendMessage(PhatLootsConfig.moneyCharged.replace("<amount>", amount));
                    }
                } else {
                    if (PhatLootsConfig.insufficientFunds != null) {
                        player.sendMessage(PhatLootsConfig.insufficientFunds.replace("<amount>", amount));
                    }
                    return flagToBreak;
                }
            } else {
                //Don't let them loot without paying
                player.sendMessage("§6Vault §4is not enabled, so no money can be processed.");
                return flagToBreak;
            }
        }

        //Give the looted experience
        if (lootBundle.getExp() > 0) {
            player.giveExp(lootBundle.getExp());
            if (PhatLootsConfig.experienceLooted != null) {
                player.sendMessage(PhatLootsConfig.experienceLooted.replace("<amount>", String.valueOf(lootBundle.getExp())));
            }
        }

        //Execute each command
        for (CommandLoot command : lootBundle.getCommandList()) {
            command.execute(player);
        }

        //Give all of the items
        List<ItemStack> itemList = lootBundle.getItemList();

        //Get the Inventory for the user
        Inventory inv = PhatLootChest.getInventory(getUser(player), title, chest);
        if (player.getOpenInventory().getTopInventory() != inv) {
            //Reset the Inventory, old loot is thrown away
            inv.clear();
        }

        if (autoLoot) { //AutoLoot the items
            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(itemList.toArray(new ItemStack[itemList.size()]));
            if (PhatLootsConfig.autoLoot != null) {
                int i = 0;
                for (ItemStack item : itemList) {
                    String msg = PhatLootsConfig.autoLoot.replace("<item>", PhatLoots.getItemName(item));
                    int amount = item.getAmount();
                    if (leftovers.containsKey(i)) {
                        amount -= leftovers.get(i).getAmount();
                    }
                    if (amount > 0) {
                        msg = amount > 1
                              ? msg.replace("<amount>", String.valueOf(item.getAmount()))
                              : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(item.getAmount()));
                        player.sendMessage(msg);
                    }
                    i++;
                }
            }
        }
        if (!itemList.isEmpty()) { //Loot did not fit in the Player's Inventory
            //Fill the inventory with items
            chest.addItems(itemList, player, inv);

            //Open the Inventory if it is not already open
            if (player.getOpenInventory().getTopInventory() != inv) {
                chest.openInventory(player, inv, global);
            }
        } else if (!autoLoot) {
            //Open the Inventory if it is not already open (even though no loot was added)
            if (player.getOpenInventory().getTopInventory() != inv) {
                chest.openInventory(player, inv, global);
            }
        } else {
            //The chest should be broken bc it was fully autolooted
            flagToBreak = PhatLootChest.useBreakAndRepawn && breakAndRespawn;
        }

        //Send loot notification messages
        if (PhatLootsConfig.lootMessage != null) {
            player.sendMessage(PhatLootsConfig.lootMessage.replace("<phatloot>", name));
        }
        if (PhatLootsConfig.lootBroadcast != null) {
            Bukkit.broadcastMessage(PhatLootsConfig.lootBroadcast
                                                .replace("<name>", player.getName())
                                                .replace("<phatloot>", name));
        }

        //Solves some inventory issues
        player.updateInventory();

        //Update the time that the user looted
        setTime(player, chest);
        return flagToBreak;
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
            drops.clear(); //Drop no items
            return 0; //Drop no experience
        }

        if (replaceMobLoot) {
            //Remove each item from the drops
            Iterator itr = drops.iterator();
            while (itr.hasNext()) {
                ItemStack item = (ItemStack) itr.next();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    //If the item has a custom name then it is most likely a PhatLoots item
                    continue;
                }
                itr.remove();
            }
        }

        //Check if the PhatLoot has timed out
        long time = getTimeRemaining(player);
        if (time > 0) {
            if (player != null && PhatLootsConfig.mobTimeRemaining != null) {
                player.sendMessage(PhatLootsConfig.mobTimeRemaining.replace("<time>", timeToString(time)));
            }
            return 0; //Drop no experience
        }

        //Get the weapon that caused the final blow
        ItemStack weapon = player == null ? null : player.getItemInHand();
        //The looting bonus is determined by the LOOT_BONUS_MOBS enchantment on the weapon
        double lootingBonus = weapon == null
                              ? 0
                              : lootingBonusPerLvl * weapon.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

        //Roll for the all the Loot
        LootBundle lootBundle = rollForLoot(new LootBundle(drops), lootingBonus);

        //Check if the player is allowed to loot money
        if (lootBundle.getMoney() > 0 && (player.getGameMode().equals(GameMode.CREATIVE) || !player.hasPermission("phatloots.moneyfrommobs"))) {
            lootBundle.setMoney(0);
        }

        //Call the event to be modified
        MobDropLootEvent event = new MobDropLootEvent(mob, player, lootBundle);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            drops.clear(); //Drop no items
            return 0; //Drop no experience
        }

        //Do money transactions
        if (player != null) {
            double money = lootBundle.getMoney();
            if (money > 0) { //Reward
                if (PhatLoots.econ != null) {
                    EconomyResponse r = PhatLoots.econ.depositPlayer(player.getName(), money);
                    if (r.transactionSuccess() && PhatLootsConfig.moneyLooted != null) {
                        String amount = PhatLoots.econ.format(money).replace(".00", "");
                        player.sendMessage(PhatLootsConfig.moneyLooted.replace("<amount>", amount));
                    }
                } else {
                    PhatLoots.logger.warning("§6Vault §4is not enabled, so no money can be processed.");
                }
            } else if (money < 0) { //Cost
                money *= -1;
                if (PhatLoots.econ != null) {
                    EconomyResponse r = PhatLoots.econ.withdrawPlayer(player.getName(), money);
                    String amount = PhatLoots.econ.format(money).replace(".00", "");
                    if (r.transactionSuccess()) {
                        if (PhatLootsConfig.moneyCharged != null) {
                            player.sendMessage(PhatLootsConfig.moneyCharged.replace("<amount>", amount));
                        }
                    } else {
                        if (PhatLootsConfig.insufficientFunds != null) {
                            player.sendMessage(PhatLootsConfig.insufficientFunds.replace("<amount>", amount));
                        }
                        drops.clear(); //Drop no items
                        return 0; //Drop no experience
                    }
                } else {
                    //Don't let them loot without paying
                    PhatLoots.logger.warning("§6Vault §4is not enabled, so no money can be processed.");
                    drops.clear(); //Drop no items
                    return 0; //Drop no experience
                }
            }
        }

        //Execute each command
        for (CommandLoot command : lootBundle.getCommandList()) {
            command.execute(player);
        }

        //Send a message for each item looted
        if (player != null && PhatLootsConfig.mobDroppedItem != null) {
            for (ItemStack item : drops) {
                String msg = PhatLootsConfig.mobDroppedItem.replace("<item>", PhatLoots.getItemName(item));
                int amount = item.getAmount();
                msg = amount > 1
                      ? msg.replace("<amount>", String.valueOf(amount))
                      : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(amount));
                player.sendMessage(msg);
            }
        }

        //Send the experience dropped message if it is present
        if (lootBundle.getExp() > 0 && player != null && PhatLootsConfig.mobDroppedExperience != null) {
            player.sendMessage(PhatLootsConfig.mobDroppedExperience.replace("<amount>", String.valueOf(lootBundle.getExp())));
        }

        return lootBundle.getExp();
    }

    /**
     * Rolls for loot that becomes the given entity's equipment
     *
     * @param entity The given LivingEntity
     * @param level The 'level' of the entity
     */
    public void rollForEquipment(LivingEntity entity, double level) {
        //Roll for all loot
        List<ItemStack> loot = rollForLoot(level).getItemList();
        //Ensure there are 5 items (even if some are air)
        if (loot.size() != 5) {
            //Try to organize the loot
            PhatLoots.logger.warning("Cannot add loot to " + entity.getType().getName() + " because the amount of loot was not equal to 5");
            return;
        }

        EntityEquipment eqp = entity.getEquipment();
        //Remove Minecraft spawned armor
        eqp.clear();
        //The order of equipment should be Hand, Head, Body, Legs, Feet
        eqp.setItemInHand(loot.remove(0));
        eqp.setHelmet(loot.remove(0));
        eqp.setChestplate(loot.remove(0));
        eqp.setLeggings(loot.remove(0));
        eqp.setBoots(loot.remove(0));

        //Set the drop chance of each item
        eqp.setItemInHandDropChance(chanceOfDrop);
        eqp.setHelmetDropChance(chanceOfDrop);
        eqp.setChestplateDropChance(chanceOfDrop);
        eqp.setLeggingsDropChance(chanceOfDrop);
        eqp.setBootsDropChance(chanceOfDrop);

        MobEquipEvent event = new MobEquipEvent(entity);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            eqp.clear();
        }

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
     * Rolls for a new LootBundle from this PhatLoot
     *
     * @return The Loot that has been rolled for
     */
    public LootBundle rollForLoot() {
        return rollForLoot(0);
    }

    /**
     * Rolls for a new LootBundle from this PhatLoot
     *
     * @param lootingBonus The amount to increase each roll by
     * @return The Loot that has been rolled for
     */
    public LootBundle rollForLoot(double lootingBonus) {
        return rollForLoot(new LootBundle(), lootingBonus);
    }

    /**
     * Rolls for a Loot to add to the given LootBundle
     *
     * @param lootBundle The given LootBundle
     * @param lootingBonus The amount to increase each roll by
     * @return The Loot that has been rolled for
     */
    public LootBundle rollForLoot(LootBundle lootBundle, double lootingBonus) {
        lootBundle.setMoney(rollForMoney());
        lootBundle.setExp(rollForExp());
        for (Loot loot : lootList) {
            if (loot.rollForLoot(lootingBonus)) {
                loot.getLoot(lootBundle, lootingBonus);
            }
        }
        return lootBundle;
    }

    /**
     * Rolls for the amount of money
     *
     * @return The amount of money rolled for
     */
    public double rollForMoney() {
        double money = PhatLoots.rollForInt(moneyLower, moneyUpper);
        if (decimals) {
            money /= 100;
        }
        return money;
    }

    /**
     * Rolls for the amount of experience
     *
     * @return The amount of experience rolled for
     */
    public int rollForExp() {
        return Math.max(PhatLoots.rollForInt(expLower, expUpper), 0);
    }

    /**
     * Returns the key for the given Player and PhatLootChest
     *
     * @param player The Player or null if global
     * @param chest The PhatLootChest which may be null
     * @return the key that represents this player and chest
     */
    private String getKey(Player player, PhatLootChest chest) {
        String user = getUser(player);
        return chest == null
               ? user
               : chest.toString() + "'" + user;
    }

    /**
     * Returns the user whether it is the Player's name or 'global'
     *
     * @param player The Player or null if global
     * @return the String of the user
     */
    private String getUser(Player player) {
        return global || player == null
               ? "global"
               : player.getName();
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
     * Returns whether the given PhatLootChest is linked to this PhatLoot
     *
     * @param chest The given PhatLootChest
     * @return true if the PhatLoot chest is linked
     */
    public boolean containsChest(PhatLootChest chest) {
        return chests.contains(chest);
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
     * Resets the user times for all PhatLootChests of this PhatLoot.
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

    /** Save/Load Methods **/

    /**
     * Saves all data of the PhatLoot
     */
    public void saveAll() {
        save();
        saveLootTimes();
        saveChests();
    }

    /**
     * Writes the Loot times of the PhatLoot to file.
     * If there is an old file it is over written
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
     * Writes the Chest Locations of the PhatLoot to file.
     * If there is an old file it is over written
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
     * Writes the Loot Tables of the PhatLoot to file.
     * If there is an old file it is over written
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
}

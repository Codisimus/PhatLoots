package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Safe;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Executes Player Commands
 * 
 * @author Codisimus
 */
public class PhatLootsCommand implements CommandExecutor {
    private static enum Action {
        HELP, MAKE, LINK, UNLINK, DELETE, TIME, GLOBAL, ROUND,
        ADD, REMOVE, MONEY, EXP, LIST, INFO, RESET, RL
    }
    private static enum Help { CREATE, SETUP, LOOT }
    private static final HashSet TRANSPARENT = Sets.newHashSet((byte)0, (byte)6,
            (byte)8, (byte)9, (byte)10, (byte)11, (byte)26, (byte)27, (byte)28,
            (byte)30, (byte)31, (byte)32, (byte)37, (byte)38, (byte)39, (byte)40,
            (byte)44, (byte)50, (byte)51, (byte)53, (byte)55, (byte)59, (byte)64,
            (byte)63, (byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70,
            (byte)71, (byte)72, (byte)75, (byte)76, (byte)77, (byte)78, (byte)85,
            (byte)90, (byte)92, (byte)96, (byte)101, (byte)102, (byte)104,
            (byte)105, (byte)106, (byte)107, (byte)108, (byte)109, (byte)111,
            (byte)113, (byte)114, (byte)115, (byte)117);
    static String command;
    static boolean setUnlockable;
    
    /**
     * Listens for PhatLoots commands to execute them
     * 
     * @param sender The CommandSender who may not be a Player
     * @param command The command that was executed
     * @param alias The alias that the sender used
     * @param args The arguments for the command
     * @return true always
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //Cancel if the command is not from a Player
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equals("rl"))
                PhatLoots.rl();
            
            return true;
        }
        
        Player player = (Player)sender;

        //Display the help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        Action action;
        
        try {
            action = Action.valueOf(args[0].toUpperCase());
        }
        catch (IllegalArgumentException notEnum) {
            //Cancel if the first argument is not a valid PhatLoot
            PhatLoot phatLoot = PhatLoots.getPhatLoot(args[0]);
            if (phatLoot == null) {
                player.sendMessage("PhatLoot " + args[0] + " does not exist");
                return true;
            }

            //Cancel if the Player does not have the needed permission
            if (!PhatLoots.hasPermission(player, "commandloot")
                    || !(PhatLoots.canLoot(player, phatLoot))) {
                player.sendMessage("You do not have permission to do that");
                return true;
            }
            
            Inventory inventory = PhatLoots.server.createInventory(player, 54, phatLoot.name);
            
            //Open the Inventory
            player.openInventory(inventory);

            phatLoot.getLoot(player, new PhatLootChest(player.getLocation().getBlock()), inventory);
            return true;
        }
        
        //Execute the correct command
        switch (action) {
            case MAKE:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                if (args.length == 2)
                    make(player, args[1]);
                else
                    sendCreateHelp(player);
                
                return true;
                
            case LINK:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                if (args.length == 2)
                    link(player, args[1]);
                else
                    sendCreateHelp(player);
                
                return true;
                
            case UNLINK:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 1: unlink(player, null); break;
                    case 2: unlink(player, args[1]); break;
                    default: sendCreateHelp(player); break;
                }
                
                return true;
                
            case DELETE:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                if (args.length == 2) {
                    PhatLoot delete = PhatLoots.getPhatLoot(args[1]);
                    
                    if (delete == null)
                        player.sendMessage("PhatLoot " + args[1] + " does not exist!");
                    else {
                        PhatLoots.removePhatLoot(delete);
                        player.sendMessage("PhatLoot " + delete.name + " was deleted!");
                    }
                }
                else
                    sendCreateHelp(player);
                
                return true;
                
            case TIME:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 2:  //Name is not provided
                        if (!args[1].equals("never"))
                            break;

                        time(player, null, -1, -1, -1, -1);
                        return true;

                    case 3: //Name is provided
                        if (!args[1].equals("never"))
                            break;

                        time(player, args[1], -1, -1, -1, -1);
                        return true;

                    case 5: //Name is not provided
                        try {
                            time(player, null, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                            return true;
                        }
                        catch (Exception notInt) {
                            break;
                        }
                        
                    case 6: //Name is provided
                        try {
                            time(player, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                                    Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                            return true;
                        }
                        catch (Exception notInt) {
                            break;
                        }
                        
                    default: break;
                }
                
                sendSetupHelp(player);
                return true;
                
            case GLOBAL:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 2: //Name is not provided
                        try {
                            global(player, null, Boolean.parseBoolean(args[1]));
                            return true;
                        }
                        catch (Exception notBool) {
                            break;
                        }
                        
                    case 3: //Name is provided
                        try {
                            global(player, args[1], Boolean.parseBoolean(args[2]));
                            return true;
                        }
                        catch (Exception notBool) {
                            break;
                        }
                        
                    default: break;
                }
                
                sendSetupHelp(player);
                return true;
                
            case ROUND:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 2: //Name is not provided
                        try {
                            round(player, null, Boolean.parseBoolean(args[1]));
                            return true;
                        }
                        catch (Exception notBool) {
                            break;
                        }
                        
                    case 3: //Name is provided
                        try {
                            round(player, args[1], Boolean.parseBoolean(args[2]));
                            return true;
                        }
                        catch (Exception notBool) {
                            break;
                        }
                        
                    default: break;
                }
                
                sendSetupHelp(player);
                return true;
                
            case REMOVE: //Fall through
            case ADD:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                boolean add = action.equals(Action.ADD);
                
                if (args.length > 1 && args[1].equals("cmd")) {
                    String name = null;
                    String cmd = "";
                    int i = 2;
                    
                    if (args.length > 2)
                        
                        
                        if (PhatLoots.hasPhatLoot(args[2])) {
                            name = args[2];
                            i++;
                        }
                    
                    while (i < args.length) {
                        cmd = cmd.concat(args[i].concat(" "));
                        i++;
                    }
                    
                    if (!cmd.isEmpty())
                        cmd = cmd.substring(0, cmd.length() - 1);

                    setCommand(player, name, add, cmd);
                    return true;
                }
                
                String name = null; //Name of the PhatLoot (if not specified then the PhatLoot of the target Block are used)
                int id = 0; //The ID of the Loot collection (defaulted to 0 == IndividualLoots)
                int baseAmount = 1; //Stack size of the Loot item (defaulted to 1)
                int bonusAmount = 1; //Amount to possibly increase the Stack size of the Loot item (defaulted to 1)
                int item = 0; //TypeID of the Loot item (if item is not provided then this is found by checking the Player's hand)
                short data = 0; //Data/Damage value of the Loot item (defaulted to 0 or unused)
                Map<Enchantment, Integer> enchantments = null; //ID of the enchantment (defaulted to -1 or unused)
                double percent = 100; //The chance of recieving the Loot item (defaulted to 100)
                
                ItemStack itemStack; //The Loot item (created from baseAmount, item and data)
                Loot loot; //The Loot to be added (created from itemStack and percent)
                
                
                switch (args.length) {
                    case 1: //0 of 5 fields
                        break;
                        
                    case 2: //1 of 5 fields
                        if (args[1].endsWith("%")) //Percent field
                            percent = getPercent(player, args[1]);
                        else if (args[1].startsWith("coll")) //AddID field
                            id = getCollID(player, args[1]);
                        else if (PhatLoots.hasPhatLoot(args[1])) //Name field
                            name = args[1];
                        else { //Item field
                            item = getItemID(player, args[1]);
                            enchantments = getEnchantments(args[1]);
                            if (enchantments == null)
                                data = getData(player, args[1]);
                        }
                        
                        break;
                            
                    case 3: //2 of 5 fields
                        if (args[2].endsWith("%")) { //Percent field
                            percent = getPercent(player, args[2]);
                            
                            if (args[1].startsWith("coll")) //AddID & Percent fields
                                id = getCollID(player, args[1]);
                            else if (PhatLoots.hasPhatLoot(args[1])) //Name & Percent fields
                                name = args[1];
                            else { //Item & Percent fields
                                item = getItemID(player, args[1]);
                                enchantments = getEnchantments(args[1]);
                                if (enchantments == null)
                                    data = getData(player, args[1]);
                            }
                        }
                        else if (args[1].startsWith("coll")) { //AddID & Item fields
                            id = getCollID(player, args[1]);
                            
                            item = getItemID(player, args[2]);
                            enchantments = getEnchantments(args[2]);
                            if (enchantments == null)
                                data = getData(player, args[2]);
                        }
                        else if (args[2].startsWith("coll")) { //Name & AddID fields
                            name = args[1];
                            
                            id = getCollID(player, args[2]);
                        }
                        else if (PhatLoots.hasPhatLoot(args[1])) { //Name & Item fields
                            name = args[1];
                            
                            item = getItemID(player, args[2]);
                            enchantments = getEnchantments(args[2]);
                            if (enchantments == null)
                                data = getData(player, args[2]);
                        }
                        else { //Amount & Item fields
                            baseAmount = getLowerBound(player, args[1]);
                            bonusAmount = getUpperBound(player, args[1]);
                            
                            item = getItemID(player, args[2]);
                            enchantments = getEnchantments(args[2]);
                            if (enchantments == null)
                                data = getData(player, args[2]);
                        }
                        
                        break;
                            
                    case 4: //3 of 5 fields
                        if (args[3].endsWith("%")) { //Percent field
                            percent = getPercent(player, args[3]);
                            
                            if (args[1].startsWith("coll")) { //AddID, Item, & Percent fields
                                id = getCollID(player, args[1]);
                                
                                item = getItemID(player, args[2]);
                                enchantments = getEnchantments(args[2]);
                                if (enchantments == null)
                                    data = getData(player, args[2]);
                            }
                            else if (args[2].startsWith("coll")) { //Name, AddID, & Percent fields
                                name = args[1];
                                
                                id = getCollID(player, args[2]);
                            }
                            else if (PhatLoots.hasPhatLoot(args[1])) { //Name, Item, & Percent fields
                                name = args[1];
                                
                                item = getItemID(player, args[2]);
                                enchantments = getEnchantments(args[2]);
                                if (enchantments == null)
                                    data = getData(player, args[2]);
                            }
                            else { //Amount, Item, & Percent fields
                                baseAmount = getLowerBound(player, args[1]);
                                bonusAmount = getUpperBound(player, args[1]);

                                item = getItemID(player, args[2]);
                                enchantments = getEnchantments(args[2]);
                                if (enchantments == null)
                                    data = getData(player, args[2]);
                            }
                        }
                        else if (args[1].startsWith("coll")) { //AddID, Amount, & Item fields
                            id = getCollID(player, args[1]);
                            
                            baseAmount = getLowerBound(player, args[2]);
                            bonusAmount = getUpperBound(player, args[2]);

                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                        }
                        else if (args[2].startsWith("coll")) { //Name, AddID, & Item fields
                            name = args[1];
                            
                            id = getCollID(player, args[2]);

                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                        }
                        else { //Name, Amount, & Item fields
                            name = args[1];
                            
                            baseAmount = getLowerBound(player, args[2]);
                            bonusAmount = getUpperBound(player, args[2]);

                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                        }
                        
                        break;
                        
                    case 5: //4 of 5 fields
                        if (!PhatLoots.hasPhatLoot(args[1])) { //AddID, Amount, Item, & Percent fields
                            id = getCollID(player, args[1]);
                            
                            baseAmount = getLowerBound(player, args[2]);
                            bonusAmount = getUpperBound(player, args[2]);

                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                            
                            percent = getPercent(player, args[4]);
                        }
                        else if (!args[4].endsWith("%")) { //Name, AddID, Amount, & Item fields
                            name = args[1];
                            
                            id = getCollID(player, args[2]);
                            
                            baseAmount = getLowerBound(player, args[3]);
                            bonusAmount = getUpperBound(player, args[3]);

                            item = getItemID(player, args[4]);
                            enchantments = getEnchantments(args[4]);
                            if (enchantments == null)
                                data = getData(player, args[4]);
                        }
                        else if (!args[2].startsWith("coll")) { //Name, Amount, Item, & Percent fields
                            name = args[1];
                            
                            baseAmount = getLowerBound(player, args[2]);
                            bonusAmount = getUpperBound(player, args[2]);

                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                            
                            percent = getPercent(player, args[4]);
                        }
                        else { //Name, AddID, Item, & Percent fields
                            name = args[1];
                            
                            id = getCollID(player, args[2]);
                            
                            item = getItemID(player, args[3]);
                            enchantments = getEnchantments(args[3]);
                            if (enchantments == null)
                                data = getData(player, args[3]);
                            
                            percent = getPercent(player, args[4]);
                        }
                        
                        break;
                        
                    case 6: //5 of 5 fields
                        name = args[1];
                        
                        id = getCollID(player, args[2]);

                        baseAmount = getLowerBound(player, args[3]);
                        bonusAmount = getUpperBound(player, args[3]);

                        item = getItemID(player, args[4]);
                        enchantments = getEnchantments(args[4]);
                        if (enchantments == null)
                            data = getData(player, args[4]);
                        
                        percent = getPercent(player, args[5]);
                        break;
                        
                    default: //Invalid number of fields
                        player.sendMessage("Invalid Number of arguments");
                        sendLootHelp(player);
                        
                        return true;
                }
                
                //Return if there was an error finding a field
                if (id == -1 || baseAmount == -1 || bonusAmount == -1 || item == -1 || data == -1 || percent == -1)
                    return true;
                
                //Construct itemStack from item, amount, and data
                //If the item is AIR (unset) uses the ItemStack in the Player's hand
                if (item == 0)
                    itemStack = player.getItemInHand();
                else {
                    itemStack = new ItemStack(item, baseAmount, data);
                    if (enchantments != null)
                        itemStack.addUnsafeEnchantments(enchantments);
                }
                
                //Contruct the Loot
                loot = new Loot(itemStack, bonusAmount - baseAmount, percent);
                
                setLoot(player, name, add, id, loot);
                return true;
                
            case MONEY:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 2: //Name is not provided
                        try {
                            setMoney(player, null, args[1]);
                            return true;
                        }
                        catch (Exception notInt) {
                            sendSetupHelp(player);
                            break;
                        }
                        
                    case 3: //Name is provided
                        try {
                            setMoney(player, args[1], args[2]);
                            return true;
                        }
                        catch (Exception notInt) {
                            sendSetupHelp(player);
                            break;
                        }
                        
                    default: break;
                }
                
                sendSetupHelp(player);
                return true;
                
            case EXP:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "make")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 2: //Name is not provided
                        setExp(player, null, args[1]);
                        return true;
                        
                    case 3: //Name is provided
                        setExp(player, args[1], args[2]);
                        return true;
                        
                    default: sendSetupHelp(player); return true;
                }
                
            case LIST:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "list")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                if (args.length == 1)
                    list(player);
                else
                    sendHelp(player);
                
                return true;
                
            case INFO:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "info")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 1: info(player, null); return true; //Name is not provided
                    case 2: info(player, args[1]); return true; //Name is provided
                    default: sendHelp(player); return true;
                }
                
            case RESET:
                //Cancel if the Player does not have permission to use the command
                if (!PhatLoots.hasPermission(player, "reset")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
                
                switch (args.length) {
                    case 1: reset(player, null); return true; //Name is not provided
                    case 2: reset(player, args[1]); return true; //Name is provided
                    default: sendHelp(player); return true;
                }
            
            case RL:
                if (args.length == 1)
                    PhatLoots.rl(player);
                else
                    sendHelp(player);
                
                return true;
                
            case HELP:
                if (args.length == 2) {
                    Help help;
        
                    try {
                        help = Help.valueOf(args[1].toUpperCase());
                    }
                    catch (Exception notEnum) {
                        sendHelp(player);
                        return true;
                    }
        
                    switch (help) {
                        case CREATE: sendCreateHelp(player); break;
                        case SETUP: sendSetupHelp(player); break;
                        case LOOT: sendLootHelp(player); break;
                    }
                }
                else
                    sendHelp(player);
                
                return true;
                
            default: sendHelp(player); return true;
        }
    }
    
    /**
     * Creates a new PhatLoot of the given name
     * 
     * @param player The Player creating the PhatLoot
     * @param name The name of the PhatLoot being created (must not already exist)
     */
    public static void make(Player player, String name) {
        //Cancel if the PhatLoot already exists
        if (PhatLoots.hasPhatLoot(name)) {
            player.sendMessage("A PhatLoots named "+name+" already exists.");
            return;
        }
        
        PhatLoots.addPhatLoot(new PhatLoot(name));
        player.sendMessage("PhatLoots "+name+" Made!");
    }
    
    /**
     * Links the target Block to the specified PhatLoot
     * 
     * @param player The Player linking the Block they are targeting
     * @param name The name of the PhatLoot the Block will be linked to
     */
    public static void link(Player player, String name) {
        //Cancel if the Player is not targeting a correct Block
        Block block = player.getTargetBlock(TRANSPARENT, 10);
        switch (block.getType()) {
            case CHEST:
                //Make the Chest unlockable if ChestLock is enabled
                if (setUnlockable && PhatLoots.pm.isPluginEnabled("ChestLock")) {
                    Safe safe = ChestLock.findSafe(block);
                    if (safe == null) {
                        safe = new Safe(player.getName(), block);
                        safe.lockable = false;
                        safe.locked = false;
                        
                        ChestLock.addSafe(safe);
                    }
                }
                
                break;
                
            case DISPENSER: break;
            
            default:
                player.sendMessage("You must target a Chest/Dispenser.");
                return;
        }
        
        //Cancel if the PhatLoot with the given name does not exist
        if (!PhatLoots.hasPhatLoot(name)) {
            player.sendMessage("PhatLoots "+name+" does not exsist.");
            return;
        }
        
        PhatLoot phatLoot = PhatLoots.getPhatLoot(name);
        
        phatLoot.addChest(block);
        player.sendMessage("Target Block has been linked to PhatLoot "+name+"!");
        phatLoot.save();
    }
    
    /**
     * Unlinks the target Block from the specified PhatLoots
     * 
     * @param player The Player unlinking the Block they are targeting
     */
    public static void unlink(Player player, String name) {
        Block block = player.getTargetBlock(TRANSPARENT, 10);
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.removeChest(block);
            player.sendMessage("Target Block has been unlinked from PhatLoot "+phatLoot.name+"!");
            phatLoot.save();
        }
    }
    
    /**
     * Modifies the reset time of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param days The amount of days
     * @param hours The amount of hours
     * @param minutes The amount of minutes
     * @param seconds The amount of seconds
     */
    public static void time(Player player, String name, int days, int hours, int minutes, int seconds) {
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.days = days;
            phatLoot.hours = hours;
            phatLoot.minutes = minutes;
            phatLoot.seconds = seconds;
            player.sendMessage("Reset time for PhatLoot "+phatLoot.name+" has been set to "+days+" days, "
                    +hours+" hours, "+minutes+" minutes, and "+seconds+" seconds.");

            phatLoot.save();
        }
    }
    
    /**
     * Modifies global of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param global The new value of global
     */
    public static void global(Player player, String name, boolean global) {
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            if (phatLoot.global != global) {
                phatLoot.global = global;
                phatLoot.reset(null);

                player.sendMessage("PhatLoot "+phatLoot.name+" has been set to "+
                        (global ? "global" : "individual")+" reset!");
            }
        }
    }
    
    /**
     * Modifies round of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param round The new value of round
     */
    public static void round(Player player, String name, boolean round) {
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.round = round;

            player.sendMessage("PhatLoot "+phatLoot.name+" has been set to "+
                    (round ? "" : "not")+"round down time!");
            phatLoot.save();
        }
    }
    
    /**
     * Adds/Removes a Loot to the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param add True the Loot will be added, false if it will be removed
     * @param lootID The id of the Loot, 0 for individual loots
     * @param loot The Loot that will be added/removed
     */
    public static void setLoot(Player player, String name, boolean add, int lootID, Loot loot) {
        String lootDescription = loot.toInfoString();
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            boolean done = false;
            //Try to find the Loot
            for (Loot tempLoot: phatLoot.loots[lootID])
                if (loot.equals(tempLoot)) {
                    /*The Loot was found*/
                    //Cancel if the Player is trying to duplicate the Loot
                    if (!add) {
                        phatLoot.loots[lootID].remove(loot);

                        //Display the appropriate message
                        if (lootID == 0) //Individual Loot
                            player.sendMessage(lootDescription+" removed as Loot for PhatLoot "+phatLoot.name+"!");
                        else //Collective Loot
                            player.sendMessage(lootDescription+" removed as Loot to coll"+lootID+", "
                                    +phatLoot.getPercentRemaining(lootID)+"% remaining");

                        phatLoot.save();
                    }
                    else
                        player.sendMessage(lootDescription+" is already Loot for PhatLoot "+phatLoot.name+"!");
                    
                    done = true;
                    break;
                }
            
            if (!done) {
                /*The Loot was not found*/
                //Cancel if the Loot is not present
                if (add) {
                    phatLoot.loots[lootID].add(loot);

                    //Display the appropriate message
                    if (lootID == 0) //Individual Loot
                        player.sendMessage(lootDescription+" added as Loot for PhatLoot "+phatLoot.name+"!");
                    else //Collective Loot
                        player.sendMessage(lootDescription+" added as Loot to coll"+lootID+", "
                                +phatLoot.getPercentRemaining(lootID)+"% remaining");

                    phatLoot.save();
                }
                else
                    player.sendMessage(lootDescription+" was not found as a Loot for PhatLoot "+phatLoot.name+"!");
            }
        }
    }
    
    /**
     * Sets the money range of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param amount The String of the range of the amount
     */
    public static void setMoney(Player player, String name, String amount) {
        int lower = getLowerBound(player, amount);
        int upper = getUpperBound(player, amount);
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.moneyLower = lower;
            phatLoot.moneyUpper = upper;

            player.sendMessage("Money for PhatLoot "+phatLoot.name+" set to "+
                    (lower == upper ? "" : "a range from "+lower+" to ")+upper);
            phatLoot.save();
        }
    }
    
    /**
     * Sets the experience range of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param amount The String of the range of the amount
     */
    public static void setExp(Player player, String name, String amount) {
        int lower = getLowerBound(player, amount);
        int upper = getUpperBound(player, amount);
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.expLower = lower;
            phatLoot.expUpper = upper;

            player.sendMessage("Experience for PhatLoot "+phatLoot.name+" set to "+
                    (lower == upper ? "" : "a range from "+lower+" to ")+upper);
            phatLoot.save();
        }
    }
    
    /**
     * Manages commands of the specified PhatLoot
     * 
     * @param player The Player modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param add True if the command is to be added
     * @param cmd The command to be added/removed
     */
    public static void setCommand(Player player, String name, boolean add, String cmd) {
        if (cmd.startsWith("/"))
            cmd = cmd.substring(1);
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            boolean done = false;
            //Try to find the cmd
            for (String string: phatLoot.commands)
                if (cmd.equals(string)) {
                    /*The Loot was found*/
                    //Cancel if the Player is trying to duplicate the cmd
                    if (!add) {
                        phatLoot.commands.remove(cmd);
                        player.sendMessage("'"+cmd+"' removed as a command for PhatLoot "+phatLoot.name+"!");
                        phatLoot.save();
                    }
                    else
                        player.sendMessage("'"+cmd+"' is already a command for PhatLoot "+phatLoot.name+"!");
                    
                    done = true;
                    break;
                }
            
            if (!done) {
                /*The Loot was not found*/
                //Cancel if the cmd is not present
                if (add) {
                    phatLoot.commands.add(cmd);
                    player.sendMessage("'"+cmd+"' added as a command for PhatLoot "+phatLoot.name+"!");
                    phatLoot.save();
                }
                else
                    player.sendMessage("'"+cmd+"' was not found as a command for PhatLoot "+phatLoot.name+"!");
            }
        }
    }
    
    /**
     * Displays a list of current PhatLoot
     * 
     * @param player The Player requesting the list
     */
    public static void list(Player player) {
        String list = "Current PhatLoots:  ";
        
        //Concat each PhatLoot
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots())
            list = list.concat(phatLoot.name+", ");
        
        player.sendMessage(list.substring(0, list.length() - 2));
    }
    
    /**
     * Displays the info of the specified PhatLoot
     * If a name is not provided, a list of PhatLoots linked to the target Block is displayed
     * 
     * @param player The Player requesting the info
     * @param name The name of the PhatLoot
     */
    public static void info(Player player, String name) {
        LinkedList<PhatLoot> phatLoots = getPhatLoots(player, name);
        switch (phatLoots.size()) {
            case 0: break;
                
            case 1: //Display information for the one PhatLoot
                PhatLoot phatLoot = phatLoots.getFirst();
                
                player.sendMessage("§2Name:§b "+phatLoot.name+" §2Global Reset:§b "+phatLoot.global+
                        " §2Round Down:§b "+phatLoot.round);
                player.sendMessage("§2Reset Time:§b "+phatLoot.days+" days, "+phatLoot.hours+
                        " hours, "+phatLoot.minutes+" minutes, and "+phatLoot.seconds+" seconds.");
                player.sendMessage("§2Money§b: "+phatLoot.moneyLower+"-"+phatLoot.moneyUpper+
                        " §2Experience§b: "+phatLoot.expLower+"-"+phatLoot.expUpper);
                player.sendMessage("§2# of collective loots:§b "+phatLoot.numberCollectiveLoots);

                //Display Individual Loots if not empty
                String loots = phatLoot.getLoots(0);
                if (!loots.isEmpty())
                    player.sendMessage("§2IndividualLoots§b: "+loots);

                //Display each Collective Loots that is not empty
                for (int i = 1; i <= 5; i++) {
                    loots = phatLoot.getLoots(i);
                    if (!loots.isEmpty())
                        player.sendMessage("§2Coll"+i+"§b: "+loots);
                }
                
                break;
                
            default: //List all PhatLoots
                String list = "Linked PhatLoots:  ";
        
                //Concat each PhatLoot
                for (PhatLoot pl: phatLoots)
                    list = list.concat(pl.name+", ");

                player.sendMessage(list.substring(0, list.length() - 2));
        }
    }
    
    /**
     * Reset the use times of the specified PhatLoot/PhatLootChest
     * If a name is not provided, the target PhatLootChest is reset
     * 
     * @param player The Player reseting the PhatLootChests
     * @param name The name of the PhatLoot
     */
    public static void reset(Player player, String name) {
        //Reset the target Button if a name was not provided
        if (name != null) {
            //Reset all Buttons in every PhatLoot if the name provided is 'all'
            if (name.equals("all")) {
                for (PhatLoot phatLoots: PhatLoots.getPhatLoots())
                    phatLoots.reset(null);

                player.sendMessage("All Chests in each PhatLoot have been reset.");
                return;
            }

            //Find the PhatLoot that will be reset using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Cancel if the PhatLoot does not exist
            if (!PhatLoots.hasPhatLoot(name)) {
                player.sendMessage("PhatLoot "+name+" does not exsist.");
                return;
            }

            //Reset all Buttons linked to the PhatLoot
            phatLoot.reset(null);

            player.sendMessage("All Chests in PhatLoot "+name+" have been reset.");
        }
        
        Block block = player.getTargetBlock(TRANSPARENT, 10);
        
        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.reset(block);
            player.sendMessage("Target Block has been reset.");
        }
    }
    
    /**
     * Displays the PhatLoots Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendHelp(Player player) {
        player.sendMessage("§e     PhatLoots Help Page:");
        player.sendMessage("§2/"+command+" [Name]§b Loot a virtual Chest for the given PhatLoot");
        player.sendMessage("§2/"+command+" list§b List all PhatLoots");
        player.sendMessage("§2/"+command+" info (Name)§b List info of PhatLoot");
        player.sendMessage("§2/"+command+" reset§b Reset looted times for target Block");
        player.sendMessage("§2/"+command+" reset [Name]§b Reset looted times for PhatLoot");
        player.sendMessage("§2/"+command+" reset all§b Reset looted times for all PhatLoots");
        player.sendMessage("§2/"+command+" help create§b Display PhatLoots Create Help Page");
        player.sendMessage("§2/"+command+" help setup§b Display PhatLoots Setup Help Page");
        player.sendMessage("§2/"+command+" help loot§b Display PhatLoots Manage Loot Help Page");
        player.sendMessage("§2/"+command+" rl§b Reload PhatLoots Plugin");
    }
    
    /**
     * Displays the PhatLoots Create Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendCreateHelp(Player player) {
        player.sendMessage("§e     PhatLoots Create Help Page:");
        player.sendMessage("§7If Name is not specified then all PhatLoots linked to the target Block will be affected");
        player.sendMessage("§2/"+command+" make [Name]§b Create PhatLoot with given name");
        player.sendMessage("§2/"+command+" link [Name]§b Link target Chest/Dispenser with PhatLoot");
        player.sendMessage("§2/"+command+" unlink (Name)§b Unlink target Block from PhatLoot");
        player.sendMessage("§2/"+command+" delete [Name]§b Delete PhatLoot");
    }
    
    /**
     * Displays the PhatLoots Setup Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendSetupHelp(Player player) {
        player.sendMessage("§e     PhatLoots Setup Help Page:");
        player.sendMessage("§7If Name is not specified then all PhatLoots linked to the target Block will be affected");
        player.sendMessage("§6Amount may be a number §4(100)§6 or range §4(100-500)");
        player.sendMessage("§2/"+command+" time (Name) [Days] [Hrs] [Mins] [Secs]§b Set cooldown time for PhatLoot");
        player.sendMessage("§2/"+command+" global (Name) true§b Set PhatLoot to a global cooldown");
        player.sendMessage("§2/"+command+" global (Name) false§b Set PhatLoot to an individual cooldown");
        player.sendMessage("§2/"+command+" round (Name) true§b Set PhatLoot to round down cooldown times (ex. Daily/Hourly loots)");
        player.sendMessage("§2/"+command+" round (Name) false§b Set PhatLoot not round down cooldown times");
        player.sendMessage("§2/"+command+" money (Name) [Amount]§b Set money range to be looted");
        player.sendMessage("§2/"+command+" exp (Name) [Amount]§b Set experience to be gained");
        player.sendMessage("§2/"+command+" add cmd (Name) /[Command]§b Add a Command that will be executed upon looting");
        player.sendMessage("§2/"+command+" remove cmd (Name) /[Command]§b Remove a Command that will be executed upon looting");
    }
    
    /**
     * Displays the PhatLoots Sign Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendLootHelp(Player player) {
        player.sendMessage("§e     PhatLoots Manage Loot Help Page:");
        player.sendMessage("§7If Name is not specified then all PhatLoots linked to the target Block will be affected");
        player.sendMessage("§6Amount may be a number §4(10)§6 or range §4(10-64)");
        player.sendMessage("§5If Amount is not specified then the amount will be 1");
        player.sendMessage("§6To add a data value to an item use §4:x§6 ex. §4wool:5§6 or §435:5");
        player.sendMessage("§3To add enchantments to an item use §4:enchantment§3 ex. §4bow:arrow_fire§3 or §4bow:arrow_fire&arrow_unlimited");
        player.sendMessage("§3Enchantment levels can be added as follows §4bow:arrow_fire(2)");
        player.sendMessage("§5If Item is not specified then the item you are holding will be used");
        player.sendMessage("§5If Percent is not specified then the percent will be 100%");
        player.sendMessage("§2/"+command+" add (Name) (coll[1-5]) (Amount) (Item) (x%)§b add item that may be looted");
        player.sendMessage("§2/"+command+" remove (Name) (coll[1-5]) (Amount) (Item) (x%)§b remove item that may be looted");
    }
    
    /**
     * Returns the a LinkedLsit of PhatLoots
     * If a name is provided then only the PhatLoot with the given name will be in the List
     * If no name is provided then each PhatLoot that is linked to the target Block will be in the List
     * 
     * @param player The Player targeting a Block
     * @param name The name of the PhatLoot to be found
     * @return The a LinkedLsit of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Player player, String name) {
        LinkedList<PhatLoot> phatLoots = new LinkedList<PhatLoot>();
        
        if (name != null) {
            //Find the PhatLoot using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);
            
            //Inform the Player if the PhatLoot does not exist
            if (phatLoot != null )
                phatLoots.add(phatLoot);
            else
                player.sendMessage("PhatLoot "+name+" does not exsist.");
        }
        else {
            //Cancel if the Player is not targeting a correct Block
            Block block = player.getTargetBlock(TRANSPARENT, 10);
            switch (block.getType()) {
                case DISPENSER:break;
                case CHEST: break;
                default:
                    player.sendMessage("You must target a Chest/Dispenser.");
                    return phatLoots;
            }
            
            phatLoots = PhatLoots.getPhatLoots(block);
            
            //Inform the Player if the Block is not linked to any PhatLoots
            if (phatLoots.isEmpty())
                player.sendMessage("Target Block is not linked to a PhatLoot");
        }
        
        return phatLoots;
    }
    
    /**
     * Retrieves an int value from the given string that starts with coll
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the id
     */
    public static int getCollID(Player player, String string) {
        if (string.length() != 5) {
            if (player != null)
                player.sendMessage("Must be written as coll1, coll2, coll3, coll4, or coll5.");
            return -1;
        }
        
        int id = 0;
        try {
            id = Integer.parseInt(string.substring(4));
        }
        catch (Exception ex) {
        }
        
        if (id < 1 || id > 5) {
            if (player != null)
                player.sendMessage("Must be written as coll1, coll2, coll3, coll4, or coll5.");
            return -1;
        }
        
        return id;
    }
    
    /**
     * Retrieves an int value from the given string
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the amount
     */
    public static int getLowerBound(Player player, String string) {
        if (string.contains("-"))
            string = string.substring(0, string.indexOf('-'));
        
        try {
            return Integer.parseInt(string);
        }
        catch (Exception notInt) {
            if (player != null)
                player.sendMessage(string+" is not a valid number");
            return -1;
        }
    }
    
    /**
     * Retrieves an int value from the given string
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the amount
     */
    public static int getUpperBound(Player player, String string) {
        if (string.contains("-"))
            string = string.substring(string.indexOf('-') + 1);
        
        try {
            return Integer.parseInt(string);
        }
        catch (Exception notInt) {
            if (player != null)
                player.sendMessage(string+" is not a valid number");
            return -1;
        }
    }
    
    /**
     * Retrieves an int value from the given string
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the item
     */
    public static int getItemID(Player player, String string) {
        if (string.contains(":"))
            string = string.substring(0, string.indexOf(':'));

        int id;
        try {
            id = Integer.parseInt(string);

            //Verify that the item id is valid
            if (Material.getMaterial(id) == null) {
                if (player != null)
                    player.sendMessage(string+" is not a valid item id");
                return -1;
            }
        }
        catch (Exception notInt) {
            try {
                id = Material.getMaterial(string.toUpperCase()).getId();
            }
            catch (Exception invalid) {
                if (player != null)
                    player.sendMessage(string+" is not a valid item");
                return -1;
            }
        }
        
        return id;
    }
    
    /**
     * Retrieves Enchantments from the given string
     * 
     * @param string The String that contains the item
     * @return The Enchantments of the item
     */
    public static Map<Enchantment, Integer> getEnchantments(String string) {
        if (!string.contains(":"))
            return null;
        
        string = string.substring(string.indexOf(':') + 1);
        Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();
        
        try {
            for (String split: string.split("&")) {
                Enchantment enchantment = null;
                int level = -1;

                if (split.contains("(")) {
                    int index = split.indexOf('(');
                    level = Integer.parseInt(split.substring(index + 1, split.length() - 1));
                    split = split.substring(0, index);
                }

                for (Enchantment enchant: Enchantment.values())
                    if (enchant.getName().equalsIgnoreCase(split))
                        enchantment = enchant;

                if (level < enchantment.getStartLevel())
                    level = enchantment.getStartLevel();

                enchantments.put(enchantment, level);
            }
        }
        catch (Exception notEnchantment) {
            return null;
        }
        
        return enchantments;
    }
    
    /**
     * Retrieves a short value from the given string
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the item
     */
    public static short getData(Player player, String string) {
        if (!string.contains(":"))
            return 0;
        
        string = string.substring(string.indexOf(':') + 1);
        short data;
        
        try {
            data = Short.parseShort(string);
        }
        catch (Exception notShort) {
            if (player != null)
                player.sendMessage(string+" is not a valid enchantment or data value");
            return -1;
        }
        
        return data;
    }
    
    /**
     * Retrieves a double value from the given string that ends with %
     * 
     * @param player The Player that will receive error messages
     * @param string The String that contains the percent
     */
    public static double getPercent(Player player, String string) {
        string = string.substring(0, string.length() - 1);
        double percent;

        try {
            percent = Double.parseDouble(string);
            if (percent < 0)
                if (player != null)
                    player.sendMessage("The percent cannot be below 0");
            if (percent > 100) {
                if (player != null)
                    player.sendMessage("The percent cannot be above 100");
            }
            else
                return percent;
        }
        catch (Exception notDouble) {
            if (player != null)
                player.sendMessage(string+" is not a valid number");
        }
        
        return -1;
    }
}
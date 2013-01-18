package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Safe;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Executes Player Commands
 *
 * @author Codisimus
 */
public class PhatLootsCommand implements CommandExecutor {
    private static enum Action {
        HELP, MAKE, DELETE, LINK, UNLINK, DESCRIPTION, TIME, GLOBAL,
        ROUND, ADD, REMOVE, MONEY, EXP, LIST, INFO, GIVE, RESET, RL
    }
    private static enum Help { CREATE, SETUP, LOOT }
    private static final HashSet<Byte> TRANSPARENT = Sets.newHashSet(
            (byte)0,   (byte)6,   (byte)8,   (byte)9,   (byte)10,  (byte)11,
            (byte)26,  (byte)27,  (byte)28,  (byte)30,  (byte)31,  (byte)32,
            (byte)37,  (byte)38,  (byte)39,  (byte)40,  (byte)44,  (byte)50,
            (byte)51,  (byte)53,  (byte)55,  (byte)59,  (byte)64,  (byte)63,
            (byte)65,  (byte)66,  (byte)67,  (byte)68,  (byte)69,  (byte)70,
            (byte)71,  (byte)72,  (byte)75,  (byte)76,  (byte)77,  (byte)78,
            (byte)85,  (byte)90,  (byte)92,  (byte)96,  (byte)101, (byte)102,
            (byte)104, (byte)105, (byte)106, (byte)107, (byte)108, (byte)109,
            (byte)111, (byte)113, (byte)114, (byte)115, (byte)117, (byte)126,
            (byte)127, (byte)131, (byte)132, (byte)139, (byte)140, (byte)141,
            (byte)142, (byte)144, (byte)145);
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
        //loot give command
        if (args.length > 0 && args[0].equals("give")) {
            if (args.length != 3) {
                if (sender instanceof Player) {
                    sendHelp((Player) sender);
                    return true;
                } else {
                    return false;
                }
            }

            //Cancel if the Player does not have the needed permission
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!PhatLoots.hasPermission(player, "give")) {
                    player.sendMessage(PhatLootsMessages.permission);
                    return true;
                }
            }

            Player player = PhatLoots.server.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§6" + args[1] + " §4is not online");
                return true;
            }

            PhatLoot phatLoot = PhatLoots.getPhatLoot(args[2]);
            if (phatLoot == null) {
                sender.sendMessage("§4PhatLoot §6" + args[2] + "§4 does not exist");
                return true;
            }

            Inventory inventory = PhatLoots.server.createInventory(player, 54, phatLoot.name);
            player.openInventory(inventory);
            phatLoot.getLoot(player, new PhatLootChest(player.getLocation().getBlock()), inventory);
            return true;
        }

        //Cancel if the command is not from a Player
        if (!(sender instanceof Player)) {
            if (args.length == 1 && args[0].equals("rl")) {
                PhatLoots.rl();
            }
            return false;
        }

        Player player = (Player) sender;

        //Display the help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        Action action;

        try {
            action = Action.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException notEnum) {
            //Cancel if the first argument is not a valid PhatLoot
            PhatLoot phatLoot = PhatLoots.getPhatLoot(args[0]);
            if (phatLoot == null) {
                player.sendMessage("§4PhatLoot §6" + args[0] + "§4 does not exist");
                return true;
            }

            //Cancel if the Player does not have the needed permission
            if (!PhatLoots.hasPermission(player, "commandloot")
                    || !(PhatLoots.canLoot(player, phatLoot))) {
                player.sendMessage(PhatLootsMessages.permission);
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

            if (args.length == 2) {
                make(player, args[1]);
            } else {
                sendCreateHelp(player);
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

                if (delete == null) {
                    player.sendMessage("§4PhatLoot §6" + args[0] + "§4 does not exist");
                } else {
                    PhatLoots.removePhatLoot(delete);
                    player.sendMessage("§5PhatLoot §6" + delete.name + "§5 was deleted!");
                }
            } else {
                sendCreateHelp(player);
            }

            return true;

        case LINK:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "make")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            if (args.length == 2) {
                link(player, args[1]);
            } else {
                sendCreateHelp(player);
            }

            return true;

        case UNLINK:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "make")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            switch (args.length) {
            case 1:
                unlink(player, null);
                break;
            case 2:
                unlink(player, args[1]);
                break;
            default:
                sendCreateHelp(player);
                break;
            }

            return true;

        case DESCRIPTION:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "make")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            if (args.length != 2) {
                sendCreateHelp(player);
                return true;
            }

            if (!args[1].matches("[a-zA-Z0-9]+")) {
                player.sendMessage("§4An item decription name may only contain letters and numbers");
                return true;
            }

            createDescription(player, args[1]);
            return true;

        case TIME:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "make")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            switch (args.length) {
            case 2:  //Name is not provided
                if (!args[1].equals("never")) {
                    break;
                }

                time(player, null, -1, -1, -1, -1);
                return true;

            case 3: //Name is provided
                if (!args[2].equals("never")) {
                    break;
                }

                time(player, args[1], -1, -1, -1, -1);
                return true;

            case 5: //Name is not provided
                try {
                    time(player, null, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                    return true;
                } catch (Exception notInt) {
                    break;
                }

            case 6: //Name is provided
                try {
                    time(player, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    return true;
                } catch (Exception notInt) {
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
                } catch (Exception notBool) {
                    break;
                }

            case 3: //Name is provided
                try {
                    global(player, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                } catch (Exception notBool) {
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
                } catch (Exception notBool) {
                    break;
                }

            case 3: //Name is provided
                try {
                    round(player, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                } catch (Exception notBool) {
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
            if (args.length < 2 ) {
                sendSetupHelp(player);
                return true;
            }

            if (args[1].equals("cmd")) {
                String name = null;
                String cmd = "";
                int i = 2;

                if (args.length > 2){
                    if (PhatLoots.hasPhatLoot(args[2])) {
                        name = args[2];
                        i++;
                    }
                }

                while (i < args.length) {
                    cmd = cmd.concat(args[i].concat(" "));
                    i++;
                }

                if (!cmd.isEmpty()) {
                    cmd = cmd.substring(0, cmd.length() - 1);
                }

                setCommand(player, name, add, cmd);
                return true;
            }

            String phatLoot = null; //Name of the PhatLoot (may be set to 'hand')
            String description = ""; //Name of the Description File (defaulted to null or unused)
            int id = 0; //The ID of the Loot collection (defaulted to 0 == IndividualLoots)
            int baseAmount = 1; //Stack size of the Loot item (defaulted to 1)
            int bonusAmount = 1; //Amount to possibly increase the Stack size of the Loot item (defaulted to 1)
            double percent = 100; //The chance of recieving the Loot item (defaulted to 100)

            ItemStack item = getItemStack(player, args[1]); //The Loot item
            if (item == null) {
                return true;
            }

            for (int i = 2; i < args.length; i++) {
                char c = args[i].charAt(0);
                String s = args[i].substring(1);
                switch (c) {
                case 'p':
                    phatLoot = s;
                    if (phatLoot == null) {
                        player.sendMessage("§4PhatLoot §6" + s + "§4 does not exist");
                        return true;
                    }
                    break;

                case 'c':
                    id = getCollID(s);
                    if (id == -1) {
                        player.sendMessage("§4Must be written as c1, c2, c3, c4, or c5.");
                        return true;
                    }
                    break;

                case '#':
                    baseAmount = getLowerBound(s);
                    bonusAmount = getUpperBound(s);
                    if (baseAmount == -1 || bonusAmount == -1) {
                        player.sendMessage("§6" + s + "§4 is not a valid number or range");
                        return true;
                    }
                    item.setAmount(baseAmount);
                    break;

                case '%':
                    percent = getPercent(player, s);
                    if (percent == -1) {
                        player.sendMessage("§6" + s + "§4 is not a percent");
                        return true;
                    }
                    break;
                case 'e':
                    Map<Enchantment, Integer> enchantments = getEnchantments(s);
                    if (enchantments == null) {
                        player.sendMessage("§6" + s + "§4 is not a valid enchantment");
                        return true;
                    }
                    item.addUnsafeEnchantments(enchantments);
                    break;

                case 'd':
                    short data = getData(s);
                    if (data == -1) {
                        player.sendMessage("§6" + s + "§4 is not a valid data/durability value");
                        return true;
                    }
                    item.setDurability(data);
                    break;

                case 'n':
                    if (!s.matches("[a-zA-Z0-9]+")) {
                        player.sendMessage("§4An item decription name may only contain letters and numbers");
                        return true;
                    }
                    description = s;
                    break;

                default:
                    player.sendMessage("§6" + s + "§4 is not a valid parameter");
                    return true;
                }
            }

            //Contruct the Loot
            Loot loot = new Loot(item, bonusAmount - baseAmount);
            loot.setProbability(percent);
            if (!loot.setName(description)) {
                player.sendMessage("§4You must create the §6"
                        + description + "§4 description file");
                createDescription(player, description);
            }

            setLoot(player, phatLoot, add, id, loot);
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
                } catch (Exception notInt) {
                    sendSetupHelp(player);
                    break;
                }

            case 3: //Name is provided
                try {
                    setMoney(player, args[1], args[2]);
                    return true;
                } catch (Exception notInt) {
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

            default:
                sendSetupHelp(player);
                return true;
            }

        case LIST:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "list")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            if (args.length == 1) {
                list(player);
            } else {
                sendHelp(player);
            }
            return true;

        case INFO:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "info")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            switch (args.length) {
            case 1: //Name is not provided
                info(player, null);
                return true;
            case 2: //Name is provided
                info(player, args[1]);
                return true;
            default:
                sendHelp(player);
                return true;
            }

        case RESET:
            //Cancel if the Player does not have permission to use the command
            if (!PhatLoots.hasPermission(player, "reset")) {
                player.sendMessage(PhatLootsMessages.permission);
                return true;
            }

            switch (args.length) {
            case 1: //Name is not provided
                reset(player, null);
                return true;
            case 2: //Name is provided
                reset(player, args[1]);
                return true;
            default:
                sendHelp(player);
                return true;
            }

        case RL:
            if (args.length == 1) {
                PhatLoots.rl(player);
            } else {
                sendHelp(player);
            }
            return true;

        case HELP:
            if (args.length == 2) {
                Help help;

                try {
                    help = Help.valueOf(args[1].toUpperCase());
                } catch (Exception notEnum) {
                    sendHelp(player);
                    return true;
                }

                switch (help) {
                case CREATE:
                    sendCreateHelp(player);
                    break;
                case SETUP:
                    sendSetupHelp(player);
                    break;
                case LOOT:
                    sendLootHelp(player);
                    break;
                }
            } else {
                sendHelp(player);
            }

            return true;

        default:
            sendHelp(player);
            return true;
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
            player.sendMessage("§4A PhatLoot named §6" + name + "§4 already exists.");
            return;
        }

        PhatLoots.addPhatLoot(new PhatLoot(name));
        player.sendMessage("§5PhatLoot §6" + name + "§5 Made!");
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
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();

            //Linked the left side if it is a DoubleChest
            if (inventory instanceof DoubleChestInventory) {
                chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                block = chest.getBlock();
            }
        case ENDER_CHEST:
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

        case DISPENSER:
            break;

        default:
            player.sendMessage("§4You must target a Chest/Dispenser.");
            return;
        }

        //Cancel if the PhatLoot with the given name does not exist
        if (!PhatLoots.hasPhatLoot(name)) {
            player.sendMessage("§4PhatLoot §6" + name + "§4 does not exsist.");
            return;
        }

        PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

        phatLoot.addChest(block);
        player.sendMessage("§5Target Block has been linked to PhatLoot §5" + name);
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
            player.sendMessage("§5Target Block has been unlinked from PhatLoot §6" + phatLoot.name);
            phatLoot.save();
        }
    }

    /**
     * Initiates the creation of a new Item Description File
     *
     * @param player The Player creating the Description
     * @param name The Name of the new Description File
     */
    public static void createDescription(Player player, String name) {
        File file = new File(PhatLoots.dataFolder + "/Item Descriptions/" + name + ".txt");
        if (file.exists()) {
            player.sendMessage("§4The Item Description §6" + name + "§4 already exists");
        } else {
            PhatLootsListener.descriptionFiles.put(player, file);
            player.sendMessage("§5Please type the Item Name as you wish it to appear");
            player.sendMessage("§5Do not start the line with §6/§5 and use §6&§5 to add color");
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
            player.sendMessage("§5Reset time for PhatLoot §6" + phatLoot.name
                    + "§5 has been set to §6" + days + " days, "
                    + hours + " hours, " + minutes + " minutes, and "
                    + seconds + " seconds");

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

                player.sendMessage("§5PhatLoot §6" + phatLoot.name + "§5 has been set to §6"
                        + (global ? "global" : "individual") + "§5 reset");
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

            player.sendMessage("§5PhatLoot §6" + phatLoot.name + "§5 has been set to §6"
                    + (round ? "" : "not") + "round down time");
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
            for (Loot tempLoot: phatLoot.loots[lootID]) {
                if (loot.equals(tempLoot)) {
                    /*The Loot was found*/
                    //Cancel if the Player is trying to duplicate the Loot
                    if (!add) {
                        phatLoot.loots[lootID].remove(loot);

                        //Display the appropriate message
                        if (lootID == 0)  { //Individual Loot
                            player.sendMessage("§6" + lootDescription
                                    + "§5 removed as Loot for PhatLoot §6"
                                    + phatLoot.name);
                        } else { //Collective Loot
                            player.sendMessage("§6" + lootDescription
                                    + "§5 removed as Loot from §6coll"
                                    + lootID + "§5, §6"
                                    + phatLoot.getPercentRemaining(lootID)
                                    + "%§5 remaining");
                        }

                        phatLoot.save();
                    } else {
                        player.sendMessage("§6" + lootDescription
                                + "§4 is already Loot for PhatLoot §6"
                                + phatLoot.name);
                    }

                    done = true;
                    break;
                }
            }

            if (!done) {
                /*The Loot was not found*/
                //Cancel if the Loot is not present
                if (add) {
                    phatLoot.loots[lootID].add(loot);

                    //Display the appropriate message
                    if (lootID == 0) { //Individual Loot
                        player.sendMessage("§6" + lootDescription
                                + "§5 added as Loot for PhatLoot §6"
                                + phatLoot.name);
                    } else { //Collective Loot
                        player.sendMessage("§6" + lootDescription
                                + "§5 added as Loot to §6coll"
                                + lootID + "§5, §6"
                                + phatLoot.getPercentRemaining(lootID)
                                + "%§5 remaining");
                    }

                    phatLoot.save();
                } else {
                    player.sendMessage("§6" + lootDescription
                            + "§4 was not found as a Loot for PhatLoot §6"
                            + phatLoot.name);
                }
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
        int lower = getLowerBound(amount);
        int upper = getUpperBound(amount);
        if (lower == -1 || upper == -1) {
            player.sendMessage("§6" + amount + " §4is not a valid number or range");
        }

        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.moneyLower = lower;
            phatLoot.moneyUpper = upper;

            player.sendMessage("§5Money for PhatLoot §6"
                    + phatLoot.name + "§5 set to "
                    + (lower == upper
                       ? "§6"
                       : "a range from §6" + lower + "§5 to §6")
                    + upper);
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
        int lower = getLowerBound(amount);
        int upper = getUpperBound(amount);
        if (lower == -1 || upper == -1) {
            player.sendMessage("§6" + amount + " §4is not a valid number or range");
        }

        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.expLower = lower;
            phatLoot.expUpper = upper;

            player.sendMessage("§5Experience for PhatLoot §6"
                    + phatLoot.name + "§5 set to "
                    + (lower == upper
                       ? "§6"
                       : "a range from §6" + lower + "§5 to §6")
                    + upper);
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
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }

        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            boolean done = false;
            //Try to find the cmd
            for (String string: phatLoot.commands) {
                if (cmd.equals(string)) {
                    /*The Loot was found*/
                    //Cancel if the Player is trying to duplicate the cmd
                    if (!add) {
                        phatLoot.commands.remove(cmd);
                        player.sendMessage("§6" + cmd
                                + "§5 removed as a command for PhatLoot §6"
                                + phatLoot.name);
                        phatLoot.save();
                    } else {
                        player.sendMessage("§6" + cmd
                                + "§4 is already a command for PhatLoot §6"
                                + phatLoot.name);
                    }

                    done = true;
                    break;
                }
            }

            if (!done) {
                /*The Loot was not found*/
                //Cancel if the cmd is not present
                if (add) {
                    phatLoot.commands.add(cmd);
                    player.sendMessage("§6" + cmd
                            + "§5 added as a command for PhatLoot §6"
                            + phatLoot.name);
                    phatLoot.save();
                } else {
                    player.sendMessage("§6" + cmd
                            + "§4 was not found as a command for PhatLoot §6"
                            + phatLoot.name);
                }
            }
        }
    }

    /**
     * Displays a list of current PhatLoot
     *
     * @param player The Player requesting the list
     */
    public static void list(Player player) {
        String list = "§5Current PhatLoots: §6";

        //Concat each PhatLoot
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            list += phatLoot.name + ", ";
        }

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
        case 0:
            break;

        case 1: //Display information for the one PhatLoot
            PhatLoot phatLoot = phatLoots.getFirst();

            player.sendMessage("§2Name:§b " + phatLoot.name
                    + " §2Global Reset:§b " + phatLoot.global
                    + " §2Round Down:§b " + phatLoot.round);
            player.sendMessage("§2Reset Time:§b " + phatLoot.days
                    + " days, " + phatLoot.hours + " hours, "
                    + phatLoot.minutes + " minutes, and "
                    + phatLoot.seconds + " seconds.");
            player.sendMessage("§2Money§b: " + phatLoot.moneyLower + "-"
                    + phatLoot.moneyUpper + " §2Experience§b: "
                    + phatLoot.expLower + "-" + phatLoot.expUpper);
            player.sendMessage("§2# of collective loots:§b "
                    + phatLoot.numberCollectiveLoots);

            //Display Individual Loots if not empty
            String loots = phatLoot.getLoots(0);
            if (!loots.isEmpty()) {
                player.sendMessage("§2IndividualLoots§b: " + loots);
            }

            //Display each Collective Loots that is not empty
            for (int i = 1; i <= 5; i++) {
                loots = phatLoot.getLoots(i);
                if (!loots.isEmpty()) {
                    player.sendMessage("§2Coll" + i + "§b: " + loots);
                }
            }

            break;

        default: //List all PhatLoots
            String list = "§5Linked PhatLoots: §6";

            //Concat each PhatLoot
            for (PhatLoot pl: phatLoots) {
                list += pl.name + ", ";
            }

            player.sendMessage(list.substring(0, list.length() - 2));
            break;
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
                for (PhatLoot phatLoots: PhatLoots.getPhatLoots()) {
                    phatLoots.reset(null);
                }

                player.sendMessage("§5All Chests in each PhatLoot have been reset.");
                return;
            }

            //Find the PhatLoot that will be reset using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Cancel if the PhatLoot does not exist
            if (!PhatLoots.hasPhatLoot(name)) {
                player.sendMessage("§4PhatLoot §6" + name + "§4 does not exsist.");
                return;
            }

            //Reset all Chests linked to the PhatLoot
            phatLoot.reset(null);

            player.sendMessage("§5All Chests in PhatLoot §6"
                    + name + "§5 have been reset.");
        }

        Block block = player.getTargetBlock(TRANSPARENT, 10);

        for (PhatLoot phatLoot: getPhatLoots(player, name)) {
            phatLoot.reset(block);
            player.sendMessage("§5Target Block has been reset.");
        }
    }

    /**
     * Displays the PhatLoots Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendHelp(Player player) {
        player.sendMessage("§e     PhatLoots Help Page:");
        player.sendMessage("§2/"+command+" <Name>§b Loot a virtual Chest for the given PhatLoot");
        player.sendMessage("§2/"+command+" list§b List all PhatLoots");
        player.sendMessage("§2/"+command+" info [Name]§b List info of PhatLoot");
        player.sendMessage("§2/"+command+" give <PhatLoot> <Name>§b Force Player to open a PhatLoot");
        player.sendMessage("§2/"+command+" reset§b Reset looted times for target Block");
        player.sendMessage("§2/"+command+" reset <Name>§b Reset looted times for PhatLoot");
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
        player.sendMessage("§2/"+command+" make <Name>§b Create PhatLoot with given name");
        player.sendMessage("§2/"+command+" delete <Name>§b Delete PhatLoot");
        player.sendMessage("§2/"+command+" link <Name>§b Link target Chest/Dispenser with PhatLoot");
        player.sendMessage("§2/"+command+" unlink [Name]§b Unlink target Block from PhatLoot");
        player.sendMessage("§2/"+command+" description <Name>§b Create a new Item Description");
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
        player.sendMessage("§2/"+command+" time [Name] <Days> <Hrs> <Mins> <Secs>§b Set cooldown time for PhatLoot");
        player.sendMessage("§2/"+command+" global [Name] true§b Set PhatLoot to a global cooldown");
        player.sendMessage("§2/"+command+" global [Name] false§b Set PhatLoot to an individual cooldown");
        player.sendMessage("§2/"+command+" round [Name] <true|false>§b Set if cooldown times should round down (ex. Daily/Hourly loots)");
        player.sendMessage("§2/"+command+" money [Name] <Amount>§b Set money range to be looted");
        player.sendMessage("§2/"+command+" exp [Name] <Amount>§b Set experience to be gained");
        player.sendMessage("§2/"+command+" add cmd [Name] /<Command>§b Add a Command that will be executed upon looting");
        player.sendMessage("§2/"+command+" remove cmd [Name] /<Command>§b Remove a Command that will be executed upon looting");
    }

    /**
     * Displays the PhatLoots Sign Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendLootHelp(Player player) {
        player.sendMessage("§e     PhatLoots Manage Loot Help Page:");
        player.sendMessage("§5A Parameter starts with the 1 character §2id");
        player.sendMessage("§2p§f: §5The Name of the PhatLoot ex. §6pEpic");
        player.sendMessage("§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected");
        player.sendMessage("§2d§f: §5The data/durability value of the item ex. §6d5");
        player.sendMessage("§2c§f: §5The id of the collective loot to specify ex. §6c1");
        player.sendMessage("§2#§f: §5The amount of the item ex. §6#10 §5or §6#1-64 §5(default: §61§5)");
        player.sendMessage("§2%§f: §5The chance of looting the item ex. §6%50 §5or §6%0.1 §5(default: §6100§5)");
        player.sendMessage("§2n§f: §5The Name of the item description ex. §6nSuperSword");
        player.sendMessage("§2e§f: §5The item enchantment ex. §6earrow_fire");
        player.sendMessage("§bEnchantment levels can be added. ex. §6arrow_fire(2)");
        player.sendMessage("§2/"+command+" <add|remove> <Item|ID|hand> [Parameter1] [Parameter2]...");
        player.sendMessage("§bex. §6/"+command+" add hand #1-16 nEnderNade %32");
        player.sendMessage("§bex. §6/"+command+" add bow earrow_fire(2) earrow_unlimited %5");
        player.sendMessage("§bTutorial Video:");
        player.sendMessage("§1§nwww.youtu.be/tRQuKbRTaA4");
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
            if (phatLoot != null ) {
                phatLoots.add(phatLoot);
            } else {
                player.sendMessage("§4PhatLoot §6" + name + "§4 does not exist.");
            }
        } else {
            //Cancel if the Player is not targeting a correct Block
            Block block = player.getTargetBlock(TRANSPARENT, 10);
            switch (block.getType()) {
            case DISPENSER: break;
            case ENDER_CHEST: break;
            case CHEST: break;
            default:
                player.sendMessage("§4You must target a Chest/Dispenser.");
                return phatLoots;
            }

            phatLoots = PhatLoots.getPhatLoots(block);

            //Inform the Player if the Block is not linked to any PhatLoots
            if (phatLoots.isEmpty()) {
                player.sendMessage("§4Target Block is not linked to a PhatLoot");
            }
        }

        return phatLoots;
    }

    /**
     * Retrieves an int value from the given string that starts with coll
     *
     * @param player The Player that will receive error messages
     * @param string The String that contains the id
     */
    public static int getCollID(String string) {
        int id = 0;
        try {
            id = Integer.parseInt(string);
        } catch (Exception ex) {
        }

        if (id < 1 || id > 5) {
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
    public static int getLowerBound(String string) {
        if (string.contains("-")) {
            string = string.substring(0, string.indexOf('-'));
        }

        try {
            return Integer.parseInt(string);
        } catch (Exception notInt) {
            return -1;
        }
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param player The Player that will receive error messages
     * @param string The String that contains the amount
     */
    public static int getUpperBound(String string) {
        if (string.contains("-")) {
            string = string.substring(string.indexOf('-') + 1);
        }

        try {
            return Integer.parseInt(string);
        } catch (Exception notInt) {
            return -1;
        }
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param player The Player that will receive error messages
     * @param string The String that contains the item
     */
    public static ItemStack getItemStack(Player player, String string) {
        if (string.equals("hand")) {
            return player.getItemInHand().clone();
        }

        Material material = null;
        if (string.matches("[0-9]+")) {
            int id = Integer.parseInt(string);
            material = Material.getMaterial(id);
        } else {
            //Verify that the item id is valid
            material = Material.getMaterial(string.toUpperCase());
        }

        //Verify that the item is valid
        if (material == null) {
            if (player != null) {
                player.sendMessage("§6" + string + "§4 is not a valid item id");
            }
            return null;
        }

        return new ItemStack(material);
    }

    /**
     * Retrieves Enchantments from the given string
     *
     * @param string The String that contains the item
     * @return The Enchantments of the item
     */
    public static Map<Enchantment, Integer> getEnchantments(String string) {
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

                for (Enchantment enchant: Enchantment.values()) {
                    if (enchant.getName().equalsIgnoreCase(split)) {
                        enchantment = enchant;
                    }
                }

                if (level < enchantment.getStartLevel()) {
                    level = enchantment.getStartLevel();
                }

                enchantments.put(enchantment, level);
            }
        } catch (Exception notEnchantment) {
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
    public static short getData(String string) {
        short data;
        try {
            data = Short.parseShort(string);
        } catch (Exception notShort) {
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
        double percent;
        try {
            percent = Double.parseDouble(string);
            if (percent < 0) {
                if (player != null) {
                    player.sendMessage("§4The percent cannot be below 0");
                }
            }
            if (percent > 100) {
                if (player != null) {
                    player.sendMessage("§4The percent cannot be above 100");
                }
            } else {
                return percent;
            }
        } catch (Exception notDouble) {
            if (player != null) {
                player.sendMessage("§6" + string + "§4 is not a valid number");
            }
        }
        return -1;
    }
}

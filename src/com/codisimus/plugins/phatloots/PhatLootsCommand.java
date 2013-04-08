package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Safe;
import com.codisimus.plugins.regionown.Region;
import com.codisimus.plugins.regionown.RegionSelector;
import com.google.common.collect.Sets;
import java.util.*;
import org.bukkit.ChatColor;
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
 * Executes Sender Commands
 *
 * @author Codisimus
 */
public class PhatLootsCommand implements CommandExecutor {
    private static enum Action {
        HELP, MAKE, DELETE, LINK, UNLINK, TIME, GLOBAL, AUTOLOOT, ROUND,
        ADD, REMOVE, COST, MONEY, EXP, LIST, INFO, GIVE, RESET, CLEAN, RL
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
        //Display the help page if the sender did not add any arguments
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        Action action;

        try {
            action = Action.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException notEnum) {
            //Cancel if the first argument is not a valid PhatLoot
            PhatLoot phatLoot = PhatLoots.getPhatLoot(args[0]);
            if (phatLoot == null) {
                sender.sendMessage("§4PhatLoot §6" + args[0] + "§4 does not exist");
                return true;
            }

            //Cancel if the sender does not have the needed permission
            if (sender instanceof Player) {
            	if (!sender.hasPermission("phatloots.commandloot")
            			|| !(PhatLoots.canLoot((Player) sender, phatLoot))) {
                    sender.sendMessage(PhatLootsConfig.permission);
            	    return true;
            	}
            	Inventory inventory = PhatLoots.server.createInventory((Player) sender, 54, phatLoot.name);

            	//Open the Inventory
            	((Player) sender).openInventory(inventory);

            	phatLoot.rollForLoot((Player) sender, new PhatLootChest(((Player) sender).getLocation().getBlock()), inventory);
            }
            return true;
        }

        //Execute the correct command
        switch (action) {
        case MAKE:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            if (args.length == 2) {
                make(sender, args[1]);
            } else {
                sendCreateHelp(sender);
            }

            return true;

        case DELETE:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            if (args.length == 2) {
                PhatLoot delete = PhatLoots.getPhatLoot(args[1]);

                if (delete == null) {
                    sender.sendMessage("§4PhatLoot §6" + args[1] + "§4 does not exist");
                } else {
                    PhatLoots.removePhatLoot(delete);
                    sender.sendMessage("§5PhatLoot §6" + delete.name + "§5 was deleted!");
                }
            } else {
                sendCreateHelp(sender);
            }

            return true;

        case LINK:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            if (args.length == 2) {
                link(sender, args[1]);
            } else {
                sendCreateHelp(sender);
            }

            return true;

        case UNLINK:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 1:
                unlink(sender, null);
                break;
            case 2:
                unlink(sender, args[1]);
                break;
            default:
                sendCreateHelp(sender);
                break;
            }

            return true;

        case TIME:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }


            switch (args.length) {
            case 2:  //Name is not provided
                if (!args[1].equals("never")) {
                    break;
                }

                time(sender, null, -1, -1, -1, -1);
                return true;

            case 3: //Name is provided
                if (!args[2].equals("never")) {
                    break;
                }

                time(sender, args[1], -1, -1, -1, -1);
                return true;

            case 5: //Name is not provided
                try {
                    time(sender, null, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                    return true;
                } catch (Exception notInt) {
                    break;
                }

            case 6: //Name is provided
                try {
                    time(sender, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    return true;
                } catch (Exception notInt) {
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case GLOBAL:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                try {
                    global(sender, null, Boolean.parseBoolean(args[1]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            case 3: //Name is provided
                try {
                    global(sender, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case AUTOLOOT:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                try {
                    autoLoot(sender, null, Boolean.parseBoolean(args[1]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            case 3: //Name is provided
                try {
                    autoLoot(sender, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case ROUND:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                try {
                    round(sender, null, Boolean.parseBoolean(args[1]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            case 3: //Name is provided
                try {
                    round(sender, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                } catch (Exception notBool) {
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case REMOVE: //Fall through
        case ADD:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            boolean add = action.equals(Action.ADD);
            if (args.length < 2 ) {
                sendSetupHelp(sender);
                return true;
            }

            if (args[1].equals("cmd")) {
                String name = null;
                double percent = 100;
                String cmd = "";
                int i = 2;

                if (args.length > i) {
                    if (PhatLoots.hasPhatLoot(args[i])) {
                        name = args[i];
                        i++;
                    }
                    if (args.length > i) {
                        if (args[i].matches("%[0-9]*[.]?[0-9]+")) {
                            percent = Double.parseDouble(args[i]);
                            i++;
                        }
                    }
                }

                while (i < args.length) {
                    cmd += args[i]+ " ";
                    i++;
                }

                if (!cmd.isEmpty()) {
                    cmd = cmd.substring(0, cmd.length() - 1);
                }

                setCommand(sender, name, percent, add, cmd);
                return true;
            }

            String phatLoot = null; //Name of the PhatLoot (may be set to 'hand')
            int id = 0; //The ID of the Loot collection (defaulted to 0 == IndividualLoots)
            int baseAmount = 1; //Stack size of the Loot item (defaulted to 1)
            int bonusAmount = 1; //Amount to possibly increase the Stack size of the Loot item (defaulted to 1)
            double percent = 100; //The chance of receiving the Loot item (defaulted to 100)
            boolean autoEnchant = false; //Whether or not the Loot Item should be automatically enchanted at time of Looting

            ItemStack item = getItemStack(sender, args[1]); //The Loot item
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
                        sender.sendMessage("§4PhatLoot §6" + s + "§4 does not exist");
                        return true;
                    }
                    break;

                case 'c':
                    id = getCollID(s);
                    if (id == -1) {
                        sender.sendMessage("§4Must be written as c1, c2, c3, c4, or c5.");
                        return true;
                    }
                    break;

                case '#':
                    baseAmount = getLowerBound(s);
                    bonusAmount = getUpperBound(s);
                    if (baseAmount == -1 || bonusAmount == -1) {
                        sender.sendMessage("§6" + s + "§4 is not a valid number or range");
                        return true;
                    }
                    item.setAmount(baseAmount);
                    break;

                case '%':
                    percent = getPercent(sender, s);
                    if (percent == -1) {
                        sender.sendMessage("§6" + s + "§4 is not a percent");
                        return true;
                    }
                    break;

                case 'e':
                    if (s.equalsIgnoreCase("auto")) {
                        autoEnchant = true;
                    } else {
                        Map<Enchantment, Integer> enchantments = getEnchantments(s);
                        if (enchantments == null) {
                            sender.sendMessage("§6" + s + "§4 is not a valid enchantment");
                            return true;
                        }
                        item.addUnsafeEnchantments(enchantments);
                    }
                    break;

                case 'd':
                    short data = getData(s);
                    if (data == -1) {
                        sender.sendMessage("§6" + s + "§4 is not a valid data/durability value");
                        return true;
                    }
                    item.setDurability(data);
                    break;

                default:
                    sender.sendMessage("§6" + s + "§4 is not a valid parameter");
                    return true;
                }
            }

            //Construct the Loot
            OldLoot loot = new OldLoot(item, bonusAmount - baseAmount);
            loot.setProbability(percent);
            if (autoEnchant) {
                loot.autoEnchant = true;
            }

            setLoot(sender, phatLoot, add, id, loot);
            return true;

        case COST:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                try {
                    setCost(sender, null, Integer.parseInt(args[1]));
                    return true;
                } catch (Exception notInt) {
                    sendSetupHelp(sender);
                    break;
                }

            case 3: //Name is provided
                try {
                    setCost(sender, args[1], Integer.parseInt(args[2]));
                    return true;
                } catch (Exception notInt) {
                    sendSetupHelp(sender);
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case MONEY:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                try {
                    setMoney(sender, null, args[1]);
                    return true;
                } catch (Exception notInt) {
                    sendSetupHelp(sender);
                    break;
                }

            case 3: //Name is provided
                try {
                    setMoney(sender, args[1], args[2]);
                    return true;
                } catch (Exception notInt) {
                    sendSetupHelp(sender);
                    break;
                }

            default: break;
            }

            sendSetupHelp(sender);
            return true;

        case EXP:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.make")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 2: //Name is not provided
                setExp(sender, null, args[1]);
                return true;

            case 3: //Name is provided
                setExp(sender, args[1], args[2]);
                return true;

            default:
                sendSetupHelp(sender);
                return true;
            }

        case LIST:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.list")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            if (args.length == 1) {
                list(sender);
            } else {
                sendHelp(sender);
            }
            return true;

        case INFO:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.info")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 1: //Name is not provided
                info(sender, null);
                return true;
            case 2: //Name is provided
                info(sender, args[1]);
                return true;
            default:
                sendHelp(sender);
                return true;
            }

        case RESET:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.reset")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 1: //Name is not provided
                reset(sender, null);
                return true;
            case 2: //Name is provided
                reset(sender, args[1]);
                return true;
            default:
                sendHelp(sender);
                return true;
            }

        case CLEAN:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.clean")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            switch (args.length) {
            case 1: //Name is not provided
                clean(sender, null);
                return true;
            case 2: //Name is provided
                clean(sender, args[1]);
                return true;
            default:
                sendHelp(sender);
                return true;
            }

        case GIVE:
            if (args.length < 3) {
                if (sender instanceof Player) {
                    sendHelp(sender);
                    return true;
                } else {
                    return false;
                }
            }

            //Cancel if the sender does not have the needed permission
            if (sender instanceof Player) {
                if (!sender.hasPermission("phatloots.give")) {
                    sender.sendMessage(PhatLootsConfig.permission);
                    return true;
                }
            }

            Player player = PhatLoots.server.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§6" + args[1] + " §4is not online");
                return true;
            }

            PhatLoot pLoot = PhatLoots.getPhatLoot(args[2]);
            if (pLoot == null) {
                sender.sendMessage("§4PhatLoot §6" + args[2] + "§4 does not exist");
                return true;
            }

            String name = args.length == 3
                          ? pLoot.name
                          : concatArgs(args, 3);
            name = ChatColor.translateAlternateColorCodes('&', name);

            Inventory inventory = PhatLoots.server.createInventory(player, 54, name);
            player.openInventory(inventory);
            pLoot.rollForLoot(player, new PhatLootChest(player.getLocation().getBlock()), inventory);
            return true;

        case RL:
            //Cancel if the sender does not have permission to use the command
            if (!sender.hasPermission("phatloots.rl")) {
                sender.sendMessage(PhatLootsConfig.permission);
                return true;
            }

            if (args.length == 1) {
                PhatLoots.rl(sender);
            } else {
                sendHelp(sender);
            }
            return true;

        case HELP:
            if (args.length == 2) {
                Help help;

                try {
                    help = Help.valueOf(args[1].toUpperCase());
                } catch (Exception notEnum) {
                    sendHelp(sender);
                    return true;
                }

                switch (help) {
                case CREATE:
                    sendCreateHelp(sender);
                    break;
                case SETUP:
                    sendSetupHelp(sender);
                    break;
                case LOOT:
                    sendLootHelp(sender);
                    break;
                }
            } else {
                sendHelp(sender);
            }

            return true;

        default:
            sendHelp(sender);
            return true;
        }
    }

    /**
     * Creates a new PhatLoot of the given name
     *
     * @param sender The CommandSender creating the PhatLoot
     * @param name The name of the PhatLoot being created (must not already exist)
     */
    public static void make(CommandSender sender, String name) {
        //Cancel if the PhatLoot already exists
        if (PhatLoots.hasPhatLoot(name)) {
            sender.sendMessage("§4A PhatLoot named §6" + name + "§4 already exists.");
            return;
        }

        PhatLoots.addPhatLoot(new PhatLoot(name));
        sender.sendMessage("§5PhatLoot §6" + name + "§5 made!");
    }

    /**
     * Links the target Block to the specified PhatLoot
     *
     * @param sender The CommandSender linking the Block they are targeting
     * @param name The name of the PhatLoot the Block will be linked to
     */
    public static void link(CommandSender sender, String name) {
    	//Cancel if the sender is console
    	if (!(sender instanceof Player)) {
            sender.sendMessage("§4You cannot do this from the console!");
            return;
    	}

        //Cancel if the sender is not targeting a correct Block
        Block block  = ((Player) sender).getTargetBlock(TRANSPARENT, 10);
        String blockName = "Block";
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
                    safe = new Safe(sender.getName(), block);
                    safe.lockable = false;
                    safe.locked = false;

                    ChestLock.addSafe(safe);
                }
            }
            blockName = "Chest";
            break;

        case DISPENSER:
        	blockName = "Dispenser";
            break;

        default:
            sender.sendMessage("§4You must target a Chest/Dispenser.");
            return;
        }

        //Cancel if the PhatLoot with the given name does not exist
        if (!PhatLoots.hasPhatLoot(name)) {
            sender.sendMessage("§4PhatLoot §6" + name + "§4 does not exist.");
            return;
        }

        PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

        phatLoot.addChest(block);
        sender.sendMessage("§5Target " + blockName + " has been linked to PhatLoot §6" + name);
        phatLoot.saveChests();
    }

    /**
     * Finds all Chests within the selected RegionOwn Region and links them to the specified PhatLoot
     *
     * @param player The Player who has a Region selected
     * @param name The name of the PhatLoot the Chests will be linked to
     */
    public static void regionLink(Player player, String name) {
        if (!PhatLoots.pm.isPluginEnabled("RegionOwn")) {
            player.sendMessage("You must install RegionOwn to use that command");
            return;
        }

        //Cancel if the PhatLoot with the given name does not exist
        if (!PhatLoots.hasPhatLoot(name)) {
            player.sendMessage("§4PhatLoot §6" + name + "§4 does not exist.");
            return;
        }

        PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

        if (RegionSelector.isSelecting(player)) {
            RegionSelector.endSelection(player);
        }

        if (!RegionSelector.hasSelection(player)) {
            player.sendMessage("You must first select a Region");
            return;
        }

        Region region = RegionSelector.getSelection(player);
        int chests = 0;

        for (Block block : region.getBlocks()) {
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();

                //Linked the left side if it is a DoubleChest
                if (inventory instanceof DoubleChestInventory) {
                    chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                    block = chest.getBlock();
                }

                phatLoot.addChest(block);
                chests++;
            }
        }

        player.sendMessage("§6" + chests + "§5 chests have been linked to PhatLoot §5" + name);
        phatLoot.saveChests();
    }

    /**
     * Unlinks the target Block from the specified PhatLoots
     *
     * @param sender The CommandSender unlinking the Block they are targeting
     */
    public static void unlink(CommandSender sender, String name) {
    	//Cancel if the sender is console
    	if (!(sender instanceof Player)) {
            sender.sendMessage("§4You cannot do this from the console!");
            return;
    	}

        Block block = ((Player) sender).getTargetBlock(TRANSPARENT, 10);
        String blockName = "Block";
        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.removeChest(block);
            switch (block.getType()) {
            case CHEST:
            case ENDER_CHEST:
            	blockName = "Chest";
                break;
            case DISPENSER:
            	blockName = "Dispenser";
                break;
            }
            sender.sendMessage("§5Target " + blockName + " has been unlinked from PhatLoot §6" + phatLoot.name);
            phatLoot.saveChests();
        }
    }

    /**
     * Modifies the reset time of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param days The amount of days
     * @param hours The amount of hours
     * @param minutes The amount of minutes
     * @param seconds The amount of seconds
     */
    public static void time(CommandSender sender, String name, int days, int hours, int minutes, int seconds) {
        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.days = days;
            phatLoot.hours = hours;
            phatLoot.minutes = minutes;
            phatLoot.seconds = seconds;
            sender.sendMessage("§5Reset time for PhatLoot §6" + phatLoot.name
                    + "§5 has been set to §6" + days + " days, "
                    + hours + " hours, " + minutes + " minutes, and "
                    + seconds + " seconds");

            phatLoot.save();
        }
    }

    /**
     * Modifies global of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param global The new value of global
     */
    public static void global(CommandSender sender, String name, boolean global) {
        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            if (phatLoot.global != global) {
                phatLoot.global = global;
                phatLoot.reset(null);

                sender.sendMessage("§5PhatLoot §6" + phatLoot.name + "§5 has been set to §6"
                        + (global ? "global" : "individual") + "§5 reset");
            }
            phatLoot.save();
        }
    }

    /**
     * Modifies autoLoot of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param autoLoot The new value of global
     */
    public static void autoLoot(CommandSender sender, String name, boolean autoLoot) {
        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            if (phatLoot.autoLoot != autoLoot) {
                phatLoot.autoLoot = autoLoot;
                phatLoot.reset(null);

                sender.sendMessage("§5PhatLoot §6" + phatLoot.name + "§5 has been set to"
                        + (autoLoot ? "automatically add Loot to the looters inventory." : "open the chest inventory for the looter."));
            }
            phatLoot.save();
        }
    }

    /**
     * Modifies round of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param round The new value of round
     */
    public static void round(CommandSender sender, String name, boolean round) {
        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.round = round;

            sender.sendMessage("§5PhatLoot §6" + phatLoot.name + "§5 has been set to §6"
                    + (round ? "" : "not") + "round down time");
            phatLoot.save();
        }
    }

    /**
     * Adds/Removes a Loot to the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param add True the Loot will be added, false if it will be removed
     * @param lootID The id of the Loot, 0 for individual loots
     * @param loot The Loot that will be added/removed
     */
    public static void setLoot(CommandSender sender, String name, boolean add, int lootID, OldLoot loot) {
        String lootDescription = loot.toString();

        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            ArrayList<OldLoot> lootTable = phatLoot.getLootTable(lootID);
            ListIterator itr = lootTable.listIterator();
            boolean found = false;
            while (itr.hasNext()) {
                if (itr.next().equals(loot)) {
                    found = true;

                    //Cancel if the Player is trying to duplicate the Loot
                    if (add) {
                        sender.sendMessage("§6" + lootDescription
                                + "§4 is already Loot for PhatLoot §6"
                                + phatLoot.name);
                    } else {
                        itr.remove();

                        //Display the appropriate message
                        if (lootID == 0)  { //Individual Loot
                            sender.sendMessage("§6" + lootDescription
                                    + "§5 removed as Loot for PhatLoot §6"
                                    + phatLoot.name);
                        } else { //Collective Loot
                            sender.sendMessage("§6" + lootDescription
                                    + "§5 removed as Loot from §6coll"
                                    + lootID + "§5, §6"
                                    + phatLoot.getPercentRemaining(lootID)
                                    + "%§5 remaining");
                        }

                        phatLoot.save();
                    }
                    break;
                }
            }

            if (!found) {
                //Cancel if the Loot is not present
                if (add) {
                    lootTable.add(loot);

                    //Display the appropriate message
                    if (lootID == 0) { //Individual Loot
                        sender.sendMessage("§6" + lootDescription
                                + "§5 added as Loot for PhatLoot §6"
                                + phatLoot.name);
                    } else { //Collective Loot
                        sender.sendMessage("§6" + lootDescription
                                + "§5 added as Loot to §6coll"
                                + lootID + "§5, §6"
                                + phatLoot.getPercentRemaining(lootID)
                                + "%§5 remaining");
                    }

                    phatLoot.save();
                } else {
                    sender.sendMessage("§6" + lootDescription
                            + "§4 was not found as a Loot for PhatLoot §6"
                            + phatLoot.name);
                }
            }
        }
    }

    /**
     * Sets the cost range of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param amount The new cost
     */
    public static void setCost(CommandSender sender, String name, int amount) {
        if (amount > 0) {
            amount = -amount;
        }

        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.moneyLower = amount;
            phatLoot.moneyUpper = amount;

            sender.sendMessage("§5Players will now be charged §6"
                    + -amount + "§5 to loot §6" + phatLoot.name);
            phatLoot.save();
        }
    }

    /**
     * Sets the money range of the specified PhatLoot
     *
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param amount The String of the range of the amount
     */
    public static void setMoney(CommandSender sender, String name, String amount) {
        int lower = getLowerBound(amount);
        int upper = getUpperBound(amount);
        if (lower == -1 || upper == -1) {
            sender.sendMessage("§6" + amount + " §4is not a valid number or range");
            return;
        }

        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.moneyLower = lower;
            phatLoot.moneyUpper = upper;

            sender.sendMessage("§5Money for PhatLoot §6"
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
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param amount The String of the range of the amount
     */
    public static void setExp(CommandSender sender, String name, String amount) {
        int lower = getLowerBound(amount);
        int upper = getUpperBound(amount);
        if (lower == -1 || upper == -1) {
            sender.sendMessage("§6" + amount + " §4is not a valid number or range");
            return;
        }

        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            phatLoot.expLower = lower;
            phatLoot.expUpper = upper;

            sender.sendMessage("§5Experience for PhatLoot §6"
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
     * @param sender The CommandSender modifying the PhatLoot
     * @param name The name of the PhatLoot to be modified or null to indicate all linked PhatLoots
     * @param double The percent chance of looting the command
     * @param add True if the command is to be added
     * @param cmd The command to be added/removed
     */
    public static void setCommand(CommandSender sender, String name, double percent, boolean add, String cmd) {
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        if (percent < 100) {
            cmd += '%' + percent;
        }

        for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
            boolean done = false;
            //Try to find the cmd
            for (String string: phatLoot.commands) {
                if (cmd.equals(string)) {
                    /*The Loot was found*/
                    //Cancel if the sender is trying to duplicate the cmd
                    if (!add) {
                        phatLoot.commands.remove(cmd);
                        sender.sendMessage("§6" + cmd
                                + "§5 removed as a command for PhatLoot §6"
                                + phatLoot.name);
                        phatLoot.save();
                    } else {
                        sender.sendMessage("§6" + cmd
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
                    sender.sendMessage("§6" + cmd
                            + "§5 added as a command for PhatLoot §6"
                            + phatLoot.name);
                    phatLoot.save();
                } else {
                    sender.sendMessage("§6" + cmd
                            + "§4 was not found as a command for PhatLoot §6"
                            + phatLoot.name);
                }
            }
        }
    }

    /**
     * Displays a list of current PhatLoot
     *
     * @param sender The CommandSender requesting the list
     */
    public static void list(CommandSender sender) {
        String list = "§5Current PhatLoots: §6";

        //Concat each PhatLoot
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            list += phatLoot.name + ", ";
        }

        sender.sendMessage(list.substring(0, list.length() - 2));
    }

    /**
     * Displays the info of the specified PhatLoot
     * If a name is not provided, a list of PhatLoots linked to the target Block is displayed
     *
     * @param sender The CommandSender requesting the info
     * @param name The name of the PhatLoot
     */
    public static void info(CommandSender sender, String name) {
        LinkedList<PhatLoot> phatLoots = getPhatLoots(sender, name);
        switch (phatLoots.size()) {
        case 0:
            break;

        case 1: //Display information for the one PhatLoot
            PhatLoot phatLoot = phatLoots.getFirst();

            sender.sendMessage("§2Name:§b " + phatLoot.name
                    + " §2Global Reset:§b " + phatLoot.global
                    + " §2Round Down:§b " + phatLoot.round);
            sender.sendMessage("§2Reset Time:§b " + phatLoot.days
                    + " days, " + phatLoot.hours + " hours, "
                    + phatLoot.minutes + " minutes, and "
                    + phatLoot.seconds + " seconds.");
            sender.sendMessage("§2Money§b: " + phatLoot.moneyLower + "-"
                    + phatLoot.moneyUpper + " §2Experience§b: "
                    + phatLoot.expLower + "-" + phatLoot.expUpper);
            sender.sendMessage("§2# of collective loots:§b "
                    + phatLoot.numberCollectiveLoots);

            //Display Individual Loots if not empty
            String loots = phatLoot.lootTableToString(PhatLoot.INDIVIDUAL);
            if (!loots.isEmpty()) {
                sender.sendMessage("§2IndividualLoots§b: " + loots);
            }

            //Display each Collective Loots that is not empty
            for (int i = 1; i <= 10; i++) {
                loots = phatLoot.lootTableToString(i);
                if (!loots.isEmpty()) {
                    sender.sendMessage("§2Coll" + i + "§b: " + loots);
                }
            }

            break;

        default: //List all PhatLoots
            String list = "§5Linked PhatLoots: §6";

            //Concat each PhatLoot
            for (PhatLoot pl : phatLoots) {
                list += pl.name + ", ";
            }

            sender.sendMessage(list.substring(0, list.length() - 2));
            break;
        }
    }

    /**
     * Reset the use times of the specified PhatLoot/PhatLootChest
     * If a name is not provided, the target PhatLootChest is reset
     *
     * @param sender The CommandSender reseting the PhatLootChests
     * @param name The name of the PhatLoot
     */
    public static void reset(CommandSender sender, String name) {
        //Reset the target Chest if a name was not provided
        if (name != null) {
            //Reset all Chests in every PhatLoot if the name provided is 'all'
            if (name.equals("all")) {
                for (PhatLoot phatLoots : PhatLoots.getPhatLoots()) {
                    phatLoots.reset(null);
                }

                if (sender != null) {
                    sender.sendMessage("§5All Chests in each PhatLoot have been reset.");
                }
                return;
            }

            //Find the PhatLoot that will be reset using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Cancel if the PhatLoot does not exist
            if (!PhatLoots.hasPhatLoot(name)) {
                if (sender != null) {
                    sender.sendMessage("§4PhatLoot §6" + name + "§4 does not exsist.");
                }
                return;
            }

            //Reset all Chests linked to the PhatLoot
            phatLoot.reset(null);

            if (sender != null) {
                sender.sendMessage("§5All Chests in PhatLoot §6"
                                    + name + "§5 have been reset.");
            }
        } else if (sender != null) {
            //Cancel is the sender is console
            if (!(sender instanceof Player)) {
                    sender.sendMessage("§4You cannot do this from the console!");
                    return;
            }

            Block block = ((Player) sender).getTargetBlock(TRANSPARENT, 10);
            String blockName = "Block";
            for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
                phatLoot.reset(block);
                switch (block.getType()) {
                case CHEST:
                case ENDER_CHEST:
                    blockName = "Chest";
                    break;
                case DISPENSER:
                    blockName = "Dispenser";
                    break;
                }
                sender.sendMessage("§5Target "+ blockName + " has been reset.");
            }
        }
    }

    /**
     * Clean the use times of the specified PhatLoot/PhatLootChest
     * If a name is not provided, the target PhatLootChest is cleaned
     *
     * @param sender The CommandSender cleaning the PhatLootChests
     * @param name The name of the PhatLoot
     */
    public static void clean(CommandSender sender, String name) {
        //Clean the target Chest if a name was not provided
        if (name != null) {
            //Clean all Chests in every PhatLoot if the name provided is 'all'
            if (name.equals("all")) {
                for (PhatLoot phatLoots : PhatLoots.getPhatLoots()) {
                    phatLoots.clean(null);
                }

                if (sender != null) {
                    sender.sendMessage("§5All Chests in each PhatLoot have been reset.");
                }
                return;
            }

            //Find the PhatLoot that will be reset using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Cancel if the PhatLoot does not exist
            if (!PhatLoots.hasPhatLoot(name)) {
                if (sender != null) {
                    sender.sendMessage("§4PhatLoot §6" + name + "§4 does not exsist.");
                }
                return;
            }

            //Clean all Chests linked to the PhatLoot
            phatLoot.clean(null);

            if (sender != null) {
                sender.sendMessage("§5All Chests in PhatLoot §6"
                                    + name + "§5 have been reset.");
            }
        } else if (sender != null) {
        	//Cancel is the sender is console
        	if (!(sender instanceof Player)) {
                    sender.sendMessage("§4You cannot do this from the console!");
                    return;
        	}

            Block block = ((Player) sender).getTargetBlock(TRANSPARENT, 10);
            String blockName = "Block";
            for (PhatLoot phatLoot : getPhatLoots(sender, name)) {
                phatLoot.clean(block);
                switch (block.getType()) {
                case CHEST:
                case ENDER_CHEST:
                    blockName = "Chest";
                    break;
                case DISPENSER:
                    blockName = "Dispenser";
                    break;
                }
                sender.sendMessage("§5Target "+ blockName + " has been reset.");
            }
        }
    }

    /**
     * Displays the PhatLoots Help Page to the given Player
     *
     * @param sender The CommandSender needing help
     */
    private static void sendHelp(CommandSender sender) {
        if (!sender.hasPermission("phatloots.make") && !sender.hasPermission("phatloots.rl")
                 && !sender.hasPermission("phatloots.reset") && !sender.hasPermission("phatloots.list")
                 && !sender.hasPermission("phatloots.info") && !sender.hasPermission("phatloots.name")
                 && !sender.hasPermission("phatloots.give")) {

        }
        sender.sendMessage("§e     PhatLoots Help Page:");
        sender.sendMessage("§2/"+command+" <Name>§b Loot a virtual Chest for the given PhatLoot");
        sender.sendMessage("§2/"+command+" list§b List all PhatLoots");
        sender.sendMessage("§2/"+command+" info [Name]§b List info of PhatLoot");
        sender.sendMessage("§2/"+command+" give <Player> <PhatLoot> [Title]§b Force Player to open a PhatLoot");
        sender.sendMessage("§2/"+command+" reset§b Reset looted times for target Block");
        sender.sendMessage("§2/"+command+" reset <Name>§b Reset looted times for PhatLoot");
        sender.sendMessage("§2/"+command+" reset all§b Reset looted times for all PhatLoots");
        sender.sendMessage("§2/"+command+" clean§b Clean looted times for target Block");
        sender.sendMessage("§2/"+command+" clean <Name>§b Clean looted times for PhatLoot");
        sender.sendMessage("§2/"+command+" clean all§b Clean looted times for all PhatLoots");
        sender.sendMessage("§2/"+command+" help create§b Display PhatLoots Create Help Page");
        sender.sendMessage("§2/"+command+" help setup§b Display PhatLoots Setup Help Page");
        sender.sendMessage("§2/"+command+" help loot§b Display PhatLoots Manage Loot Help Page");
        sender.sendMessage("§2/"+command+" rl§b Reload PhatLoots Plugin");
    }

    /**
     * Displays the PhatLoots Create Help Page to the given Player
     *
     * @param sender The CommandSender needing help
     */
    private static void sendCreateHelp(CommandSender sender) {
        sender.sendMessage("§e     PhatLoots Create Help Page:");
        sender.sendMessage("§7If Name is not specified then all PhatLoots linked to the target Block will be affected");
        sender.sendMessage("§2/"+command+" make <Name>§b Create PhatLoot with given name");
        sender.sendMessage("§2/"+command+" delete <Name>§b Delete PhatLoot");
        sender.sendMessage("§2/"+command+" link <Name>§b Link target Chest/Dispenser with PhatLoot");
        sender.sendMessage("§2/"+command+" unlink [Name]§b Unlink target Block from PhatLoot");
    }

    /**
     * Displays the PhatLoots Setup Help Page to the given Player
     *
     * @param sender The CommandSender needing help
     */
    private static void sendSetupHelp(CommandSender sender) {
        sender.sendMessage("§e     PhatLoots Setup Help Page:");
        sender.sendMessage("§7If Name is not specified then all PhatLoots linked to the target Block will be affected");
        sender.sendMessage("§6Amount may be a number §4(100)§6 or range §4(100-500)");
        sender.sendMessage("§2/"+command+" time [Name] <Days> <Hrs> <Mins> <Secs>§b Set cooldown time for PhatLoot");
        sender.sendMessage("§2/"+command+" time [Name] never§b Set PhatLoot to only be lootable once per chest");
        sender.sendMessage("§2/"+command+" global [Name] <true|false>§b Set PhatLoot to global or individual");
        sender.sendMessage("§2/"+command+" autoloot [Name] <true|false>§b Set if Items are automatically looted");
        sender.sendMessage("§2/"+command+" round [Name] <true|false>§b Set if cooldown times should round down (ex. Daily/Hourly loots)");
        sender.sendMessage("§2/"+command+" cost [Name] <Amount>§b Set cost of looting");
        sender.sendMessage("§2/"+command+" money [Name] <Amount>§b Set money range to be looted");
        sender.sendMessage("§2/"+command+" exp [Name] <Amount>§b Set experience to be gained");
        sender.sendMessage("§2/"+command+" <add|remove> cmd [Name] [Percent] /<Command>§b Set a Command that will be executed upon looting");
        sender.sendMessage("§5To add a percent chance to a cmd use % similar to item loot");
        sender.sendMessage("§bex. §1/"+command+" add cmd Test %50 /lightning <player>");
    }

    /**
     * Displays the PhatLoots Sign Help Page to the given Player
     *
     * @param sender The CommandSender needing help
     */
    private static void sendLootHelp(CommandSender sender) {
        sender.sendMessage("§e     PhatLoots Manage Loot Help Page:");
        sender.sendMessage("§5A Parameter starts with the 1 character §2id");
        sender.sendMessage("§2p§f: §5The Name of the PhatLoot ex. §6pEpic");
        sender.sendMessage("§bIf PhatLoot is not specified then all PhatLoots linked to the target Block will be affected");
        sender.sendMessage("§2d§f: §5The data/durability value of the item ex. §6d5");
        sender.sendMessage("§2c§f: §5The id of the collective loot to specify ex. §6c1");
        sender.sendMessage("§2#§f: §5The amount of the item ex. §6#10 §5or §6#1-64 §5(default: §61§5)");
        sender.sendMessage("§2%§f: §5The chance of looting the item ex. §6%50 §5or §6%0.1 §5(default: §6100§5)");
        sender.sendMessage("§2e§f: §5The item enchantment ex. §6earrow_fire §5or §6eauto");
        sender.sendMessage("§bEnchantment levels can be added. ex. §6arrow_fire(2)");
        sender.sendMessage("§2/"+command+" <add|remove> <Item|ID|hand> [Parameter1] [Parameter2]...");
        sender.sendMessage("§bex. §6/"+command+" add hand #1-16 nEnderNade %32");
        sender.sendMessage("§bex. §6/"+command+" add bow earrow_fire(2) earrow_unlimited %5");
        sender.sendMessage("§bTutorial Video:");
        sender.sendMessage("§1§nwww.youtu.be/tRQuKbRTaA4");
    }

    /**
     * Returns the a LinkedList of PhatLoots
     * If a name is provided then only the PhatLoot with the given name will be in the List
     * If no name is provided then each PhatLoot that is linked to the target Block will be in the List
     *
     * @param sender The CommandSender targeting a Block
     * @param name The name of the PhatLoot to be found
     * @return The a LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(CommandSender sender, String name) {
        LinkedList<PhatLoot> phatLoots = new LinkedList<PhatLoot>();

        if (name != null) {
            //Find the PhatLoot using the given name
            PhatLoot phatLoot = PhatLoots.getPhatLoot(name);

            //Inform the sender if the PhatLoot does not exist
            if (phatLoot != null ) {
                phatLoots.add(phatLoot);
            } else {
                sender.sendMessage("§4PhatLoot §6" + name + "§4 does not exist.");
            }
        } else {
        	//Cancel is the sender is console
        	if (!(sender instanceof Player)) {
                    sender.sendMessage("§4You cannot do this from the console!");
                    return phatLoots;
        	}

            //Cancel if the sender is not targeting a correct Block
        	Block block = ((Player)sender).getTargetBlock(TRANSPARENT, 10);
        	String blockName = "Block";
            switch (block.getType()) {
            case CHEST:
            case ENDER_CHEST:
            	blockName = "Chest";
                break;
            case DISPENSER:
            	blockName = "Dispenser";
                break;
            default:
                sender.sendMessage("§4You must target a Chest/Dispenser.");
                return phatLoots;
            }

            phatLoots = PhatLoots.getPhatLoots(block);

            //Inform the sender if the Block is not linked to any PhatLoots
            if (phatLoots.isEmpty()) {
                sender.sendMessage("§4Target " + blockName + " is not linked to a PhatLoot");
            }
        }

        return phatLoots;
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the id
     */
    public static int getCollID(String string) {
        int id = 0;
        try {
            id = Integer.parseInt(string);
        } catch (Exception ex) {
        }

        if (id < 1 || id > 10) {
            return -1;
        }

        return id;
    }

    /**
     * Retrieves an int value from the given string
     *
     * @param sender The CommandSender that will receive error messages
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
     * @param sender The CommandSender that will receive error messages
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
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the item
     */
    public static ItemStack getItemStack(CommandSender sender, String string) {
        if (string.equals("hand")) {
            if (sender instanceof Player) {
                return ((Player) sender).getItemInHand().clone();
            }
        }

        Material material;
        if (string.matches("[0-9]+")) {
            int id = Integer.parseInt(string);
            material = Material.getMaterial(id);
        } else {
            //Verify that the item id is valid
            material = Material.getMaterial(string.toUpperCase());
        }

        //Verify that the item is valid
        if (material == null) {
            if (sender != null) {
                sender.sendMessage("§6" + string + "§4 is not a valid item id");
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
     * @param sender The CommandSender that will receive error messages
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
     * @param sender The CommandSender that will receive error messages
     * @param string The String that contains the percent
     */
    public static double getPercent(CommandSender sender, String string) {
        double percent;
        try {
            percent = Double.parseDouble(string);
            if (percent < 0) {
                if (sender != null) {
                    sender.sendMessage("§4The percent cannot be below 0");
                }
            }
            if (percent > 100) {
                if (sender != null) {
                    sender.sendMessage("§4The percent cannot be above 100");
                }
            } else {
                return percent;
            }
        } catch (Exception notDouble) {
            if (sender != null) {
                sender.sendMessage("§6" + string + "§4 is not a valid number");
            }
        }
        return -1;
    }

    /**
     * Concats arguments together to create a sentence from words
     * This also replaces & with § to add color codes
     *
     * @param args the arguments to concat
     * @param first Which argument should the sentence start with
     * @return The new String that was created
     */
    private static String concatArgs(String[] args, int first) {
        StringBuilder sb = new StringBuilder();
        if (first > args.length) {
            return "";
        }
        for (int i = first; i <= args.length - 1; i++) {
            sb.append(" ");
            sb.append(args[i]);
        }
        String string = sb.substring(1);
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}

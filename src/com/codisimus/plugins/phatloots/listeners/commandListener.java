package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.Loot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsChest;
import com.codisimus.plugins.phatloots.PhatLootsMain;
import com.codisimus.plugins.phatloots.SaveSystem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Executes Player Commands
 * 
 * @author Codisimus
 */
public class commandListener implements CommandExecutor {
    //public static final HashSet TRANSPARENT = Sets.newHashSet(27, 28, 37, 38, 39, 40, 50, 65, 66, 69, 70, 72, 75, 76, 78);
    
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
        if (!(sender instanceof Player))
            return true;
        
        Player player = (Player)sender;

        //Display help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        //Set the ID of the command
        int commandID = 0;
        if (args[0].equals("make"))
            commandID = 1;
        else if (args[0].equals("link"))
            commandID = 2;
        else if (args[0].equals("unlink"))
            commandID = 3;
        else if (args[0].equals("delete"))
            commandID = 4;
        else if (args[0].equals("time"))
            commandID = 5;
        else if (args[0].equals("type"))
            commandID = 6;
        else if (args[0].equals("add"))
            commandID = 7;
        else if (args[0].equals("remove"))
            commandID = 8;
        else if (args[0].equals("money"))
            commandID = 9;
        else if (args[0].equals("list"))
            commandID = 10;
        else if (args[0].equals("name"))
            commandID = 11;
        else if (args[0].equals("info"))
            commandID = 12;
        else if (args[0].equals("reset"))
            commandID = 13;
        
        //Execute the command
        switch (commandID) {
            case 1: //command == make
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 2)
                    make(player, args[1]);
                else
                    sendHelp(player);
                
                return true;
                
            case 2: //command == link
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 2)
                    link(player, args[1]);
                else
                    sendHelp(player);
                
                return true;
                
            case 3: //command == unlink
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 1)
                    unlink(player);
                else
                    sendHelp(player);
                
                return true;
                
            case 4: //command == delete
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 2)
                    delete(player, args[1]);
                else
                    sendHelp(player);
                
                return true;
                
            case 5: //command == time
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                switch (args.length) {
                    case 5:
                        try {
                            time(player, null, Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                            return true;
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            break;
                        }
                        
                    case 6:
                        try {
                            time(player, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                                    Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                            return true;
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            break;
                        }
                        
                    default: break;
                }
                
                sendHelp(player);
                return true;
                
            case 6: //command == type
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                boolean global;
                switch (args.length) {
                    case 2:
                        if (args[1].equals("global"))
                            global = true;
                        else if (args[1].equals("player"))
                            global = false;
                        else
                            break;
                        
                        type(player, null, global);
                        return true;
                        
                    case 3:
                        if (args[2].equals("global"))
                            global = true;
                        else if (args[2].equals("player"))
                            global = false;
                        else
                            break;
                        
                        type(player, args[1], global);
                        return true;
                        
                    default: break;
                }
                
                sendHelp(player);
                return true;
                
            case 7: //command == add
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                String addName = null;
                int addID = 0;
                Loot addLoot;
                
                switch (args.length) {
                    case 4:
                        addLoot = getLoot(player, args[1], "0", args[2], args[3]);
                        break;
                        
                    case 5:
                        try {
                            Integer.parseInt(args[1]);
                            addLoot = getLoot(player, args[1], args[2], args[3], args[4]);
                            break;
                        }
                        catch (Exception notDurability) {
                            addLoot = getLoot(player, args[2], "0", args[3], args[4]);
                        }
                        
                        if (!args[1].startsWith("coll")) {
                            addName = args[1];
                            break;
                        }
                        
                        try {
                            addID = Integer.parseInt(args[1].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        break;
                        
                    case 6:
                        try {
                            Integer.parseInt(args[1]);
                            addLoot = getLoot(player, args[1], args[2], args[4], args[5]);
                            
                            if (!args[1].startsWith("coll")) {
                                addName = args[1];
                                break;
                            }

                            try {
                                addID = Integer.parseInt(args[1].substring(4));
                            }
                            catch (Exception notInt) {
                                sendHelp(player);
                                return true;
                            }
                            
                            break;
                        }
                        catch (Exception notDurability) {
                            addLoot = getLoot(player, args[2], "0", args[4], args[5]);
                        }
                        
                        addName = args[1];
                        
                        try {
                            addID = Integer.parseInt(args[2].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        break;
                        
                    case 7:
                        addName = args[1];
                        
                        try {
                            addID = Integer.parseInt(args[2].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        addLoot = getLoot(player, args[3], args[4], args[5], args[6]);
                        break;
                        
                    default: sendHelp(player); return true;
                    
                }
                
                if (addLoot == null) {
                    sendHelp(player);
                    return true;
                }
                
                setLoot(player, addName, true, addID, addLoot);
                return true;
                
            case 8: //command == remove
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                String removeName = null;
                int removeID = 0;
                Loot removeLoot;
                
                switch (args.length) {
                    case 4:
                        removeLoot = getLoot(player, args[1], "0", args[2], args[3]);
                        break;
                        
                    case 5:
                        try {
                            Integer.parseInt(args[1]);
                            removeLoot = getLoot(player, args[1], args[2], args[3], args[4]);
                            break;
                        }
                        catch (Exception notDurability) {
                            removeLoot = getLoot(player, args[2], "0", args[3], args[4]);
                        }
                        
                        if (!args[1].startsWith("coll")) {
                            removeName = args[1];
                            break;
                        }
                        
                        try {
                            removeID = Integer.parseInt(args[1].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        break;
                        
                    case 6:
                        try {
                            Integer.parseInt(args[1]);
                            removeLoot = getLoot(player, args[1], args[2], args[4], args[5]);
                            
                            if (!args[1].startsWith("coll")) {
                                removeName = args[1];
                                break;
                            }

                            try {
                                removeID = Integer.parseInt(args[1].substring(4));
                            }
                            catch (Exception notInt) {
                                sendHelp(player);
                                return true;
                            }
                            
                            break;
                        }
                        catch (Exception notDurability) {
                            removeLoot = getLoot(player, args[2], "0", args[4], args[5]);
                        }
                        
                        removeName = args[1];
                        
                        try {
                            removeID = Integer.parseInt(args[2].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        break;
                        
                    case 7:
                        removeName = args[1];
                        
                        try {
                            removeID = Integer.parseInt(args[2].substring(4));
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            return true;
                        }
                        
                        removeLoot = getLoot(player, args[3], args[4], args[5], args[6]);
                        break;
                        
                    default: sendHelp(player); return true;
                    
                }
                
                if (removeLoot == null) {
                    sendHelp(player);
                    return true;
                }
                
                setLoot(player, removeName, false, removeID, removeLoot);
                return true;
                
            case 9: //command == money
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "make")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                switch (args.length) {
                    case 3:
                        try {
                            setMoney(player, null, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                            return true;
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            break;
                        }
                        
                    case 4:
                        try {
                            setMoney(player, args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                            return true;
                        }
                        catch (Exception notInt) {
                            sendHelp(player);
                            break;
                        }
                        
                    default: break;
                }
                
                sendHelp(player);
                return true;
                
            case 10: //command == list
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "list")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 1)
                    list(player);
                else
                    sendHelp(player);
                
                return true;
                
            case 11: //command == name
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "name")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 1)
                    list(player);
                else
                    sendHelp(player);
                
                return true;
                
            case 12: //command == info
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "info")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                if (args.length == 2)
                    info(player, args[1]);
                else
                    sendHelp(player);
                
                return true;
                
            case 13: //command == reset
                //Cancel if the Player does not have permission to use the command
                if (!PhatLootsMain.hasPermission(player, "reset")) {
                    player.sendMessage("You do not have permission to do that.");
                    return true;
                }
                
                switch (args.length) {
                    case 2: reset(player, null); return true;
                    case 3: reset(player, args[1]); return true;
                    default: break;
                }
                
                sendHelp(player);
                return true;
                
            default: sendHelp(player); return true;
        }
    }
    
    public static void make(Player player, String name) {
        //Cancel if the PhatLoots already exists
        if (SaveSystem.findPhatLoots(name) != null) {
            player.sendMessage("A PhatLoots named "+name+" already exists.");
            return;
        }
        
        SaveSystem.phatLootsList.add(new PhatLoots(name));
        player.sendMessage("PhatLoots "+name+" Made!");
        SaveSystem.save();
    }
    
    public static void link(Player player, String name) {
        //Cancel if the Player is not targeting a correct Block
        Block block = player.getTargetBlock(null, 10);
        int id = block.getTypeId();
        if (id != 54 && id != 23) {
            player.sendMessage("You must target a Chest/Dispenser.");
            return;
        }
        
        //Cancel if the Block is already linked to a PhatLoots
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        if (phatLoots != null) {
            player.sendMessage("TargetBlock is already linked to PhatLoots "+phatLoots.name+".");
            return;
        }
        
        //Cancel if the PhatLoots with the given name does not exist
        phatLoots = SaveSystem.findPhatLoots(name);
        if (phatLoots == null) {
            player.sendMessage("PhatLoots "+name+" does not exsist.");
            return;
        }
        
        phatLoots.chests.add(new PhatLootsChest(block));
        player.sendMessage("Target Block has been linked to PhatLoots "+name+"!");
        SaveSystem.save();
    }
    
    public static void unlink(Player player) {
        //Cancel if the Player is not targeting a correct Block
        Block block = player.getTargetBlock(null, 10);
        int id = block.getTypeId();
        if (id != 54 && id != 23) {
            player.sendMessage("You must target a Chest/Dispenser.");
            return;
        }
        
        //Cancel if the Block is not linked to a PhatLoots
        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
        if (phatLoots == null) {
            player.sendMessage("Target Block is not linked to a PhatLoots");
            return;
        }
        
        phatLoots.chests.remove(phatLoots.findChest(block));
        player.sendMessage("Button has been unlinked from PhatLoots "+phatLoots.name+"!");
        SaveSystem.save();
    }
    
    public static void delete(Player player, String name) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        SaveSystem.phatLootsList.remove(phatLoots);
        player.sendMessage("PhatLoots "+phatLoots.name+" was deleted!");
        SaveSystem.save();
    }
    
    public static void time(Player player, String name, int days, int hours, int minutes, int seconds) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        phatLoots.days = days;
        phatLoots.hours = hours;
        phatLoots.minutes = minutes;
        phatLoots.seconds = seconds;
        player.sendMessage("Reset time for PhatLoots "+phatLoots.name+" has been set to "+days+" days, "
                +hours+" hours, "+minutes+" minutes, and "+seconds+" seconds.");
        
        SaveSystem.save();
    }
    
    public static void type(Player player, String name, boolean global) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        phatLoots.global = global;
        String type = "Player";
        if (global)
            type = "global";
        player.sendMessage("Reset type for PhatLoots "+phatLoots.name+" has been set to "+type+"!");
        
        SaveSystem.save();
    }
    
    public static void setLoot(Player player, String name, boolean add, int lootID, Loot loot) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        if (add) {
            phatLoots.loots[lootID].add(loot);
            if (lootID == 0)
                player.sendMessage(loot.item.getType().name()+" added as Loot for Phat Loot "+phatLoots.name+"!");
            else
                player.sendMessage(loot.item.getType().name()+" added as Loot to coll"+lootID+", "
                        +phatLoots.getPercentRemaining(lootID)+"% remaining");
        }
        else {
            phatLoots.loots[lootID].remove(loot);
            if (lootID == 0)
                player.sendMessage(loot.item.getType().name()+" removed as Loot for Phat Loot "+phatLoots.name+"!");
            else
                player.sendMessage(loot.item.getType().name()+" removed as Loot to coll"+lootID+", "
                        +phatLoots.getPercentRemaining(lootID)+"% remaining");
        }
        
        SaveSystem.save();
    }
    
    public static void setMoney(Player player, String name, int low, int high) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        //Swap the numbers if they are out of order
        if (high < low) {
            int temp = high;
            high = low;
            low = temp;
        }
        
        phatLoots.rangeLow = low;
        phatLoots.rangeHigh = high;
        player.sendMessage("Money set to a range from "+low+" to "+high);
        
        SaveSystem.save();
    }
    
    public static void list(Player player) {
        String list = "Current Phat Loots:  ";
        for (PhatLoots phatLoots : SaveSystem.phatLootsList)
            list = list.concat(phatLoots.name+", ");
        player.sendMessage(list.substring(0, list.length() - 2));
    }
    
    public static void name(Player player) {
        //Find the PhatLoots using the target Block
        PhatLoots phatLoots = SaveSystem.findPhatLoots(player.getTargetBlock(null, 10));
        
        if (phatLoots != null)
            player.sendMessage("Block is part of Phat Loot "+phatLoots.name+"!");
        else
            player.sendMessage("Block is not linked to a Phat Loot.");
    }
    
    public static void info(Player player, String name) {
        PhatLoots phatLoots = getPhatLoots(player, name);
        if (phatLoots == null)
            return;
        
        player.sendMessage("IndividualLoots: "+phatLoots.getLoots(0));
        player.sendMessage("Coll1: "+phatLoots.getLoots(1));
        player.sendMessage("Coll2: "+phatLoots.getLoots(2));
        player.sendMessage("Coll3: "+phatLoots.getLoots(3));
        player.sendMessage("Coll4: "+phatLoots.getLoots(4));
        player.sendMessage("Coll5: "+phatLoots.getLoots(5));
    }
    
    public static void reset(Player player, String name) {
        //Reset the target Button if a name was not provided
        if (name == null) {
            //Find the PhatLoots that will be reset using the given name
            Block block = player.getTargetBlock(null, 10);
            PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
            
            //Cancel if the PhatLoots does not exist
            if (phatLoots == null ) {
                player.sendMessage("Target Block is not linked to a PhatLoots");
                return;
            }
            
            phatLoots.reset(block);
            
            player.sendMessage("Target Block has been reset.");
            return;
        }
        
        //Reset all Buttons in every PhatLoots if the name provided is 'all'
        if (name.equals("all")) {
            for (PhatLoots phatLoots: SaveSystem.phatLootsList)
                phatLoots.reset(null);
            
            player.sendMessage("All Chests in each PhatLoots has been reset.");
            return;
        }
        
        //Find the PhatLoots that will be reset using the given name
        PhatLoots phatLoots = SaveSystem.findPhatLoots(name);

        //Cancel if the PhatLoots does not exist
        if (phatLoots == null ) {
            player.sendMessage("PhatLoots "+name+" does not exsist.");
            return;
        }
        
        //Reset all Buttons linked to the PhatLoots
        phatLoots.reset(null);
        
        player.sendMessage("All Chests in PhatLoots "+name+" have been reset.");
        SaveSystem.save();
    }
    
    /**
     * Displays the PhatLoots Help Page to the given Player
     *
     * @param player The Player needing help
     */
    public static void sendHelp(Player player) {
        player.sendMessage("§e     PhatLoots Help Page:");
        player.sendMessage("§2/loot make [Name]§b Creates PhatLoot");
        player.sendMessage("§2/loot link [Name]§b Links target Chest/Dispenser with PhatLoot");
        player.sendMessage("§2/loot unlink§b Unlinks target Block");
        player.sendMessage("§2/loot delete (Name)§b Deletes PhatLoot and unlinks Block");
        player.sendMessage("§2/loot time (Name) [Days] [Hrs] [Mins] [Secs]§b Sets cooldown time");
        player.sendMessage("§2/loot type (Name) ['global' or 'player']§b Sets cooldown type");
        player.sendMessage("§2/loot ['add' or 'remove] (Name) ('coll'[1-5]) [Item] (Durability) [Amount] [Percent]"
                + "§b Manage items that may be looted");
        player.sendMessage("§2/loot money (Name) [Low] [High]§b Sets money range to be looted");
        player.sendMessage("§2/loot list§b Lists all PhatLoots");
        player.sendMessage("§2/loot name§b Gives PhatLoot name of target Block");
        player.sendMessage("§2/loot info (Name)§b Lists info of PhatLoot");
        player.sendMessage("§2/loot reset§b Reset activation times for target Block");
        player.sendMessage("§2/loot reset [Name or 'all']§b Reset Block linked to the PhatLoots");
    }
    
    public static PhatLoots getPhatLoots(Player player, String name) {
        PhatLoots phatLoots = null;
        
        if (name == null) {
            //Find the PhatLoots using the target Block
            phatLoots = SaveSystem.findPhatLoots(player.getTargetBlock(null, 10));
            
            //Cancel if the PhatLoots does not exist
            if (phatLoots == null ) {
                player.sendMessage("Target Block is not linked to a PhatLoots");
                return null;
            }
        }
        else {
            //Find the PhatLoots using the given name
            phatLoots = SaveSystem.findPhatLoots(name);
            
            //Cancel if the PhatLoots does not exist
            if (phatLoots == null ) {
                player.sendMessage("PhatLoots "+name+" does not exsist.");
                return null;
            }
        }
        
        return phatLoots;
    }
    
    /**
     * Checks different aspects of the loot values and returns the combined string
     * 
     * @param player The player who is using the command
     * @param item The String of the item name or ID
     * @param amount The String of the number of items
     * @param percent The String of the percent chance of receiving loot
     */
    public Loot getLoot(Player player, String item, String durability, String amount, String percent) {
        int id;

        try {
            id = Integer.parseInt(item);
            if (Material.getMaterial(id) == null) {
                player.sendMessage(item+" is not a valid item id");
                return null;
            }
        }
        catch (Exception notInt) {
            try {
                id = Material.getMaterial(item.toUpperCase()).getId();
            }
            catch (Exception invalid) {
                player.sendMessage(item+" is not a valid item");
                return null;
            }
        }

        int probablility;

        try {
            probablility = Integer.parseInt(percent);

            if (probablility < 1 || probablility > 100) {
                player.sendMessage(percent+" is not between 0 and 100");
                return null;
            }
        }
        catch (Exception notInt) {
            player.sendMessage(percent+" is not valid number between 0 and 100");
            return null;
        }

        return new Loot(id, Short.parseShort(durability), Integer.parseInt(amount), probablility);
    }
}

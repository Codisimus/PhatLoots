
package PhatLoots;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Codisimus
 */
public class PhatLootsPlayerListener extends PlayerListener{

    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        if (split[0].startsWith("/loot")) {
            event.setCancelled(true);
            try {
                if (split[1].startsWith("make")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    if (SaveSystem.findPhatLoots(split[2]) != null) {
                        player.sendMessage("A Phat Loot named "+split[2]+" already exists.");
                        return;
                    }
                    PhatLoots phatLoots = new PhatLoots(split[2]);
                    player.sendMessage("Phat Loot "+split[2]+" Made!");
                    SaveSystem.addPhatLoots(phatLoots);
                }
                else if(split[1].startsWith("link")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    Block block = player.getTargetBlock(null, 100);
                    Material mat = block.getType();
                    if (!mat.equals(Material.CHEST)) {
                        player.sendMessage("You must link the Phat Loot to a chest.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null) {
                        event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    if (SaveSystem.findPhatLoots(block) != null) {
                        player.sendMessage("Chest is already linked to Phat Loot "+split[2]+"!");
                        return;
                    }
                    phatLoots.addChest(block);
                    player.sendMessage("Chest has been linked to Phat Loot "+split[2]+"!");
                    SaveSystem.save();
                }
                else if(split[1].startsWith("add")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots != null) {
                        if (split[3].startsWith("coll")) {
                            String loot = checkLoot(player, split[4], split[5], split[6]);
                            if (loot == null)
                                return;
                            int total = phatLoots.addCollectiveLoot(Character.getNumericValue(split[3].charAt(4)), loot.concat(",~"));
                            int remaining = 100 - total;
                            player.sendMessage(loot+" added as loot to "+split[2]+", "+remaining+"% remaining");
                        }
                        else {
                            String loot = checkLoot(player, split[3], split[4], split[5]);
                            if (loot == null)
                                return;
                            phatLoots.individualLoots = phatLoots.individualLoots.concat(loot.concat(",~"));
                            player.sendMessage(loot+" added as loot for Phat Loot "+split[2]+"!");
                        }
                        SaveSystem.save();
                    }
                    else {
                        Block block = player.getTargetBlock(null, 100);
                        phatLoots = SaveSystem.findPhatLoots(block);
                        if (phatLoots == null) {
                            event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                            return;
                        }
                        if (split[2].startsWith("coll")) {
                            String loot = checkLoot(player, split[3], split[4], split[5]);
                            if (loot == null)
                                return;
                            int total = phatLoots.addCollectiveLoot(Character.getNumericValue(split[3].charAt(4)), loot.concat(",~"));
                            int remaining = 100 - total;
                            player.sendMessage(loot+" added as loot to "+phatLoots.name+", "+remaining+"% remaining");
                        }
                        else {
                            String loot = checkLoot(player, split[2], split[3], split[4]);
                            if (loot == null)
                                return;
                            phatLoots.individualLoots = phatLoots.individualLoots.concat(loot.concat(",~"));
                            player.sendMessage(loot+" added as loot for Phat Loot "+phatLoots.name+"!");
                        }
                        SaveSystem.save();
                    }
                }
                else if(split[1].startsWith("remove")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots != null) {
                        if (split[3].startsWith("coll")) {
                            String loot = split[4]+","+split[5]+","+split[6];
                            int total = phatLoots.removeCollectiveLoot(Character.getNumericValue(split[2].charAt(4)), loot.concat(",~"));
                            int remaining = 100 - total;
                            player.sendMessage(split[3]+" removed as loot to "+split[2]+", "+remaining+"% remaining");
                        }
                        else {
                            String loot = split[3]+","+split[4]+","+split[5];
                            if (phatLoots.individualLoots.contains(loot))
                                phatLoots.individualLoots = phatLoots.individualLoots.replaceAll(loot.concat(",~"), "");
                            else {
                                player.sendMessage("Loot "+loot+" not found in Phat Loot "+split[2]+"!");
                                return;
                            }
                            player.sendMessage(loot+" removed as loot for Phat Loot "+split[2]+"!");
                        }
                        SaveSystem.save();
                    }
                    else {
                        Block block = player.getTargetBlock(null, 100);
                        phatLoots = SaveSystem.findPhatLoots(block);
                        if (phatLoots == null) {
                            event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                            return;
                        }
                        if (split[3].startsWith("coll")) {
                            String loot = split[3]+","+split[4]+","+split[5];
                            int total = phatLoots.removeCollectiveLoot(Character.getNumericValue(split[2].charAt(4)), loot.concat(",~"));
                            int remaining = 100 - total;
                            player.sendMessage(split[3]+" removed as loot to "+split[2]+", "+remaining+"% remaining");
                        }
                        else {
                            String loot = split[2]+","+split[3]+","+split[4];
                            if (phatLoots.individualLoots.contains(loot))
                                phatLoots.individualLoots = phatLoots.individualLoots.replaceAll(loot.concat(",~"), "");
                            else {
                                player.sendMessage("Loot "+loot+" not found in Phat Loot "+phatLoots.name+"!");
                                return;
                            }
                            player.sendMessage(loot+" removed as loot for Phat Loot "+phatLoots.name+"!");
                        }
                        SaveSystem.save();
                    }
                }
                else if(split[1].startsWith("unlink")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    Block block = player.getTargetBlock(null, 100);
                    Material mat = block.getType();
                    if (!mat.equals(Material.CHEST)) {
                        player.sendMessage("You must target the chest you wish to unlink");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null) {
                        event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    if (phatLoots.removeChest(block))
                        player.sendMessage("Chest sucessfully unlinked!");
                    else
                        player.sendMessage("Chest was not linked to Phat Loot "+split[2]);
                    SaveSystem.save();
                }
                else if(split[1].startsWith("delete")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null ) {
                        player.sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    SaveSystem.removePhatLoots(phatLoots);
                    player.sendMessage("Phat Loot "+split[2]+" Deleted!");
                }
                else if (split[1].startsWith("reset")) {
                    if (!PhatLootsMain.hasPermission(player, "reset")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    if (split[2].equalsIgnoreCase("all")) {
                        LinkedList<PhatLoots> tempList = SaveSystem.getPhatLootsList();
                        for (PhatLoots phatLoots : tempList) {
                            phatLoots.reset();
                        }
                        player.sendMessage("All Phat Loots have been reset");
                    }
                    else {
                        PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                        if (phatLoots == null ) {
                            player.sendMessage("Phat Loot "+split[2]+" does not exsist.");
                            return;
                        }
                        phatLoots.reset();
                        player.sendMessage("Phat Loot "+split[2]+" has been reset");
                    }
                    SaveSystem.save();
                }
                else if (split[1].startsWith("type")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null) {
                        event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    if (split[3].equalsIgnoreCase("user")) {
                        if (phatLoots.resetType.equals("user")) {
                            player.sendMessage("That PhatLoot is already set to user reset");
                        }
                        else {
                            phatLoots.resetType = "user";
                            player.sendMessage("PhatLoot "+split[2]+" Is now set to user reset");
                        }
                    }
                    else if (split[3].equalsIgnoreCase("global")) {
                        if (phatLoots.resetType.equals("global")) {
                            player.sendMessage("That PhatLoot is already set to global reset");
                        }
                        else {
                            phatLoots.resetType = "global";
                            player.sendMessage("PhatLoot "+split[2]+" Is now set to global reset");
                        }
                    }
                }
                else if (split[1].startsWith("list")) {
                    if (!PhatLootsMain.hasPermission(player, "list")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    LinkedList<PhatLoots> tempList = SaveSystem.getPhatLootsList();
                    String phatLootsList = "";
                    player.sendMessage("Current Phat Loots:");
                    for (PhatLoots phatLoots : tempList) {
                        phatLootsList = phatLootsList.concat(phatLoots.name+", ");
                    }
                    player.sendMessage(phatLootsList);
                }
                else if(split[1].startsWith("time")) {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null) {
                        event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    phatLoots.resetTime = split[3];
                    player.sendMessage("Phat Loot "+split[2]+" reset time changed to "+split[3]+"!");
                    SaveSystem.save();
                }
                else if (split[1].startsWith("info")) {
                    if (!PhatLootsMain.hasPermission(player, "info")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null ) {
                        player.sendMessage("Phat Loot "+split[2]+" does not exsist.");
                        return;
                    }
                    player.sendMessage("IndividualLoots: "+phatLoots.individualLoots);
                    player.sendMessage("Coll1: "+phatLoots.getCollectiveLoots(1));
                    player.sendMessage("Coll2: "+phatLoots.getCollectiveLoots(2));
                    player.sendMessage("Coll3: "+phatLoots.getCollectiveLoots(3));
                    player.sendMessage("Coll4: "+phatLoots.getCollectiveLoots(4));
                    player.sendMessage("Coll5: "+phatLoots.getCollectiveLoots(5));
                }
                else if(split[1].startsWith("name")) {
                    if (PhatLootsMain.hasPermission(player, "name"))
                                        player.sendMessage("You do not have permission to do that.");
                    Block block = player.getTargetBlock(null, 100);
                    if (block.getType().equals(Material.CHEST)) {
                        PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
                        if (phatLoots != null)
                            player.sendMessage("Chest is part of Phat Loot "+phatLoots.name+"!");
                    }
                }
                else if (split[1].startsWith("help") )
                    throw new Exception();
            }
            catch (Exception e) {
                player.sendMessage("§e     PhatLoots Help Page:");
                player.sendMessage("§2/loot make [Name]§b Creates PhatLoot");
                player.sendMessage("§2/loot link [Name]§b Links target chest with PhatLoot");
                player.sendMessage("§2/loot add coll[1-5] [Item] [Amount] [Percent]");
                player.sendMessage("§b Adds collective loot to target chest's PhatLoot");
                player.sendMessage("§2/loot add [Item] [Amount] [Percent]");
                player.sendMessage("§b Adds individual loot to target chest's PhatLoot");
                player.sendMessage("§2/loot add money [Low] [High]");
                player.sendMessage("§b Adds range of money that can be looted");
                player.sendMessage("§2/loot remove coll[1-5] [Item] [Amount] [Percent]");
                player.sendMessage("§b Removes collective loot from PhatLoot");
                player.sendMessage("§2/loot remove [Item] [Amount] [Percent]");
                player.sendMessage("§b Removes individual loot from PhatLoot");
                player.sendMessage("§2/loot unlink [Name]§b Unlinks target chest with PhatLoot");
                player.sendMessage("§2/loot info [Name]§b Lists info of PhatLoot");
                player.sendMessage("§2/loot name§b Gives PhatLoot name of target chest");
                player.sendMessage("§2/loot reset [Name or all]§b Resets PhatLoot restricted list");
                player.sendMessage("§2/loot delete [Name]§b Deletes PhatLoot and unlinks chests");
                player.sendMessage("§2/loot list§b Lists all PhatLoots");
            }
        }
        else
            return;
    }

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (block.getType().equals(Material.CHEST)) {
                PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
                if (phatLoots == null) {
                    block = PhatLootsMain.getBigChest(block);
                    if (block != null)
                        phatLoots = SaveSystem.findPhatLoots(block);
                }
                if (phatLoots != null)
                    if (!PhatLootsMain.hasPermission(player, "use"))
                        player.sendMessage("You do not have permission to receive loots.");
                    else if (!event.isCancelled()) {
                        phatLoots.getLoot(player, block);
                        SaveSystem.save();
                    }
            }
        }
    }
    
    /**
     * Checks different aspects of the loot values and returns the combined string
     * 
     * @param player The player who is using the command
     * @param item The String of the item name or ID
     * @param amount The String of the number of items
     * @param percent The String of the percent chance of receiving loot
     */
    private String checkLoot(Player player, String item, String amount, String percent) {
        if (item.equals("money")) {
            try {
                int low = Integer.parseInt(amount);
                int high = Integer.parseInt(percent);
                if (high < low)
                    throw new Exception();
            }
            catch (Exception e) {
                player.sendMessage("The correct format is 'money [low] [high]' ");
                player.sendMessage("Ex. 'money 1 10'");
                return null;
            }
        }
        else {
            int id;
            try {
                id = Integer.parseInt(item);
                if (!inRange(id)) {
                    player.sendMessage(item+" is not a valid item id");
                    return null;
                }
            }
            catch (Exception e) {
                try {
                    item = Material.getMaterial(item.toUpperCase()).getId()+"";
                }
                catch (Exception in) {
                    player.sendMessage(item+" is not a valid item");
                    return null;
                }
            }
            int number = Integer.parseInt(percent);
            if (number < 1 || number > 100) {
                player.sendMessage(percent+" is not between 0 and 100");
                return null;
            }
        }
        return item+","+amount+","+percent;
    }
    
    /**
     * Verifies that the item is valid before adding as loot
     * This avoids client crashing whenever opening the chest
     * 
     * @param dataValue The int of the itemType
     * @return false if given dataValue does not fall within a valid range
     */
    private boolean inRange(int dataValue) {
        if (dataValue < 1)
            return false;
        if (dataValue == 29)
            return false;
        if (dataValue == 33)
            return false;
        if (dataValue == 34)
            return false;
        if (dataValue == 36)
            return false;
        if (dataValue > 96 && dataValue < 256)
            return false;
        if (dataValue > 358 && dataValue < 2256)
            return false;
        if (dataValue > 2257)
            return false;
        else
            return true;
    }
}


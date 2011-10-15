
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
                Block block = player.getTargetBlock(null, 100);
                if (split[1].startsWith("help") )
                    throw new Exception();
                else if (split[1].startsWith("reset")) {
                    if (!PhatLootsMain.hasPermission(player, "reset")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    if (split[2].equalsIgnoreCase("all")) {
                        LinkedList<PhatLoots> tempList = SaveSystem.getPhatLootsList();
                        for (PhatLoots phatLoots: tempList)
                            phatLoots.reset();
                        player.sendMessage("All Phat Loots have been reset");
                    }
                    else {
                        PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                        if (phatLoots == null)
                            phatLoots = SaveSystem.findPhatLoots(block);
                        if (phatLoots == null) {
                            player.sendMessage("Phat Loot "+split[2]+" does not exsist.");
                            return;
                        }
                        phatLoots.reset();
                        player.sendMessage("Phat Loot "+split[2]+" has been reset");
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
                    for (PhatLoots phatLoots : tempList)
                        phatLootsList = phatLootsList.concat(phatLoots.name+", ");
                    player.sendMessage(phatLootsList);
                    return;
                }
                else if (split[1].startsWith("info")) {
                    if (!PhatLootsMain.hasPermission(player, "info")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                    if (phatLoots == null)
                        phatLoots = SaveSystem.findPhatLoots(block);
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
                    return;
                }
                else if(split[1].startsWith("name")) {
                    if (PhatLootsMain.hasPermission(player, "name")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
                    if (phatLoots != null)
                        player.sendMessage("Block is part of Phat Loot "+phatLoots.name+"!");
                    else
                        player.sendMessage("Block is not linked to a Phat Loot.");
                    return;
                }
                else {
                    if (!PhatLootsMain.hasPermission(player, "make")) {
                        player.sendMessage("You do not have permission to do that.");
                        return;
                    }
                    if (split[1].startsWith("make")) {
                        if (SaveSystem.findPhatLoots(split[2]) != null) {
                            player.sendMessage("A Phat Loot named "+split[2]+" already exists.");
                            return;
                        }
                        PhatLoots phatLoots = new PhatLoots(split[2]);
                        player.sendMessage("Phat Loot "+split[2]+" Made!");
                        SaveSystem.addPhatLoots(phatLoots);
                    }
                    else {
                        PhatLoots phatLoots = SaveSystem.findPhatLoots(split[2]);
                        int i = 3;
                        if (phatLoots == null) {
                            i--;
                            phatLoots = SaveSystem.findPhatLoots(block);
                        }
                        if (phatLoots == null) {
                            event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                            return;
                        }
                        if(split[1].startsWith("add")) {
                            if (split[i].startsWith("coll")) {
                                String loot = checkLoot(player, split[i+1], split[i+2], split[i+3]);
                                if (loot == null)
                                    return;
                                int total = phatLoots.addCollectiveLoot(Character.getNumericValue(split[i].charAt(4)), loot);
                                int remaining = 100 - total;
                                player.sendMessage(loot+" added as loot to "+split[i]+", "+remaining+"% remaining");
                            }
                            else {
                                String loot = checkLoot(player, split[i], split[i+1], split[i+2]);
                                if (loot == null)
                                    return;
                                phatLoots.individualLoots = phatLoots.individualLoots.concat(loot.concat(",~"));
                                player.sendMessage(loot+" added as loot for Phat Loot "+phatLoots.name+"!");
                            }
                            SaveSystem.save();
                        }
                        else if(split[1].startsWith("remove")) {
                            if (split[i].startsWith("coll")) {
                                String loot = checkLoot(player, split[i+1], split[i+2], split[i+3]);
                                if (loot == null)
                                    return;
                                int total = phatLoots.removeCollectiveLoot(Character.getNumericValue(split[i].charAt(4)), loot);
                                int remaining = 100 - total;
                                player.sendMessage(loot+" removed as loot to "+split[i]+", "+remaining+"% remaining");
                            }
                            else {
                                String loot = checkLoot(player, split[i], split[i+1], split[i+2]);
                                if (loot == null)
                                    return;
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
                        else {
                            if (phatLoots == null) {
                                event.getPlayer().sendMessage("Phat Loot "+split[2]+" does not exsist.");
                                return;
                            }
                            if(split[1].startsWith("link")) {
                                int id = block.getTypeId();
                                if (id != 54 && id != 23) {
                                    player.sendMessage("You must link the Phat Loot to a Chest or Dispenser.");
                                    return;
                                }
                                if (SaveSystem.findPhatLoots(block) != null) {
                                    player.sendMessage("Block is already linked to Phat Loot "+split[2]+"!");
                                    return;
                                }
                                phatLoots.addChest(block);
                                player.sendMessage("Block has been linked to Phat Loot "+split[2]+"!");
                                SaveSystem.save();
                            }
                            else if(split[1].startsWith("unlink")) {
                                int id = block.getTypeId();
                                if (id != 54 && id != 23) {
                                    player.sendMessage("You must target the Block you wish to unlink");
                                    return;
                                }
                                if (!phatLoots.removeChest(block)) {
                                    player.sendMessage("Block was not linked to Phat Loot "+split[2]);
                                    return;
                                }
                                player.sendMessage("Block sucessfully unlinked!");
                            }
                            else if(split[1].startsWith("delete")) {
                                SaveSystem.removePhatLoots(phatLoots);
                                player.sendMessage("Phat Loot "+split[2]+" Deleted!");
                            }
                            else if (split[1].startsWith("type")) {
                                if (!(split[3].equalsIgnoreCase("user") || split[3].equalsIgnoreCase("global")))
                                    throw new Exception();
                                if (phatLoots.resetType.equals(split[3])) {
                                    player.sendMessage("That PhatLoot is already set to "+split[3]+" cooldown");
                                    return;
                                }
                                phatLoots.resetType = split[3];
                                player.sendMessage("PhatLoot "+split[2]+" Is now set to "+split[3]+" cooldown");
                            }
                            else if(split[1].startsWith("time")) {
                                phatLoots.resetTime = split[3];
                                player.sendMessage("Phat Loot "+split[2]+" cooldown time changed to "+split[3]+"!");
                            }
                        }
                    }
                }
                SaveSystem.save();
            }
            catch (Exception e) {
                player.sendMessage("§e     PhatLoots Help Page:");
                player.sendMessage("§2/loot make [Name]§b Creates PhatLoot");
                player.sendMessage("§2/loot link [Name]§b Links target Chest/Dispenser with PhatLoot");
                player.sendMessage("§2/bw time [Name] [0'0'0'0]§b Sets cooldown time");
                player.sendMessage("§2/bw type [Name] [global or user]§b Sets cooldown type");
                player.sendMessage("§2/loot add coll[1-5] [Item] [Amount] [Percent]");
                player.sendMessage("§b Adds collective loot to target Block's PhatLoot");
                player.sendMessage("§2/loot add [Item] [Amount] [Percent]");
                player.sendMessage("§b Adds individual loot to target Block's PhatLoot");
                player.sendMessage("§2/loot add money [Low] [High]");
                player.sendMessage("§b Adds range of money that can be looted");
                player.sendMessage("§2/loot remove coll[1-5] [Item] [Amount] [Percent]");
                player.sendMessage("§b Removes collective loot from PhatLoot");
                player.sendMessage("§2/loot remove [Item] [Amount] [Percent]");
                player.sendMessage("§b Removes individual loot from PhatLoot");
                player.sendMessage("§2/loot unlink [Name]§b Unlinks target Block with PhatLoot");
                player.sendMessage("§2/loot info [Name]§b Lists info of PhatLoot");
                player.sendMessage("§2/loot name§b Gives PhatLoot name of target Block");
                player.sendMessage("§2/loot reset [Name or all]§b Resets PhatLoot restricted list");
                player.sendMessage("§2/loot delete [Name]§b Deletes PhatLoot and unlinks Block");
                player.sendMessage("§2/loot list§b Lists all PhatLoots");
            }
        }
    }

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if ((action.equals(Action.RIGHT_CLICK_BLOCK) && block.getTypeId() == 54) || (action.equals(Action.LEFT_CLICK_BLOCK) && block.getTypeId() == 23)) {
            PhatLoots phatLoots = SaveSystem.findPhatLoots(block);
            if (phatLoots == null && block.getTypeId() == 54) {
                block = PhatLootsMain.getBigChest(block);
                if (block != null)
                    phatLoots = SaveSystem.findPhatLoots(block);
            }
            Player player = event.getPlayer();
            if (phatLoots != null)
                if (!PhatLootsMain.hasPermission(player, "use"))
                    player.sendMessage("You do not have permission to receive loots.");
                else if (!event.isCancelled()) {
                    phatLoots.getLoot(player, block);
                    SaveSystem.save();
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
        if (item.equals("money"))
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
        else {
            int id;
            try {
                id = Integer.parseInt(item);
                if (Material.getMaterial(id) == null) {
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
}


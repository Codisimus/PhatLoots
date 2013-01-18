package com.codisimus.plugins.phatloots;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    static String chestName;
    private static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>();
    static HashMap<Player, File> descriptionFiles = new HashMap<Player, File>();
    //private static HashMap<Player, InventoryView> currentLooters = new HashMap<Player, InventoryView>();
    //private static LinkedList<InventoryView> openChests = new LinkedList<InventoryView>();

    /**
     * Writes Player Chat to Item Description Files
     *
     * @param event The AsyncPlayerChatEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!descriptionFiles.containsKey(player)) {
            return;
        }
        event.setCancelled(true);

        String line = event.getMessage();
        if (line.equals("done")) {
            descriptionFiles.remove(player);
            player.sendMessage("§5The Item Description has been saved");
            return;
        }

        File file = descriptionFiles.get(player);
        boolean newLine = file.exists();
        FileWriter fWriter = null;
        try {
            fWriter = new FileWriter(file, true);
            if (newLine) {
                line = "\n" + line;
            }
            fWriter.write(line);
            player.sendMessage("§5The line has been added to the Item Description");
        } catch (Exception e) {
            player.sendMessage("§4Error while writing the Item Description to file");
        } finally {
            try {
                fWriter.flush();
                fWriter.close();
            } catch (Exception ex) {
            }
        }

        player.sendMessage("§5Type the next description line or §5done to finish");
    }

    /**
     * Attempts to load OldChestData for newly loaded Worlds
     *
     * @param event The WorldLoadEvent that occurred
     */
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        FileInputStream fis = null;
        for (File file: new File(PhatLoots.dataFolder + "/PhatLoots/").listFiles()) {
            String name = file.getName();
            if (name.endsWith(".properties")) {
                try {
                    //Load the Properties file for reading
                    Properties p = new Properties();
                    fis = new FileInputStream(file);
                    p.load(fis);

                    if (p.containsKey("OldChestsData")) {
                        PhatLoots.getPhatLoot(name.substring(0, name.length() - 11))
                                .setChests(p.getProperty("OldChestsData"));
                    }
                } catch (Exception loadFailed) {
                } finally {
                    try {
                        fis.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

//    /**
//     * Checks if a Player loots a PhatLootChest
//     *
//     * @param event The PlayerInteractEvent that occurred
//     */
//    @EventHandler (ignoreCancelled = true)
//    public void onPlayerOpenChest(InventoryOpenEvent event) {
//        HumanEntity entity = event.getPlayer();
//        if (!(entity instanceof Player)) {
//            return;
//        }
//
//        Player player = (Player) entity;
//        if (currentLooters.containsKey(player)) {
//            return;
//        }
//
//        Inventory inventory = event.getInventory();
//        if (inventory.getType() != InventoryType.CHEST) {
//            return;
//        }
//
//        if (inventory instanceof DoubleChestInventory) {
//            //We only care about the left side because that is the Block that would be linked
//            inventory = ((DoubleChestInventory) inventory).getLeftSide();
//        }
//
//        Chest chest = (Chest) inventory.getHolder();
//        Block block = chest.getBlock();
//
//        //Return if the Chest is not a PhatLootChest
//        LinkedList<PhatLoot> phatLoots = PhatLoots.getPhatLoots(block);
//        if (phatLoots.isEmpty()) {
//            return;
//        }
//
//        boolean global = true;
//        for (PhatLoot phatLoot: phatLoots) {
//            if (!phatLoot.global) {
//                global = false;
//            }
//        }
//
//        //Create the custom key using the Player Name and Block location
//        final String KEY = (global ? "global" : player.getName())
//                            + "@" + block.getLocation().toString();
//
//        //Grab the custom Inventory belonging to the Player
//        ForgettableInventory fInventory = inventories.get(KEY);
//        if (fInventory != null) {
//            inventory = fInventory.getInventory();
//        } else { //Create a new Inventory for the Player
//            String phatLootName = "PhatLoots Chest";
//            for (PhatLoot phatLoot: phatLoots) {
//                if (PhatLoots.canLoot(player, phatLoot)) {
//                    phatLootName = phatLoot.name;
//                    break;
//                }
//            }
//
//            String name = chestName.replace("<name>", phatLootName);
//
//            inventory = PhatLoots.server.createInventory(chest, inventory.getSize(), name);
//            fInventory = new ForgettableInventory(PhatLoots.plugin, inventory) {
//                @Override
//                protected void execute() {
//                    inventories.remove(KEY);
//                }
//            };
//            inventories.put(KEY, fInventory);
//        }
//
//        //Forget the Inventory in the scheduled time
//        fInventory.schedule();
//
//        //Swap the Inventories
//        event.setCancelled(true);
//        InventoryView view = event.getView();
//        currentLooters.put(player, view);
//        openChests.add(view);
//        player.openInventory(inventory);
//
//        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
//            PhatLootChest plc = phatLoot.findChest(block);
//
//            if (plc != null && PhatLoots.canLoot(player, phatLoot)) {
//                phatLoot.getLoot(player, plc, inventory);
//                phatLoot.save();
//            }
//        }
//    }
//
//    /**
//     * Checks if a Player loots a PhatLootChest
//     *
//     * @param event The PlayerInteractEvent that occurred
//     */
//    @EventHandler (ignoreCancelled = true)
//    public void onPlayerCloseChest(InventoryCloseEvent event) {
//        HumanEntity entity = event.getPlayer();
//        if (!(entity instanceof Player)) {
//            return;
//        }
//
//        Player player = (Player) entity;
//        if (currentLooters.containsKey(player)) {
//            InventoryView view = currentLooters.remove(player);
//            openChests.remove(view);
//            if (!openChests.contains(view)) {
//                view.close();
//            }
//        }
//    }

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Chest chest = null;
        Inventory inventory = null;
        Block block = event.getClickedBlock();
        switch (block.getType()) {
        case DISPENSER:
            //Return if the Dispenser was not punched
            if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                return;
            }

            //Return if the Dispenser is not a PhatLootChest
            if (!isPhatLootChest(block)) {
                return;
            }

            Dispenser dispenser = (Dispenser) block.getState();
            inventory = dispenser.getInventory();

            break;

        case CHEST: //Fall through
        case ENDER_CHEST:
            //Return if the Chest was not opened
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (block.getType() == Material.CHEST) {
                chest = (Chest) block.getState();
                inventory = chest.getInventory();

                //We only care about the left side because that is the Block that would be linked
                if (inventory instanceof DoubleChestInventory) {
                    chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                    block = chest.getBlock();
                }
            }

            //Return if the Chest is not a PhatLootChest
            LinkedList<PhatLoot> phatLoots = PhatLoots.getPhatLoots(block);
            if (phatLoots.isEmpty()) {
                return;
            }

            boolean global = true;
            for (PhatLoot phatLoot: phatLoots) {
                if (!phatLoot.global) {
                    global = false;
                }
            }

            //Create the custom key using the Player Name and Block location
            final String KEY = (global ? "global" : player.getName())
                                + "@" + block.getLocation().toString();

            //Grab the custom Inventory belonging to the Player
            ForgettableInventory fInventory = inventories.get(KEY);
            if (fInventory != null) {
                inventory = fInventory.getInventory();
            } else {
                String phatLootName = "PhatLoots Chest";
                for (PhatLoot phatLoot: phatLoots) {
                    if (PhatLoots.canLoot(player, phatLoot)) {
                        phatLootName = phatLoot.name;
                        break;
                    }
                }

                phatLootName = phatLootName.replace('_', ' ');
                String name = chestName.replace("<name>", phatLootName);

                //Create a new Inventory for the Player
                inventory = PhatLoots.server.createInventory(chest,
                                                             inventory == null
                                                             ? 27
                                                             : inventory.getSize()
                                                             , name);

                fInventory = new ForgettableInventory(PhatLoots.plugin, inventory) {
                    @Override
                    protected void execute() {
                        inventories.remove(KEY);
                    }
                };
                inventories.put(KEY, fInventory);
            }

            //Forget the Inventory in the scheduled time
            fInventory.schedule();

            //Swap the Inventories
            event.setCancelled(true);
            player.openInventory(inventory);
            player.playSound(block.getLocation(), Sound.CHEST_OPEN, 0.75F, 0.95F);

            break;

        default: return;
        }

        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest plChest = phatLoot.findChest(block);
            if (plChest != null && PhatLoots.canLoot(player, phatLoot)) {
                phatLoot.getLoot(player, plChest, inventory);
                phatLoot.save();
            }
        }
    }

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Chest) {
            HumanEntity human = event.getPlayer();
            if (human instanceof Player) {
                Player player = (Player) human;
                Location location = ((Chest) holder).getLocation();
                String key = "global@" + location.toString();
                if (inventories.containsKey(key)
                        || inventories.containsKey(player.getName() + key.substring(6))) {
                    player.playSound(location, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                }
            }
        }
    }

    /**
     * Checks if a PhatLootChest is powered
     *
     * @param event The BlockPhysicsEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onBlockPowered(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.DISPENSER) {
            return;
        }

        if (block.getBlockPower() == 0) {
            return;
        }

        //Return if the Dispenser is not a PhatLootChest
        if (!isPhatLootChest(block)) {
            return;
        }

        Player player = getNearestPlayer(block.getLocation());
        if (player == null) {
            return;
        }

        //Return if the Player does not have permission to receive loots
        if (!PhatLoots.hasPermission(player, "use")) {
            player.sendMessage(PhatLootsMessages.permission);
            return;
        }

        Dispenser dispenser = (Dispenser) block.getState();
        Inventory inventory = dispenser.getInventory();

        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            if (chest != null) {
                phatLoot.getLoot(player, chest, inventory);
                phatLoot.save();
            }
        }
    }

    /**
     * Prevents non-admins from breaking PhatLootsChests
     *
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        //Return if the Material of the Block is not a Chest or Furnace
        Block block = event.getBlock();
        switch (block.getType()) {
            case ENDER_CHEST: break;
            case CHEST: break;
            case FURNACE: break;
            default: return;
        }

        if (!isPhatLootChest(block)) {
            return;
        }

        //Cancel if the Block was not broken by a Player
        Player player = event.getPlayer();
        if (player == null) {
            event.setCancelled(true);
            return;
        }

        //Cancel if the Block was not broken by an Admin
        if (!PhatLoots.hasPermission(player, "admin")) {
            player.sendMessage(PhatLootsMessages.permission);
            event.setCancelled(true);
        }
    }

    /**
     * Manages Mob drops
     *
     * @param event The EntityDeathEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot(event.getEntityType().getName());
        if (phatLoot != null) {
            event.setDroppedExp(phatLoot.getLoot(
                    event.getEntity().getKiller(), event.getDrops()));
        }
    }

    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public boolean isPhatLootChest(Block block) {
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            if (phatLoot.findChest(block) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Player that is closest to the given Location
     * Returns null if no Players are within 50 Blocks
     *
     * @param location The given Location
     * @return the closest Player
     */
    private Player getNearestPlayer(Location location) {
        Player nearestPlayer = null;
        double shortestDistance = 50;
        for (Player player: location.getWorld().getPlayers()) {
            Location playerLocation = player.getLocation();
            double distanceToPlayer = location.distance(playerLocation);
            if (distanceToPlayer < shortestDistance) {
                nearestPlayer = player;
                shortestDistance = distanceToPlayer;
            }
        }
        return nearestPlayer;
    }
}

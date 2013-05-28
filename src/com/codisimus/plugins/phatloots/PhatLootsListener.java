package com.codisimus.plugins.phatloots;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    static String chestName;
    static EnumMap<Material, HashMap<String, String>> types = new EnumMap(Material.class);
    static HashMap<String, PhatLoot> infoViewers = new HashMap<String, PhatLoot>();
    private static HashMap<String, ForgettableInventory> inventories = new HashMap<String, ForgettableInventory>();

    /**
     * Checks if a Player loots a PhatLootChest
     *
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Material type = block.getType();
        if (!types.containsKey(type)) {
            return;
        }

        Player player = event.getPlayer();
        Inventory inventory = null;
        LinkedList<PhatLoot> phatLoots = null;

        if (types.get(type) != null) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            HashMap<String, String> map = types.get(type);
            PhatLoot phatLoot;
            String world = player.getWorld().getName();
            if (map.containsKey(world)) {
                phatLoot = PhatLoots.getPhatLoot(map.get(world));
            } else {
                phatLoot = PhatLoots.getPhatLoot(map.get("all"));
            }
            if (phatLoot == null) {
                PhatLoots.logger.warning("PhatLoot " + types.get(type) + " does not exist");
            }

            phatLoots = new LinkedList<PhatLoot>();
            phatLoots.add(phatLoot);
        }

        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            if (type == Material.DISPENSER) {
                if (phatLoots == null) {
                    //Return if the Dispenser is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                Dispenser dispenser = (Dispenser) block.getState();
                inventory = dispenser.getInventory();
                break;
            }
            return;

        case RIGHT_CLICK_BLOCK:
            switch (type) {
            case TRAPPED_CHEST:
            case CHEST:
                Chest chest = (Chest) block.getState();
                inventory = chest.getInventory();

                if (phatLoots == null) {
                    //We only care about the left side because that is the Block that would be linked
                    if (inventory instanceof DoubleChestInventory) {
                        chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                        block = chest.getBlock();
                    }

                    //Return if the Chest is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                boolean individual = false;
                for (PhatLoot phatLoot : phatLoots) {
                    if (!phatLoot.global) {
                        individual = true;
                        break;
                    }
                }

                if (individual) {
                    //Create the custom key using the Player Name and Block location
                    final String KEY = player.getName() + "@" + block.getLocation().toString();

                    //Grab the custom Inventory belonging to the Player
                    ForgettableInventory fInventory = inventories.get(KEY);
                    if (fInventory != null) {
                        inventory = fInventory.getInventory();
                    } else {
                        String name = chestName.replace("<name>", phatLoots.getFirst().name.replace('_', ' '));

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
                    PhatLoots.openInventory(player, inventory, block.getLocation(), false);
                }

                break;

            default:
                if (phatLoots == null) {
                    //Return if the Block is not a PhatLootChest
                    phatLoots = PhatLoots.getPhatLoots(block, player);
                    if (phatLoots.isEmpty()) {
                        return;
                    }
                }

                boolean global = true;
                for (PhatLoot phatLoot : phatLoots) {
                    if (!phatLoot.global) {
                        global = false;
                        break;
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
                    String name = chestName.replace("<name>", phatLoots.getFirst().name.replace('_', ' '));

                    //Create a new Inventory for the Player
                    inventory = PhatLoots.server.createInventory(null,
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

                if (type == Material.ENDER_CHEST) {
                    //Swap the Inventories
                    event.setCancelled(true);
                    PhatLoots.openInventory(player, inventory, block.getLocation(), global);
                }
                player.openInventory(inventory);

                break;
            }
            break;

        default: return;
        }

        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : phatLoots) {
            phatLoot.rollForLoot(player, plChest, inventory);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInvClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (human instanceof Player) {
            Player player = (Player) human;
            if (infoViewers.containsKey(player.getName())) {
                event.setResult(Event.Result.DENY);
                player.updateInventory();
                ItemStack stack = event.getCurrentItem();
                if (stack == null) {
                    return;
                }
                switch (stack.getType()) {
                case ENDER_CHEST:
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        String name = stack.getItemMeta().getDisplayName();
                        if (name.endsWith(" (Collection)")) {
                            name = name.substring(2, name.length() - 13);
                            viewCollection(player, name);
                        }
                    }
                    break;
                case LADDER:
                    ItemMeta details = stack.getItemMeta();
                    if (details.hasDisplayName()) {
                        String name = stack.getItemMeta().getDisplayName();
                        if (name.equals("§2Back to top...")) {
                            viewPhatLoot(player, infoViewers.get(player.getName()));
                        }
//                        if (name.equals("§2Up to...")) {
//                            List<String> lore = details.getLore();
//                            if (lore.get(0).substring(2).equals("Collection")) {
//                                viewCollection(player, lore.get(1).substring(2));
//                            } else {
//                                viewPhatLoot(player, infoViewers.get(player.getName()));
//                            }
//                        }
                    }
                    break;
                default:
                    break;
                }
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
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (holder instanceof Chest) {
            HumanEntity human = event.getPlayer();
            if (human instanceof Player) {
                Player player = (Player) human;
                if (infoViewers.containsKey(player.getName())) {
                    infoViewers.remove(player.getName());
                } else {
                    Location location = ((Chest) holder).getLocation();
                    String key = "global@" + location.toString();
                    if (inventories.containsKey(key)) {
                        PhatLoots.closeInventory(player, inv, location, true);
                    } else if (inventories.containsKey(player.getName() + key.substring(6))) {
                        PhatLoots.closeInventory(player, inv, location, false);
                    }
                }
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

        //Return if the Block is not a PhatLootChest
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
        if (!player.hasPermission("phatloots.admin")) {
            player.sendMessage(PhatLootsConfig.permission);
            event.setCancelled(true);
        }
    }

    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public boolean isPhatLootChest(Block block) {
        PhatLootChest plChest = new PhatLootChest(block);
        for (PhatLoot phatLoot : PhatLoots.getPhatLoots()) {
            if (phatLoot.containsChest(plChest)) {
                return true;
            }
        }
        return false;
    }

    public static void viewPhatLoot(final Player player, final PhatLoot phatLoot) {
        String invName = phatLoot.name + " Loot Tables";
        if (invName.length() > 32) {
            invName = phatLoot.name;
            if (invName.length() > 32) {
                invName = phatLoot.name.substring(0, 32);
            }
        }

        final Inventory inv = Bukkit.createInventory(player, 54, invName);
        int index = 0;
        for (Loot loot : phatLoot.lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= 54) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        final InventoryView view = new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return inv;
            }

            @Override
            public Inventory getBottomInventory() {
                return Bukkit.createInventory(null, 0);
            }

            @Override
            public HumanEntity getPlayer() {
                return player;
            }

            @Override
            public InventoryType getType() {
                return InventoryType.CHEST;
            }
        };

        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                infoViewers.put(player.getName(), phatLoot);
                player.openInventory(inv);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    public static void viewCollection(final Player player, String name) {
        final PhatLoot phatLoot = infoViewers.get(player.getName());
        LootCollection coll = phatLoot.findCollection(name);
        String invName = name + " (Collection)";
        if (invName.length() > 32) {
            invName = name;
            if (invName.length() > 32) {
                invName = name.substring(0, 32);
            }
        }

        final Inventory inv = Bukkit.createInventory(player, 54, invName);
        int index = 0;
        for (Loot loot : coll.lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= 53) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        ItemStack infoStack = new ItemStack(Material.LADDER);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2Back to top...");
        infoStack.setItemMeta(info);
        inv.setItem(53, infoStack);

        final InventoryView view = new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return inv;
            }

            @Override
            public Inventory getBottomInventory() {
                return Bukkit.createInventory(null, 0);
            }

            @Override
            public HumanEntity getPlayer() {
                return player;
            }

            @Override
            public InventoryType getType() {
                return InventoryType.CHEST;
            }
        };

        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                infoViewers.put(player.getName(), phatLoot);
                player.openInventory(inv);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }
}

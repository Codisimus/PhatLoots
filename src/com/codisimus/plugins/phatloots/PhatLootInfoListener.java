package com.codisimus.plugins.phatloots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for interactions with PhatLoot info GUIs
 *
 * @author Cody
 */
public class PhatLootInfoListener implements Listener {
    private final static int SIZE = 54;
    static HashMap<String, Boolean> switchingPages = new HashMap<String, Boolean>(); //Player name -> Going deeper (true) or back (false)
    static HashMap<String, PhatLoot> infoViewers = new HashMap<String, PhatLoot>(); //Player name -> PhatLoot they are viewing
    static HashMap<String, Stack<Inventory>> pageStacks = new HashMap<String, Stack<Inventory>>(); //Player name -> Page history

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInvClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }
        //Return if the Player is not viewing a PhatLoot's info
        Player player = (Player) human;
        if (!infoViewers.containsKey(player.getName())) {
            return;
        }

        //Don't allow any inventory clicking
        event.setResult(Event.Result.DENY);
        player.updateInventory();

        //Check if they clicked something to change pages
        ItemStack stack = event.getCurrentItem();
        if (stack == null) {
            return;
        }
        switch (stack.getType()) {
        case ENDER_CHEST: //Enter LootCollection
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                //Get the collection name from the item's display name
                String name = stack.getItemMeta().getDisplayName();
                if (name.endsWith(" (Collection)")) {
                    name = name.substring(2, name.length() - 13);
                    viewCollection(player, name);
                }
            }
            break;
        case LADDER: //Go back in page history
            ItemMeta details = stack.getItemMeta();
            if (details.hasDisplayName()) {
                String name = stack.getItemMeta().getDisplayName();
                if (name.equals("§2Up to...")) {
                    up(player);
                } else if (name.equals("§2Back to top...")) {
                    viewPhatLoot(player, infoViewers.get(player.getName()));
                }
            }
            break;
        default:
            break;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player)) {
            return;
        }
        //Return if the Player is not viewing a PhatLoot's info
        Player player = (Player) human;
        String playerName = player.getName();
        if (!infoViewers.containsKey(playerName)) {
            return;
        }

        if (switchingPages.containsKey(playerName)) { //Switching pages
            if (switchingPages.get(playerName)) { //Going deeper
                pageStacks.get(playerName).add(event.getInventory());
            }
            //The Player has finished switching pages
            switchingPages.remove(playerName);
        } else { //Closing info view
            infoViewers.remove(playerName);
            pageStacks.get(playerName).empty();
            pageStacks.remove(playerName);
        }
    }

    /**
     * Opens an Inventory GUI for the given PhatLoot to the given Player
     *
     * @param player The given Player
     * @param phatLoot The given PhatLoot
     */
    public static void viewPhatLoot(final Player player, final PhatLoot phatLoot) {
        //Create the Inventory
        String invName = phatLoot.name + " Loot Tables";
        if (invName.length() > 32) {
            invName = phatLoot.name;
            if (invName.length() > 32) {
                invName = phatLoot.name.substring(0, 32);
            }
        }
        final Inventory inv = Bukkit.createInventory(player, SIZE, invName);

        int index = SIZE;
        ItemStack infoStack;
        ItemMeta info;
        String amount;

        //Show the money Range
        if (PhatLoots.econ != null) {
            infoStack = new ItemStack(Material.GOLD_NUGGET);
            info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
            amount = String.valueOf(phatLoot.moneyLower);
            if (phatLoot.moneyUpper != phatLoot.moneyLower) {
                amount += '-' + String.valueOf(phatLoot.moneyUpper);
            }
            info.setDisplayName("§6" + amount + ' ' + PhatLoots.econ.currencyNamePlural());
            infoStack.setItemMeta(info);
            index--;
            inv.setItem(index, infoStack);
        }

        //Show the experience Range
        infoStack = new ItemStack(Material.EXP_BOTTLE);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        amount = String.valueOf(phatLoot.expLower);
        if (phatLoot.expUpper != phatLoot.expLower) {
            amount += '-' + String.valueOf(phatLoot.expUpper);
        }
        info.setDisplayName("§6" + amount + " exp");
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Show the Reset Time
        infoStack = new ItemStack(Material.WATCH);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2Reset Time");
        List<String> details = new ArrayList();
        details.add("§4Days: §6" + phatLoot.days);
        details.add("§4Hours: §6" + phatLoot.hours);
        details.add("§4Minutes: §6" + phatLoot.minutes);
        details.add("§4Seconds: §6" + phatLoot.seconds);
        details.add("§4Reset Type: §6" + (phatLoot.global ? "Global" : "Individual"));
        if (phatLoot.round) {
            details.add("§6Time is rounded down");
        }
        info.setLore(details);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Show the autoloot status
        infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH_ON : Material.REDSTONE_TORCH_OFF);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        details.add("§4AutoLoot: §6" + phatLoot.autoLoot);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Populate the inventory
        index = 0;
        for (Loot loot : phatLoot.lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= SIZE - 9) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        //Create an InventoryView which excludes the viewer's personal inventory
//        final InventoryView view = new InventoryView() {
//            @Override
//            public Inventory getTopInventory() {
//                return Bukkit.createInventory(null, 0, invName);
//            }
//
//            @Override
//            public Inventory getBottomInventory() {
//                return inv;
//            }
//
//            @Override
//            public HumanEntity getPlayer() {
//                return player;
//            }
//
//            @Override
//            public InventoryType getType() {
//                return InventoryType.CHEST;
//            }
//        };

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        final String playerName = player.getName();
        pageStacks.put(playerName, new Stack<Inventory>());
        switchingPages.put(playerName, true);
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                infoViewers.put(playerName, phatLoot);
                player.openInventory(inv);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    /**
     * Opens an Inventory GUI for the specified LootCollection to the given Player
     *
     * @param player The given Player
     * @param name The name of the LootCollection to open
     */
    public static void viewCollection(final Player player, String name) {
        //Create the Inventory
        final PhatLoot phatLoot = infoViewers.get(player.getName());
        LootCollection coll = phatLoot.findCollection(name);
        String invName = name + " (Collection)";
        if (invName.length() > 32) {
            invName = name;
            if (invName.length() > 32) {
                invName = name.substring(0, 32);
            }
        }
        final Inventory inv = Bukkit.createInventory(player, SIZE, invName);

        //Populate the inventory
        int index = 0;
        for (Loot loot : coll.lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= SIZE - 9) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        //Create back buttons
        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Back to top...");
        item.setItemMeta(info);
        inv.setItem(SIZE - 2, item);

        item = new ItemStack(Material.LADDER);
        info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Up to...");
        List<String> details = new ArrayList();

        final String playerName = player.getName();
        switchingPages.put(playerName, true);
        player.closeInventory();
        details.add("§6" + pageStacks.get(playerName).peek().getTitle());
        info.setLore(details);
        item.setItemMeta(info);
        inv.setItem(SIZE - 1, item);

        //Create an InventoryView which excludes the viewer's personal inventory
//        final InventoryView view = new InventoryView() {
//            @Override
//            public Inventory getTopInventory() {
//                return Bukkit.createInventory(null, 0);
//            }
//
//            @Override
//            public Inventory getBottomInventory() {
//                return inv;
//            }
//
//            @Override
//            public HumanEntity getPlayer() {
//                return player;
//            }
//
//            @Override
//            public InventoryType getType() {
//                return InventoryType.CHEST;
//            }
//        };

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    /**
     * Opens the top Inventory in the given Players Stack
     *
     * @param player The given Player
     */
    public static void up(final Player player) {
        //Pop the inventory off of the Stack
        final String playerName = player.getName();
        final Stack<Inventory> stack = pageStacks.get(playerName);

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        switchingPages.put(playerName, false);
        player.closeInventory();
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(stack.pop());
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }
}

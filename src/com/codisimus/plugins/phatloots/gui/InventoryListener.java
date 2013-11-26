package com.codisimus.plugins.phatloots.gui;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.Item;
import com.codisimus.plugins.phatloots.loot.Loot;
import com.codisimus.plugins.phatloots.loot.LootCollection;
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
import org.bukkit.event.inventory.ClickType;
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
public class InventoryListener implements Listener {
    private final static int SIZE = 54;
    final static int TOOL_SLOT = SIZE - 9;
    private static HashMap<String, Boolean> switchingPages = new HashMap<String, Boolean>(); //Player name -> Going deeper (true) or back (false)
    private static HashMap<String, PhatLoot> infoViewers = new HashMap<String, PhatLoot>(); //Player name -> PhatLoot they are viewing
    private static HashMap<String, Stack<Inventory>> pageStacks = new HashMap<String, Stack<Inventory>>(); //Player name -> Page history
    private static HashMap<String, Loot> holding = new HashMap<String, Loot>();
    private static HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();

    /**
     * Registers a Button to be added to GUI pages
     *
     * @param button The Button to register
     */
    public static void registerButton(Button button) {
        buttons.put(button.getSlot(), button);
    }

    /**
     * Returns the List of loot for the current Inventory
     *
     * @param phatLoot The PhatLoot that contains the Loot
     * @param inv The current Inventory
     * @return The List of loot for the PhatLoot/LootCollection
     */
    private static List<Loot> getLootList(PhatLoot phatLoot, Inventory inv) {
        LootCollection coll = null;
        String invName = inv.getName();
        if (invName.endsWith(" (Collection)")) {
            coll = phatLoot.findCollection(invName.substring(0, invName.length() - 13));
        }
        return coll == null ? phatLoot.lootList : coll.getLootList();
    }

    /** LISTENERS **/

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInvClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player)) {
            return;
        }
        //Return if the Player is not viewing a PhatLoot's info
        Player player = (Player) human;
        String playerName = player.getName();
        if (!infoViewers.containsKey(playerName)) {
            return;
        }

        //Don't allow any inventory clicking
        event.setResult(Event.Result.DENY);
        player.updateInventory();

        //Store popularly accessed variables
        PhatLoot phatLoot = infoViewers.get(playerName);
        Inventory inv = event.getInventory();
        Tool tool = Tool.getTool(inv.getItem(TOOL_SLOT));
        ItemStack stack = event.getCurrentItem();
        List<Loot> lootList = getLootList(phatLoot, inv);
        int slot = event.getRawSlot();
        Loot loot = slot >= 0 && lootList.size() > slot ? lootList.get(slot) : null;

        //Check if the Player is holding a Loot
        if (holding.containsKey(playerName)) {
            switch (event.getClick()) {
            case LEFT: //Allow
                if (loot == null) {
                    if (slot == lootList.size()) { //Put down Loot
                        lootList.add(holding.remove(playerName));
                        event.setCurrentItem(event.getCursor());
                    } else if (slot > lootList.size() && slot < TOOL_SLOT) { //Support adding the Loot to any slot
                        lootList.add(holding.remove(playerName));
                        inv.setItem(lootList.size() - 1, event.getCursor());
                        player.updateInventory();
                    } else if (slot == -999) { //Remove Loot
                        holding.remove(playerName);
                    } else {
                        break;
                    }
                    event.setCursor(null);
                    return;
                }
                break;
            case RIGHT: //Go back a page
                if (slot < 45) { //Minecraft crash avoidance
                    up(player);
                }
                return;
            default: //Deny all other actions
                return;
            }
        }

        /** Switch Tools **/
        if (slot == -999 || slot == TOOL_SLOT) {
            switch (event.getClick()) {
            case LEFT: //Previous Tool
                //Deny switching toos while holding Loot
                if (!holding.containsKey(playerName)) {
                    inv.setItem(TOOL_SLOT, tool.prevTool().getItem());
                    player.updateInventory();
                }
                break;
            case RIGHT: //Next Tool
                inv.setItem(TOOL_SLOT, tool.nextTool().getItem());
                player.updateInventory();
                break;
            default: //Do nothing
                break;
            }
            return;
        }

        /** Go back to a previous View **/
        if (stack.getType() == Material.LADDER) {
            ItemMeta details = stack.getItemMeta();
            if (details.hasDisplayName()) {
                String name = stack.getItemMeta().getDisplayName();
                if (name.equals("§2Up to...")) { //Back one view
                    up(player);
                    return;
                } else if (name.equals("§2Back to top...")) { //Back to first view
                    viewPhatLoot(player, phatLoot);
                    return;
                }
            }
        }

        /** Check if a Button was Clicked **/
        if (buttons.containsKey(slot)) {
            if (buttons.get(slot).onClick(inv, phatLoot, lootList)) {
                refreshPage(player, inv, lootList);
            }
            return;
        }

        /** No Loot was Clicked **/
        if (loot == null) {
            if (tool == Tool.NAVIGATE_AND_MOVE) {
                switch (event.getClick()) {
                case LEFT: //Pickup or put down an Item
                    if (slot >= SIZE) {
                        //Remove Loot (if holding)
                        Loot l = holding.remove(playerName);
                        if (stack != null) {
                            //Only pick up AIR if they are not setting an Item down
                            if (stack.getType() != Material.AIR || l == null) { //Pick up Item (Add loot)
                                loot = new Item(stack, 0);
                                holding.put(playerName, loot);
                                event.setCursor(loot.getInfoStack());
                                event.setCurrentItem(null);
                            } else { //Pick up nothing
                                event.setCursor(null);
                            }
                        }
                        if (l != null && l instanceof Item) { //Put down Item
                            event.setCurrentItem(((Item) l).getItem());
                        }
                        break;
                    }
                    break;
                case RIGHT: //Go back a page
                    up(player);
                    break;
                case MIDDLE: //Add an Item as Loot
                    if (slot > SIZE) {
                        ItemStack item = stack == null ? new ItemStack(Material.AIR) : stack.clone();
                        lootList.add(new Item(item, 0));
                        refreshPage(player, inv, lootList);
                    }
                }
                return;
            }

            int amount = 0;
            boolean both = false;
            if (tool == Tool.MODIFY_AMOUNT) {
                switch (event.getClick()) {
                case LEFT: //+1 amount
                    amount = 1;
                    both = true;
                    break;
                case DOUBLE_CLICK: //+9 amount
                    amount = 9;
                    both = true;
                    break;
                case RIGHT: //-1 amount
                    amount = -1;
                    both = true;
                    break;
                case SHIFT_LEFT: //+1 upper amount
                    amount = 1;
                    both = false;
                    break;
                case SHIFT_RIGHT: //-1 upper amount
                    amount = -1;
                    both = false;
                    break;
                case MIDDLE: //Set amount to 0
                    amount = 0;
                    break;
                default:
                    return;
                }
            }

            if (inv.getTitle().endsWith("Loot Tables")) { //Default View
                ItemStack infoStack;
                ItemMeta info = Bukkit.getItemFactory().getItemMeta(Material.STONE); //Block type doesn't really matter
                List<String> details = new ArrayList();

                switch (slot) {
                case SIZE - 5: //Toggle Break and Respawn
                    if (tool == Tool.MODIFY_AMOUNT) {
                        return;
                    }
                    phatLoot.breakAndRespawn = !phatLoot.breakAndRespawn;

                    //Show the break and respawn status
                    infoStack = new ItemStack(phatLoot.breakAndRespawn ? Material.MOB_SPAWNER : Material.CHEST);
                    info.setDisplayName("§4Break and Respawn: §6" + phatLoot.breakAndRespawn);
                    if (phatLoot.breakAndRespawn) {
                        details.add("§6This chest will break after it is looted");
                        details.add("§6and respawn once it may be looted again.");
                    } else {
                        details.add("§6This chest will always be present");
                        details.add("§6even after it is looted.");
                    }
                    break;

                case SIZE - 4: //Toggle AutoLoot
                    if (tool == Tool.MODIFY_AMOUNT) {
                        return;
                    }
                    phatLoot.autoLoot = !phatLoot.autoLoot;

                    //Show the autoloot status
                    infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH_ON : Material.REDSTONE_TORCH_OFF);
                    info.setDisplayName("§4AutoLoot: §6" + phatLoot.autoLoot);
                    break;

                case SIZE - 3: //Toggle Global/Round or Modify Reset Time
                    if (tool == Tool.MODIFY_AMOUNT) {
                        if (amount == 0) {
                            phatLoot.days = 0;
                            phatLoot.hours = 0;
                            phatLoot.minutes = 0;
                            phatLoot.seconds = 0;
                        } else if (both) {
                            phatLoot.hours += amount;
                        } else {
                            phatLoot.minutes += amount;
                        }
                    } else if (event.getClick() == ClickType.LEFT) {
                        phatLoot.global = !phatLoot.global;
                    } else {
                        phatLoot.round = !phatLoot.round;
                    }

                    //Show the Reset Time
                    infoStack = new ItemStack(Material.WATCH);
                    info.setDisplayName("§2Reset Time");
                    details.add("§4Days: §6" + phatLoot.days);
                    details.add("§4Hours: §6" + phatLoot.hours);
                    details.add("§4Minutes: §6" + phatLoot.minutes);
                    details.add("§4Seconds: §6" + phatLoot.seconds);
                    details.add("§4Reset Type: §6" + (phatLoot.global ? "Global" : "Individual"));
                    if (phatLoot.round) {
                        details.add("§6Time is rounded down");
                    }
                    break;

                case SIZE - 2: //Modify Experience
                    if (tool == Tool.MODIFY_PROBABILITY_AND_TOGGLE) {
                        return;
                    }
                    if (amount == 0) {
                        phatLoot.expLower = 0;
                        phatLoot.expUpper = 0;
                    } else {
                        if (both) {
                            phatLoot.expLower += amount;
                        }
                        phatLoot.expUpper += amount;
                    }

                    //Show the experience Range
                    infoStack = new ItemStack(Material.EXP_BOTTLE);
                    String exp = String.valueOf(phatLoot.expLower);
                    if (phatLoot.expUpper != phatLoot.expLower) {
                        exp += '-' + String.valueOf(phatLoot.expUpper);
                    }
                    info.setDisplayName("§6" + exp + " exp");
                    break;

                case SIZE - 1: //Modify Money
                    if (tool == Tool.MODIFY_PROBABILITY_AND_TOGGLE) {
                        return;
                    }
                    if (amount == 0) {
                        phatLoot.moneyLower = 0;
                        phatLoot.moneyUpper = 0;
                    } else {
                        if (both) {
                            phatLoot.moneyLower += amount;
                        }
                        phatLoot.moneyUpper += amount;
                    }

                    //Show the money Range
                    infoStack = new ItemStack(Material.GOLD_NUGGET);
                    String money = String.valueOf(phatLoot.moneyLower);
                    if (phatLoot.moneyUpper != phatLoot.moneyLower) {
                        money += '-' + String.valueOf(phatLoot.moneyUpper);
                    }

                    info.setDisplayName(PhatLoots.econ == null
                                        ? "§6No Economy plugin detected!"
                                        : "§6" + money + ' ' + PhatLoots.econ.currencyNamePlural());
                    break;

                default:
                    return;
                }

                info.setLore(details);
                infoStack.setItemMeta(info);
                event.setCurrentItem(infoStack);
            }
            return;
        }

        /** Action determined based on the Tool **/
        switch (tool) {
        case NAVIGATE_AND_MOVE:
            switch (event.getClick()) {
            case LEFT: //Move Loot or Enter a Collection
                if (stack.getType() == Material.ENDER_CHEST) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        //Get the collection name from the item's display name
                        String name = stack.getItemMeta().getDisplayName();
                        if (name.endsWith(" (Collection)")) { //Clicked a LootCollection
                            name = name.substring(2, name.length() - 13);
                            if (holding.containsKey(playerName)) { //Place Loot in Collection
                                Loot l = holding.remove(playerName);
                                phatLoot.findCollection(name).addLoot(l);
                                event.setCursor(null);
                            } else { //Enter LootCollection
                                viewCollection(player, name);
                            }
                            return;
                        }
                    }
                }

                //Clicked some other Loot
                if (holding.containsKey(playerName)) { //Swap Loot
                    Loot l = holding.remove(playerName);
                    holding.put(playerName, lootList.get(slot));
                    lootList.set(slot, l);
                    event.setCurrentItem(event.getCursor()); //Put down Loot
                    event.setCursor(stack); //Pick up new Loot
                } else { //Pick up Loot
                    holding.put(playerName, lootList.remove(slot));
                    event.setCursor(stack);
                    refreshPage(player, inv, lootList); //Shifts remaining loot down
                }
                break;
            case RIGHT: //Go back a page
                up(player);
                break;
            case SHIFT_LEFT: //Move Loot left
                if (loot != null && slot > 0) {
                    lootList.set(slot, lootList.get(slot - 1));
                    lootList.set(slot - 1, loot);
                    refreshPage(player, inv, lootList);
                }
                break;
            case SHIFT_RIGHT: //Move Loot right
                if (slot < lootList.size() + 1) {
                    lootList.set(slot, lootList.get(slot + 1));
                    lootList.set(slot + 1, loot);
                    refreshPage(player, inv, lootList);
                }
                break;
            case MIDDLE: //Remove Loot
                lootList.remove(slot);
                refreshPage(player, inv, lootList); //Shifts remaining loot down
            }
            break;

        case MODIFY_PROBABILITY_AND_TOGGLE:
            switch (event.getClick()) {
            case DOUBLE_CLICK: //+9%
                loot.setProbability(loot.getProbability() + 8);
                //Fall through
            case LEFT: //+1%
                loot.setProbability(loot.getProbability() + 1);
                while (loot.getProbability() > 100) {
                    loot.setProbability(loot.getProbability() - 100);
                }
                break;
            case RIGHT: //-1%
                loot.setProbability(loot.getProbability() - 1);
                while (loot.getProbability() < 0) {
                    loot.setProbability(loot.getProbability() + 100);
                }
                break;
            default:
                if (loot.onToggle(event.getClick())) {
                    break;
                } else {
                    return;
                }
            }
            event.setCurrentItem(loot.getInfoStack());
            break;

        case MODIFY_AMOUNT:
            int amount;
            boolean both;
            switch (event.getClick()) {
            case LEFT: //+1 amount
                amount = 1;
                both = true;
                break;
            case DOUBLE_CLICK: //+9 amount
                amount = 9;
                both = true;
                break;
            case RIGHT: //-1 amount
                amount = -1;
                both = true;
                break;
            case SHIFT_LEFT: //+1 upper amount
                amount = 1;
                both = false;
                break;
            case SHIFT_RIGHT: //-1 upper amount
                amount = -1;
                both = false;
                break;
            case MIDDLE: //Set amount to 1
                if (loot.resetAmount()) {
                    event.setCurrentItem(loot.getInfoStack());
                }
                return;
            default:
                return;
            }

            if (loot.modifyAmount(amount, both)) {
                event.setCurrentItem(loot.getInfoStack());
            }
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
            infoViewers.remove(playerName).save(); //Save the PhatLoot in case it has been modified
            pageStacks.get(playerName).empty();
            pageStacks.remove(playerName);
        }

        //Don't drop items
        if (holding.containsKey(playerName)) {
            holding.remove(playerName);
            event.getView().setCursor(null);
        }
    }

    /** END LISTENERS **/
    /** INVENTORY VIEWS **/

    /**
     * Opens an Inventory GUI for the given PhatLoot to the given Player
     *
     * @param player The given Player
     * @param phatLoot The given PhatLoot
     */
    public static void viewPhatLoot(Player player, PhatLoot phatLoot) {
        //Get the Inventory title
        String invName;
        if (phatLoot.name.length() <= 20) {
            invName = phatLoot.name + " Loot Tables";
        } else if (phatLoot.name.length() <= 32) {
            invName = phatLoot.name;
        } else {
            invName = phatLoot.name.substring(0, 32);
        }

        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, SIZE, invName);

        int index = SIZE;
        ItemStack infoStack;
        ItemMeta info;
        String amount;

        //Display the money Range
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

        //Display the experience Range
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

        //Display the Reset Time
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

        //Display the autoloot status
        infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH_ON : Material.REDSTONE_TORCH_OFF);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§4AutoLoot: §6" + phatLoot.autoLoot);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Display the break and respawn status
        infoStack = new ItemStack(phatLoot.breakAndRespawn ? Material.MOB_SPAWNER : Material.CHEST);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§4Break and Respawn: §6" + phatLoot.breakAndRespawn);
        details = new ArrayList();
        if (phatLoot.breakAndRespawn) {
            details.add("§6This chest will break after it is looted");
            details.add("§6and respawn once it may be looted again.");
        } else {
            details.add("§6This chest will always be present");
            details.add("§6even after it is looted.");
        }
        info.setLore(details);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Store the current view in the Player's stack
        pageStacks.put(player.getName(), new Stack<Inventory>());

        infoViewers.put(player.getName(), phatLoot);
        switchView(player, inv);
    }

    /**
     * Opens an Inventory GUI for the specified LootCollection to the given Player
     *
     * @param player The given Player
     * @param name The name of the LootCollection to open
     */
    private static void viewCollection(Player player, String name) {
        //Store the current view in the Player's stack
        pageStacks.get(player.getName()).add(player.getOpenInventory().getTopInventory());

        //Get the Inventory title
        String invName;
        if (name.length() < 20) {
            invName = name + " (Collection)";
        } else if (name.length() <= 32) {
            invName = name;
        } else {
            invName = name.substring(0, 32);
        }

        //Create the Inventory view
        Inventory inv = Bukkit.createInventory(null, SIZE, invName);

        //Create the Back to top button
        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Back to top...");
        item.setItemMeta(info);
        inv.setItem(SIZE - 2, item);

        //Create the Up button
        item = new ItemStack(Material.LADDER);
        info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Up to...");
        List<String> details = new ArrayList();
        details.add("§6" + pageStacks.get(player.getName()).peek().getTitle());
        info.setLore(details);
        item.setItemMeta(info);
        inv.setItem(SIZE - 1, item);

        switchView(player, inv);
    }

    /**
     * Opens the top Inventory in the given Player's Stack
     *
     * @param player The given Player
     */
    private static void up(Player player) {
        if (!pageStacks.isEmpty()) {
            switchView(player, pageStacks.get(player.getName()).pop());
        }
    }

    /**
     * Switches the Player to viewing the given Inventory
     *
     * @param player The Player to open the Inventory
     * @param inv The Inventory to open
     */
    private static void switchView(final Player player, final Inventory inv) {
        String playerName = player.getName();
        switchingPages.put(playerName, false);

        //Get the Loot that the Player is holding
        final ItemStack hand = player.getItemOnCursor();
        Loot loot = holding.get(playerName);

        //Close the old InventoryView
        player.closeInventory();

        //Re-add the Loot if there was any (it is removed onInventoryClose)
        if (loot != null) {
            holding.put(playerName, loot);
        }

        //Display the default Tool
        inv.setItem(TOOL_SLOT, Tool.NAVIGATE_AND_MOVE.getItem());

        //Add Buttons to view
        for (int i = 1; i < buttons.size(); i++) {
            inv.setItem(TOOL_SLOT + i, buttons.get(i).getItem());
        }

        //Populate the inventory
        refreshPage(player, inv, getLootList(infoViewers.get(player.getName()), inv));

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        new BukkitRunnable() {
            @Override
            public void run() {
                //Open the Inventory and place the ItemStack on the cursor
                player.openInventory(inv).setCursor(hand);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    /**
     * Refreshes each InfoView within the Inventory
     *
     * @param player The Player viewing the Inventory
     * @param inv The Inventory being viewed
     * @param lootList The list of loot to display
     */
    private static void refreshPage(Player player, Inventory inv, List<Loot> lootList) {
        //Populate the inventory with the Loot items
        int index = 0;
        for (Loot loot : lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= TOOL_SLOT) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }
        //Clear the rest of the Loot slots
        while (index < TOOL_SLOT) {
            inv.clear(index);
            index++;
        }
        player.updateInventory();
    }

    /** END INVENTORY VIEWS **/
}

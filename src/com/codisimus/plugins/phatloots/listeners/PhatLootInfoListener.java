package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.Item;
import com.codisimus.plugins.phatloots.loot.Loot;
import com.codisimus.plugins.phatloots.loot.LootCollection;
import java.util.*;
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
public class PhatLootInfoListener implements Listener {
    private final static int SIZE = 54;
    private final static int TOOL_SLOT = SIZE - 9;
    private static enum Tool {
        NAVIGATE_AND_MOVE(0, Material.LEASH),
        MODIFY_PROBABILITY_AND_TOGGLE(1, Material.NAME_TAG),
        MODIFY_AMOUNT(2, Material.GOLD_NUGGET);

        private int id;
        private Material mat;

        private Tool(int id, Material mat) {
            this.id = id;
            this.mat = mat;
        }

        int getID() {
            return id;
        }

        ItemStack getItem() {
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(mat);
            List<String> lore = new ArrayList<String>();

            switch (this) {
            case NAVIGATE_AND_MOVE:
                meta.setDisplayName("§2Navigate/Move (Click to change Tool)");
                lore.add("§4LEFT CLICK:");
                lore.add("§6 Enter a Collection");
                lore.add("§4RIGHT CLICK:");
                lore.add("§6 Leave a Collection");
                lore.add("§4SHIFT + LEFT CLICK:");
                lore.add("§6 Shift a Loot to the Left");
                lore.add("§4SHIFT + RIGHT CLICK:");
                lore.add("§6 Shift a Loot to the Right");
                lore.add("§4SCROLL CLICK:");
                lore.add("§6 Remove a Loot/Add an Item (from inventory)");
                break;
            case MODIFY_PROBABILITY_AND_TOGGLE:
                meta.setDisplayName("§2Modify Probability/Toggle (Click to change Tool)");
                lore.add("§4LEFT CLICK:");
                lore.add("§6 +1 Probability");
                lore.add("§4DOUBLE LEFT CLICK:");
                lore.add("§6 +10 Probability");
                lore.add("§4RIGHT CLICK:");
                lore.add("§6 -1 Probability");
                lore.add("§4SHIFT + LEFT CLICK:");
                lore.add("§6 Toggle AutoEnchant/FromConsole");
                lore.add("§4SHIFT + RIGHT CLICK:");
                lore.add("§6 Toggle GenerateName/TempOP");
                lore.add("§4SCROLL CLICK:");
                lore.add("§6 Toggle TieredName and Loot table settings");
                break;
            case MODIFY_AMOUNT:
                meta.setDisplayName("§2Modify Amount (Click to change Tool)");
                lore.add("§4LEFT CLICK:");
                lore.add("§6 +1 Amount");
                lore.add("§4DOUBLE LEFT CLICK:");
                lore.add("§6 +10 Amount");
                lore.add("§4RIGHT CLICK:");
                lore.add("§6 -1 Amount");
                lore.add("§4SHIFT + LEFT CLICK:");
                lore.add("§6 +1 Amount (Upper Range)");
                lore.add("§4SHIFT + RIGHT CLICK:");
                lore.add("§6 -1 Amount (Upper Range)");
                lore.add("§4SCROLL CLICK:");
                lore.add("§6 Set Amount to 1 and Clear time/exp/money");
                break;
            default:
                break;
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        }

        Tool prevTool() {
            int toolID = id - 1;
            if (toolID < 0) {
                toolID = Tool.values().length - 1;
            }
            return getToolByID(toolID);
        }

        Tool nextTool() {
            int toolID = id + 1;
            if (toolID >= Tool.values().length) {
                toolID = 0;
            }
            return getToolByID(toolID);
        }

        static Tool getToolByID(int id) {
            for (Tool tool : Tool.values()) {
                if (tool.id == id) {
                    return tool;
                }
            }
            return null;
        }

        static Tool getTool(ItemStack item) {
            Material mat = item.getType();
            for (Tool tool : Tool.values()) {
                if (tool.mat == mat) {
                    return tool;
                }
            }
            return null;
        }
    }
    private static HashMap<String, Boolean> switchingPages = new HashMap<String, Boolean>(); //Player name -> Going deeper (true) or back (false)
    private static HashMap<String, PhatLoot> infoViewers = new HashMap<String, PhatLoot>(); //Player name -> PhatLoot they are viewing
    private static HashMap<String, Stack<Inventory>> pageStacks = new HashMap<String, Stack<Inventory>>(); //Player name -> Page history
    private static HashMap<String, Loot> holding = new HashMap<String, Loot>();

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

        //Check if the player is switching tools
        Inventory inv = event.getInventory();
        Tool tool = Tool.getTool(inv.getItem(TOOL_SLOT));
        if (event.getSlot() == -999 || event.getSlot() == TOOL_SLOT) {
            if (holding.containsKey(player.getName())) {
                return;
            }
            switch (event.getClick()) {
            case LEFT:
                inv.setItem(TOOL_SLOT, tool.prevTool().getItem());
                player.updateInventory();
                break;
            case RIGHT:
                inv.setItem(TOOL_SLOT, tool.nextTool().getItem());
                player.updateInventory();
                break;
            default:
                break;
            }
            return;
        }

        if (holding.containsKey(player.getName())) {
            switch (event.getClick()) {
            case LEFT:
                break;
            case RIGHT:
                up(player, event.getCursor());
                return;
            default:
                //Don't allow anything else while holding an item
                return;
            }
            if (event.getRawSlot() > 44) { //Minecraft crash avoidance
                return;
            }
        }

        //Check if the player is climbing a ladder
        ItemStack stack = event.getCurrentItem();
        if (stack.getType() == Material.LADDER) {
            ItemMeta details = stack.getItemMeta();
            if (details.hasDisplayName()) {
                String name = stack.getItemMeta().getDisplayName();
                if (name.equals("§2Up to...")) {
                    up(player, event.getCursor());
                    return;
                } else if (name.equals("§2Back to top...")) {
                    viewPhatLoot(player, infoViewers.get(player.getName()), event.getCursor());
                    return;
                }
            }
        }

        //Store popularly accessed variables
        PhatLoot phatLoot = infoViewers.get(player.getName());
        int slot = event.getRawSlot();
        List<Loot> lootList = getLootList(phatLoot, inv);
        Loot loot = lootList.size() > slot ? lootList.get(slot) : null;

        //Check if the player is creating a new Collection
        if (slot == TOOL_SLOT + 1) {
            int i = 1;
            while (phatLoot.findCollection(String.valueOf(i)) != null) {
                i++;
            }
            lootList.add(new LootCollection(String.valueOf(i)));
            refreshPage(player, inv, lootList);
        }

        switch (tool) {
        case NAVIGATE_AND_MOVE:
            switch (event.getClick()) {
            case LEFT: //Switch Pages
                if (stack == null && !holding.containsKey(player.getName())) {
                    return;
                }
                switch (stack.getType()) {
                case ENDER_CHEST:
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        //Get the collection name from the item's display name
                        String name = stack.getItemMeta().getDisplayName();
                        if (name.endsWith(" (Collection)")) {
                            name = name.substring(2, name.length() - 13);
                            if (holding.containsKey(player.getName())) {
                                Loot l = holding.remove(player.getName());
                                phatLoot.findCollection(name).addLoot(l);
                                event.setCursor(null);
                            } else { //Enter LootCollection
                                viewCollection(player, name, event.getCursor());
                            }
                        }
                    }
                    break;
                default:
                    if (holding.containsKey(player.getName())) {
                        if (lootList.size() < slot) {
                            return;
                        }
                        Loot l = holding.remove(player.getName());
                        if (lootList.size() > slot) {
                            holding.put(player.getName(), lootList.get(slot));
                            lootList.set(slot, l);
                        } else {
                            lootList.add(l);
                        }
                        ItemStack i = inv.getItem(slot);
                        inv.setItem(slot, event.getCursor());
                        event.setCursor(i);
                        player.updateInventory();
                    } else if (loot != null) {
                        holding.put(player.getName(), lootList.remove(slot));
                        event.setCursor(inv.getItem(slot));
                        refreshPage(player, inv, lootList);
                    }
                    break;
                }
                break;
            case RIGHT: //Go back a page
                up(player, event.getCursor());
                break;
            case SHIFT_LEFT: //Move Loot left
                if (loot != null) {
                    Loot temp = lootList.get(slot);
                    lootList.set(slot, lootList.get(slot - 1));
                    lootList.set(slot - 1, temp);
                    refreshPage(player, inv, lootList);
                }
                break;
            case SHIFT_RIGHT: //Move Loot right
                if (loot != null) {
                    Loot temp = lootList.get(slot);
                    lootList.set(slot, lootList.get(slot + 1));
                    lootList.set(slot + 1, temp);
                    refreshPage(player, inv, lootList);
                }
                break;
            case MIDDLE: //Add/Remove Loot
                if (loot != null) {
                    lootList.remove(slot);
                } else if (slot > SIZE) {
                    ItemStack item = event.getCurrentItem() == null ? new ItemStack(Material.AIR) : event.getCurrentItem().clone();
                    lootList.add(new Item(item, 0));
                } else {
                    break;
                }
                refreshPage(player, inv, lootList);
                break;
            }
            break;

        case MODIFY_PROBABILITY_AND_TOGGLE:
            if (loot != null) {
                switch (event.getClick()) {
                case LEFT: //+1%
                    loot.setProbability(loot.getProbability() + 1);
                    break;
                case DOUBLE_CLICK: //+9%
                    loot.setProbability(loot.getProbability() + 9);
                    break;
                case RIGHT: //-1%
                    loot.setProbability(loot.getProbability() - 1);
                    break;
                case SHIFT_LEFT: //Toggle autoEnchant/fromConsole
                    if (loot instanceof Item) {
                        ((Item) loot).autoEnchant = !((Item) loot).autoEnchant;
                    } else if (loot instanceof CommandLoot) {
                        ((CommandLoot) loot).fromConsole = !((CommandLoot) loot).fromConsole;
                    }
                    break;
                case SHIFT_RIGHT: //Toggle generateName/tempOP
                    if (loot instanceof Item) {
                        ((Item) loot).generateName = !((Item) loot).generateName;
                    } else if (loot instanceof CommandLoot) {
                        ((CommandLoot) loot).tempOP = !((CommandLoot) loot).tempOP;
                    }
                    break;
                case MIDDLE: //Toggle tieredName/breakAndRespawn/autoLoot/global
                    if (loot instanceof Item) {
                        ((Item) loot).tieredName = !((Item) loot).tieredName;
                    }
                    break;
                default:
                    return;
                }
                while (loot.getProbability() < 0) {
                    loot.setProbability(loot.getProbability() + 100);
                }
                while (loot.getProbability() > 100) {
                    loot.setProbability(loot.getProbability() - 100);
                }
                refreshPage(player, inv, lootList);
            } else if (inv.getTitle().endsWith("Loot Tables")) {
                ItemStack infoStack;
                ItemMeta info;
                List<String> details = new ArrayList();

                switch (slot) {
                case SIZE - 5:
                    phatLoot.breakAndRespawn = !phatLoot.breakAndRespawn;

                    //Show the break and respawn status
                    infoStack = new ItemStack(phatLoot.breakAndRespawn ? Material.MOB_SPAWNER : Material.CHEST);
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    info.setDisplayName("§4Break and Respawn: §6" + phatLoot.breakAndRespawn);
                    if (phatLoot.breakAndRespawn) {
                        details.add("§6This chest will break after it is looted");
                        details.add("§6and respawn once it may be looted again.");
                    } else {
                        details.add("§6This chest will always be present");
                        details.add("§6even after it is looted.");
                    }
                    info.setLore(details);
                    infoStack.setItemMeta(info);
                    break;

                case SIZE - 4:
                    phatLoot.autoLoot = !phatLoot.autoLoot;

                    //Show the autoloot status
                    infoStack = new ItemStack(phatLoot.autoLoot ? Material.REDSTONE_TORCH_ON : Material.REDSTONE_TORCH_OFF);
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    info.setDisplayName("§4AutoLoot: §6" + phatLoot.autoLoot);
                    infoStack.setItemMeta(info);
                    break;

                case SIZE - 3:
                    phatLoot.global = !phatLoot.global;

                    //Show the Reset Time
                    infoStack = new ItemStack(Material.WATCH);
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    info.setDisplayName("§2Reset Time");
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
                    break;

                default:
                    return;
                }

                inv.setItem(slot, infoStack);
            }
            break;

        case MODIFY_AMOUNT:
            int amount = 0;
            boolean both = false;
            switch (event.getClick()) {
            case LEFT: //+1 amount
                amount = 1;
                both = true;
                break;
            case DOUBLE_CLICK: //+8 amount
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
                if (loot instanceof Item) {
                    ((Item) loot).item.setAmount(1);
                    ((Item) loot).amountBonus = 0;
                } else if (loot instanceof LootCollection) {
                    ((LootCollection) loot).lowerNumberOfLoots = 1;
                    ((LootCollection) loot).upperNumberOfLoots = 1;
                } else {
                    break;
                }
                refreshPage(player, inv, lootList);
                return;
            default:
                return;
            }
            if (loot != null) {
                if (loot instanceof Item) {
                    if (both) {
                        ((Item) loot).item.setAmount(((Item) loot).item.getAmount() + amount);
                    } else {
                        ((Item) loot).amountBonus += amount;
                    }
                } else if (loot instanceof LootCollection) {
                    if (both) {
                        ((LootCollection) loot).lowerNumberOfLoots += amount;
                    }
                    ((LootCollection) loot).upperNumberOfLoots += amount;
                }
                refreshPage(player, inv, lootList);
            } else if (inv.getTitle().endsWith("Loot Tables")) {
                ItemStack infoStack;
                ItemMeta info;
                List<String> details = new ArrayList();

                switch (slot) {
                case SIZE - 3:
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

                    //Show the Reset Time
                    infoStack = new ItemStack(Material.WATCH);
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    info.setDisplayName("§2Reset Time");
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
                    break;

                case SIZE - 2:
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
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    String exp = String.valueOf(phatLoot.expLower);
                    if (phatLoot.expUpper != phatLoot.expLower) {
                        exp += '-' + String.valueOf(phatLoot.expUpper);
                    }
                    info.setDisplayName("§6" + exp + " exp");
                    infoStack.setItemMeta(info);
                    break;

                case SIZE - 1:
                    if (PhatLoots.econ == null) {
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
                    info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
                    String money = String.valueOf(phatLoot.moneyLower);
                    if (phatLoot.moneyUpper != phatLoot.moneyLower) {
                        money += '-' + String.valueOf(phatLoot.moneyUpper);
                    }
                    info.setDisplayName("§6" + money + ' ' + PhatLoots.econ.currencyNamePlural());
                    infoStack.setItemMeta(info);
                    break;

                default:
                    return;
                }

                inv.setItem(slot, infoStack);
            }
            break;
        }
    }

    private List<Loot> getLootList(PhatLoot phatLoot, Inventory inv) {
        LootCollection coll = null;
        String invName = inv.getName();
        if (invName.endsWith(" (Collection)")) {
            coll = phatLoot.findCollection(invName.substring(0, invName.length() - 13));
        }
        return coll == null ? phatLoot.lootList : coll.getLootList();
    }

    private void refreshPage(Player player, Inventory inv, List<Loot> lootList) {
        //Populate the inventory
        int index = 0;
        for (Loot loot : lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= TOOL_SLOT) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }
        while (index < TOOL_SLOT) {
            inv.clear(index);
            index++;
        }
        player.updateInventory();
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

    /**
     * Opens an Inventory GUI for the given PhatLoot to the given Player
     *
     * @param player The given Player
     * @param phatLoot The given PhatLoot
     */
    public static void viewPhatLoot(final Player player, final PhatLoot phatLoot, final ItemStack hand) {
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
        info.setDisplayName("§4AutoLoot: §6" + phatLoot.autoLoot);
        infoStack.setItemMeta(info);
        index--;
        inv.setItem(index, infoStack);

        //Show the break and respawn status
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

        //Set Tool
        inv.setItem(TOOL_SLOT, Tool.NAVIGATE_AND_MOVE.getItem());

        //Show the Add Collection Tool
        infoStack = new ItemStack(Material.ENDER_CHEST);
        info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2Add new collection...");
        infoStack.setItemMeta(info);
        inv.setItem(TOOL_SLOT + 1, infoStack);

        //Populate the inventory
        index = 0;
        for (Loot loot : phatLoot.lootList) {
            inv.setItem(index, loot.getInfoStack());
            index++;
            if (index >= TOOL_SLOT) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        final String playerName = player.getName();
        pageStacks.put(playerName, new Stack<Inventory>());
        switchingPages.put(playerName, true);
        Loot loot = holding.get(playerName);
        player.closeInventory();
        if (loot != null) {
            holding.put(playerName, loot);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                infoViewers.put(playerName, phatLoot);
                player.openInventory(inv).setCursor(hand);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    /**
     * Opens an Inventory GUI for the specified LootCollection to the given Player
     *
     * @param player The given Player
     * @param name The name of the LootCollection to open
     */
    public static void viewCollection(final Player player, String name, final ItemStack hand) {
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
        for (Loot loot : coll.getLootList()) {
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
        Loot loot = holding.get(playerName);
        player.closeInventory();
        if (loot != null) {
            holding.put(playerName, loot);
        }
        details.add("§6" + pageStacks.get(playerName).peek().getTitle());
        info.setLore(details);
        item.setItemMeta(info);
        inv.setItem(SIZE - 1, item);

        inv.setItem(TOOL_SLOT, Tool.NAVIGATE_AND_MOVE.getItem());

        //Show the Add Collection Tool
        item = new ItemStack(Material.ENDER_CHEST);
        info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Add new collection...");
        item.setItemMeta(info);
        inv.setItem(TOOL_SLOT + 1, item);

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv).setCursor(hand);
            }
        }.runTaskLater(PhatLoots.plugin, 2);

    }

    /**
     * Opens the top Inventory in the given Players Stack
     *
     * @param player The given Player
     */
    public static void up(final Player player, final ItemStack hand) {
        //Pop the inventory off of the Stack
        final String playerName = player.getName();
        final Stack<Inventory> stack = pageStacks.get(playerName);

        //Open the Inventory in 2 ticks to avoid Bukkit glitches
        switchingPages.put(playerName, false);
        Loot loot = holding.get(playerName);
        player.closeInventory();
        if (loot != null) {
            holding.put(playerName, loot);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(stack.pop()).setCursor(hand);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }
}

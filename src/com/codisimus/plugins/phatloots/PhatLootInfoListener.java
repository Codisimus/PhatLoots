package com.codisimus.plugins.phatloots;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootInfoListener implements Listener {
    static HashMap<String, Boolean> switchingPages = new HashMap<String, Boolean>();
    static HashMap<String, PhatLoot> infoViewers = new HashMap<String, PhatLoot>();
    static HashMap<String, Stack<Inventory>> infoStacks = new HashMap<String, Stack<Inventory>>();

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
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (human instanceof Player) {
            Player player = (Player) human;
            String playerName = player.getName();
            if (infoViewers.containsKey(playerName)) {
                if (switchingPages.containsKey(playerName)) {
                    if (switchingPages.get(playerName)) {
                        infoStacks.get(playerName).add(event.getInventory());
                    }
                    switchingPages.remove(playerName);
                } else {
                    infoViewers.remove(playerName);
                    infoStacks.get(playerName).empty();
                    infoStacks.remove(playerName);
                }
            }
        }
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

        final String playerName = player.getName();
        infoStacks.put(playerName, new Stack<Inventory>());
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
            if (index >= 52) {
                player.sendMessage("§4Not all items could fit within the inventory view.");
                break;
            }
        }

        ItemStack item = new ItemStack(Material.LADDER);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Back to top...");
        item.setItemMeta(info);
        inv.setItem(52, item);

        item = new ItemStack(Material.LADDER);
        info = Bukkit.getItemFactory().getItemMeta(item.getType());
        info.setDisplayName("§2Up to...");
        List<String> details = new ArrayList();

        final String playerName = player.getName();
        switchingPages.put(playerName, true);
        player.closeInventory();
        details.add("§6" + infoStacks.get(playerName).peek().getTitle());
        info.setLore(details);
        item.setItemMeta(info);
        inv.setItem(53, item);

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

        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTaskLater(PhatLoots.plugin, 2);
    }

    public static void up(final Player player) {
        final String playerName = player.getName();
        final Stack<Inventory> stack = infoStacks.get(playerName);
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

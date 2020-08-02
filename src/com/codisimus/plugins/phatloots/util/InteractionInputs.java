package com.codisimus.plugins.phatloots.util;

import com.codisimus.plugins.phatloots.PhatLoots;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for chat input
 *
 * @author Redned
 */
public class InteractionInputs {

    public static abstract class ChatInput {

        /**
         * Constructs a new ChatInput instance
         *
         * @param player the player to receive the chat input from
         */
        public ChatInput(Player player) {
            player.sendMessage(ChatColor.YELLOW + "Enter the input in chat...");
            Listener listener = new Listener() {

                @EventHandler
                public void onChat(AsyncPlayerChatEvent event) {
                    if (!player.getName().equalsIgnoreCase(event.getPlayer().getName()))
                        return;

                    event.setCancelled(true);
                    String message = ChatColor.stripColor(event.getMessage());

                    // Run task synchronously since chat is async
                    Bukkit.getScheduler().runTask(PhatLoots.plugin, () -> {
                        onChatInput(message);
                    });

                    HandlerList.unregisterAll(this);
                }
            };

            Bukkit.getPluginManager().registerEvents(listener, PhatLoots.plugin);
        }

        /**
         * Runs when the player enters text in chat
         *
         * @param input the text the player inputted
         */
        public abstract void onChatInput(String input);
    }

    public static abstract class InventoryInput {

        /**
         * Constructs a new InventoryInput instance
         *
         * @param player the player to receive the inventory input from
         */
        public InventoryInput(Player player) {
            player.sendMessage(ChatColor.YELLOW + "Select an item from your inventory...");
            Listener listener = new Listener() {

                @EventHandler
                public void onInteract(InventoryClickEvent event) {
                    if (!player.getName().equalsIgnoreCase(event.getWhoClicked().getName()))
                        return;

                    if (!player.getInventory().equals(event.getClickedInventory())) {
                        player.sendMessage(ChatColor.RED + "Interacted with inventory that was not own.. cancelling item selection.");
                        HandlerList.unregisterAll(this);
                        return;
                    }

                    onInventoryInteract(event.getCurrentItem());

                    event.setCancelled(true);
                    HandlerList.unregisterAll(this);
                }
            };

            Bukkit.getPluginManager().registerEvents(listener, PhatLoots.plugin);
        }

        /**
         * Runs when the player interacts with an item in
         * their inventory
         *
         * @param item the item the player interacted with
         */
        public abstract void onInventoryInteract(ItemStack item);
    }
}
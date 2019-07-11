package com.codisimus.plugins.phatloots.util;

import com.codisimus.plugins.phatloots.PhatLoots;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listens for chat input
 *
 * @author Redned
 */
public abstract class ChatInput {

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
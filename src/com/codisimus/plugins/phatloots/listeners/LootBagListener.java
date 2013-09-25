package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listens for interactions with Loot bags
 *
 * @author Cody
 */
public class LootBagListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Open the backpack when the player clicks while holding it
        switch (event.getAction()) {
        case RIGHT_CLICK_AIR:
        case RIGHT_CLICK_BLOCK:
            if (event.hasItem()) {
                ItemStack hand = event.getItem();
                if (hand.hasItemMeta()) {
                    ItemMeta meta = hand.getItemMeta();
                    if (meta.hasLore()) {
                        List<String> lore = meta.getLore();
                        for (String line : lore) {
                            if (line.startsWith(PhatLootsConfig.lootBagKey)) {
                                PhatLoot phatLoot = PhatLoots.getPhatLoot(line.substring(PhatLootsConfig.lootBagKey.length()));
                                phatLoot.rollForLoot(event.getPlayer());
                            }
                        }
                    }
                }
            }
        }
    }
}

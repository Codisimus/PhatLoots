package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Listens for interactions with Loot bags
 *
 * @author Codisimus
 */
public class LootBagListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        //Open the backpack when the player clicks while holding it
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        if (!event.hasItem())
            return;

        ItemStack hand = event.getItem();
        if (!hand.hasItemMeta())
            return;

        if (!hand.getItemMeta().hasLore())
            return;

        List<String> lore = hand.getItemMeta().getLore();
        for (String line : lore) {
            String finalLine = "";
            for (String str : PhatLootsConfig.lootBagKeys) {
                if (line.startsWith(str)) {
                    finalLine = line;
                    break;
                }
            }

            if (finalLine == null || finalLine.isEmpty())
                continue;

            PhatLoot phatLoot = PhatLoots.getPhatLoot(line.substring(finalLine.length()));
            if (phatLoot == null)
                continue;

            event.setCancelled(true);
            //Remove the bag from the player's inventory
            if (hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                PlayerInventory inv = event.getPlayer().getInventory();
                if (inv.getItemInMainHand().equals(hand)) {
                    inv.setItemInMainHand(new ItemStack(Material.AIR));
                } else if (inv.getItemInOffHand().equals(hand)) {
                    inv.setItemInOffHand(new ItemStack(Material.AIR));
                } else {
                    PhatLoots.logger.warning("Player attempted to use LootBag with an unexpected hand.");
                    return;
                }
            }

            //Give loot to the player
            phatLoot.rollForLoot(event.getPlayer());
        }
    }
}

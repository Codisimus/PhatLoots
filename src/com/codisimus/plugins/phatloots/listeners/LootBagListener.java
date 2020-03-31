package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

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
        if (hand.getItemMeta() == null)
            return;

        if (PhatLootsConfig.persistentDataContainerLinks) {
            if (hand.getItemMeta().getCustomTagContainer().hasCustomTag(PhatLoot.LINK_TAG, ItemTagType.STRING)) {
                PhatLoot phatLoot = PhatLoots.getPhatLoot(hand.getItemMeta().getCustomTagContainer().getCustomTag(PhatLoot.LINK_TAG, ItemTagType.STRING));
                if (phatLoot == null) {
                    return;
                }

                event.setCancelled(true);
                giveLootBag(event.getPlayer(), phatLoot, hand);
            }
        }

        if (!hand.getItemMeta().hasLore())
            return;

        List<String> lore = hand.getItemMeta().getLore();
        for (String line : lore) {
            String finalLine = "";
            for (String str : PhatLootsConfig.lootBagKeys) {
                if (line.startsWith(str)) {
                    finalLine = str;
                    break;
                }
            }

            if (finalLine.isEmpty())
                continue;

            PhatLoot phatLoot = PhatLoots.getPhatLoot(line.substring(finalLine.length()));
            if (phatLoot == null)
                continue;

            event.setCancelled(true);
            //Remove the bag from the player's inventory
            giveLootBag(event.getPlayer(), phatLoot, hand);
        }
    }

    private void giveLootBag(Player player, PhatLoot loot, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            PlayerInventory inv = player.getInventory();
            if (inv.getItemInMainHand().equals(item)) {
                inv.setItemInMainHand(new ItemStack(Material.AIR));
            } else if (inv.getItemInOffHand().equals(item)) {
                inv.setItemInOffHand(new ItemStack(Material.AIR));
            } else {
                PhatLoots.logger.warning("Player attempted to use LootBag with an unexpected hand.");
                return;
            }
        }

        //Give loot to the player
        loot.rollForLoot(player);
    }
}

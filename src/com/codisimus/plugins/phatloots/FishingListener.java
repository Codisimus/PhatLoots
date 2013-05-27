package com.codisimus.plugins.phatloots;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for Fishing events and handles what is caught
 *
 * @author Codisimus
 */
public class FishingListener implements Listener {
    @EventHandler (ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        PhatLoot phatLoot = PhatLoots.getPhatLoot("Fishing");
        ItemStack pole = event.getPlayer().getItemInHand();
        double lootingBonus = PhatLoot.lootingBonusPerLvl * pole.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
        if (event.getCaught() instanceof Item && phatLoot != null) {
            Item fish = (Item) event.getCaught();
            fish.setItemStack(phatLoot.lootAll(event.getPlayer(), lootingBonus).getFirst());
            int exp = 0;
            if (phatLoot.expUpper > 0) {
                exp = PhatLoots.random.nextInt(phatLoot.expUpper + 1 - phatLoot.expLower);
                exp += phatLoot.expLower;
            }
            event.setExpToDrop(exp);
        }
    }
}

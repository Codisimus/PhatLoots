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
        //Check if there is a PhatLoot for Fishing
        PhatLoot phatLoot = PhatLoots.getPhatLoot("Fishing");
        if (phatLoot == null) {
            return;
        }

        //Check if something has been caught
        if (event.getCaught() instanceof Item) {
            //Get the looting bonus of the fishing pole
            ItemStack pole = event.getPlayer().getItemInHand();
            double lootingBonus = PhatLoot.lootingBonusPerLvl * pole.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

            //Change the 'fish' to which ever Item has been looted
            Item fish = (Item) event.getCaught();
            fish.setItemStack(phatLoot.lootAll(event.getPlayer(), lootingBonus).getFirst());

            //Roll for experience to be gained by the fisher
            event.setExpToDrop(phatLoot.expUpper > 0
                               ? PhatLoots.rollForInt(phatLoot.expLower, phatLoot.expUpper)
                               : 0);
        }
    }
}

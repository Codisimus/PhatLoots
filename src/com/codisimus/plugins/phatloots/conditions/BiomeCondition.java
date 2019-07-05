package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.AnvilGUI;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.gui.InventoryConditionListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Loot condition that checks for biome.
 *
 * @author Redned
 */
@SerializableAs("BiomeCondition")
public class BiomeCondition extends LootCondition {

    private String biome;
    private String option;

    public BiomeCondition(String name) {
        super(name);

        biome = Biome.PLAINS.name();
        option = "inside";
    }

    public BiomeCondition(Map<String, Object> map) {
        super(map);

        biome = (String) map.get("Biome");
        option = (String) map.get("Option");
    }

    @Override
    public boolean checkCondition(Player player) {
        Biome biome;
        try {
            biome = Biome.valueOf(this.biome.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        Location loc = player.getLocation();
        switch (option) {
            case "inside":
                if (player.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ()) == biome)
                    return true;
                break;
            case "outside":
                if (player.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ()) != biome)
                    return true;
        }


        return false;
    }


    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (option) {
                case "inside":
                    option = "outside";
                    break;
                case "outside":
                    option = "inside";
                    break;
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Biome Condition");
        item.setType(Material.OAK_SAPLING);
        List<String> lore = meta.getLore();
        lore.add("§6 Biome: §e" + biome);
        lore.add("§6 Biome Option: §e" + option);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Cycle Through the Options");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Set Biome");
        meta.setLore(lore);
        item.setItemMeta(meta);

        if (click == ClickType.SHIFT_LEFT) {
            AnvilGUI gui = new AnvilGUI(player, new AnvilGUI.AnvilClickEventHandler() {

                @Override
                public void onAnvilClick(AnvilGUI.AnvilClickEvent event){
                    event.setWillClose(false);
                    event.setWillDestroy(true);

                    if (event.getSlot() == AnvilGUI.AnvilSlot.OUTPUT) {
                        biome = event.getName();
                        InventoryConditionListener.viewConditionMenu(player, phatLoot);
                    }
                }
            });

            ItemStack tag = new ItemStack(Material.NAME_TAG);
            ItemMeta tagMeta = tag.getItemMeta();
            tagMeta.setDisplayName("Enter Biome Name...");
            tag.setItemMeta(tagMeta);
            gui.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, tag);

            gui.open();
        }

        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Biome", biome);
        map.put("Option", option);
        return map;
    }
}
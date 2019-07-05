package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Loot condition that checks for weather.
 *
 * @author Redned
 */
@SerializableAs("WeatherCondition")
public class WeatherCondition extends LootCondition {

    private String weather;

    public WeatherCondition(String name) {
        super(name);

        weather = "sunny";
    }

    public WeatherCondition(Map<String, Object> map) {
        super(map);

        weather = (String) map.get("Weather");
    }

    @Override
    public boolean checkCondition(Player player) {
        switch (weather) {
            case "sunny":
                if (!player.getWorld().hasStorm() && !player.getWorld().isThundering())
                    return true;
                break;
            case "raining":
                if (player.getWorld().hasStorm() && !player.getWorld().isThundering())
                    return true;
                break;
            case "thundering":
                if (player.getWorld().isThundering())
                    return true;
                break;
        }

        return false;
    }

    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (weather) {
                case "sunny":
                    weather = "raining";
                    break;
                case "raining":
                    weather = "thundering";
                    break;
                case "thundering":
                    weather = "sunny";
                    break;
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eWeather Condition");
        item.setType(Material.LAPIS_LAZULI);
        List<String> lore = meta.getLore();
        lore.add("§6 Required Weather: §e" + weather);
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Change the Weather");
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Weather", weather);
        return map;
    }
}
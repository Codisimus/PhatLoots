package com.codisimus.plugins.phatloots.conditions;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.gui.InventoryConditionListener;
import com.codisimus.plugins.phatloots.hook.placeholder.PlaceholderManager;
import com.codisimus.plugins.phatloots.util.InteractionInputs;

import org.bukkit.ChatColor;
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
 * Loot condition that checks for placeholder data.
 *
 * @author Redned
 */
@SerializableAs("PlaceholderDataCondition")
public class PlaceholderDataCondition extends LootCondition {

    private String placeholder;
    private String expectedResult;
    private String inputType;
    private String option;

    private PlaceholderManager placeholderManager;

    public PlaceholderDataCondition(String name) {
        super(name);

        this.placeholder = "none";
        this.expectedResult = "none";
        this.inputType = "string";
        this.option = "equals";

        this.placeholderManager = PhatLoots.plugin.getPluginHookManager().getPlaceholderManager();
    }

    public PlaceholderDataCondition(Map<String, Object> map) {
        super(map);

        this.placeholder = (String) map.get("Placeholder");
        this.expectedResult = (String) map.get("ExpectedResult");
        this.inputType = (String) map.get("InputType");
        this.option = (String) map.get("Option");

        this.placeholderManager = PhatLoots.plugin.getPluginHookManager().getPlaceholderManager();
    }

    @Override
    public boolean checkCondition(Player player) {
        if (!placeholderManager.isPlaceholderPluginPresent()) {
            return false;
        }

        if (placeholder.equalsIgnoreCase("none") || expectedResult.equalsIgnoreCase("none")) {
            return false;
        }
        String replaced = placeholderManager.getReplacementString(player, placeholder);
        switch (inputType) {
            case "string":
                if (option.equalsIgnoreCase("equals")) {
                    if (expectedResult.equalsIgnoreCase(replaced)) {
                        return true;
                    }
                } else if (option.equalsIgnoreCase("notequals")){
                    if (!expectedResult.equalsIgnoreCase(replaced)) {
                        return true;
                    }
                }
                break;
            case "number":
                if (!isDouble(replaced) || !isDouble(expectedResult)) {
                    return false;
                }
                double doubleReplaced = Double.parseDouble(replaced);
                double doubleExpected = Double.parseDouble(expectedResult);

                if (option.equalsIgnoreCase("equals")) {
                    if (doubleReplaced == doubleExpected) {
                        return true;
                    }
                } else if (option.equalsIgnoreCase("notequals")){
                    if (doubleReplaced != doubleExpected) {
                        return true;
                    }
                } else if (option.equalsIgnoreCase("greaterthan")) {
                    if (doubleReplaced > doubleExpected) {
                        return true;
                    }
                } else if (option.equalsIgnoreCase("lessthan")) {
                    if (doubleReplaced < doubleExpected) {
                        return true;
                    }
                }
                break;
            case "boolean":
               if (!isBoolean(replaced) || !isBoolean(expectedResult)) {
                   return false;
               }
               boolean booleanReplaced = Boolean.parseBoolean(replaced);
               boolean booleanExpected = Boolean.parseBoolean(expectedResult);
                if (option.equalsIgnoreCase("equals")) {
                    if (booleanReplaced == booleanExpected) {
                        return true;
                    }
                } else if (option.equalsIgnoreCase("notequals")){
                    if (booleanReplaced != booleanExpected) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }


    @Override
    public ItemStack handleClick(Player player, PhatLoot phatLoot, Inventory inventory, ClickType click) {
        if (click == ClickType.LEFT) {
            switch (option) {
                case "equals":
                    option = "notequals";
                    break;
                case "notequals":
                    option = "greaterthan";
                    break;
                case "greaterthan":
                    option = "lessthan";
                    break;
                case "lessthan":
                    option = "equals";
                    break;
            }
        }

        if (click == ClickType.SHIFT_LEFT) {
            player.closeInventory();
            new InteractionInputs.ChatInput(player) {

                @Override
                public void onChatInput(String input) {
                    placeholder = input;
                    InventoryConditionListener.viewConditionMenu(player, phatLoot);
                }
            };
        }

        if (click == ClickType.SHIFT_RIGHT) {
            player.closeInventory();
            new InteractionInputs.ChatInput(player) {

                @Override
                public void onChatInput(String input) {
                    expectedResult = input;
                    InventoryConditionListener.viewConditionMenu(player, phatLoot);
                }
            };
        }

        if (click == ClickType.MIDDLE) {
            switch (inputType) {
                case "string":
                    inputType = "number";
                    break;
                case "number":
                    inputType = "boolean";
                    break;
                case "boolean":
                    inputType = "string";
                    break;
            }
        }

        ItemStack item = super.handleClick(player, phatLoot, inventory, click);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Placeholder Data Condition");
        item.setType(Material.DEBUG_STICK);
        List<String> lore = meta.getLore();
        lore.add("§6 Placeholder: §e" + placeholder);
        lore.add("§6 Expected Result: §e" + expectedResult);
        lore.add("§6 Input Type: §e" + inputType);
        lore.add("§6 Compare Option: §e" + option);
        lore.add("§6 Placeholder Plugins:");
        if (placeholderManager.getPlaceholderPlugins().isEmpty()) {
            lore.add(ChatColor.RED + " - None Present ");
        } else {
            for (String plugin : placeholderManager.getPlaceholderPlugins()) {
                lore.add(ChatColor.YELLOW + " - " + plugin);
            }
        }
        lore.add("§a");
        lore.add("§aActions:");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Toggle the Condition");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Change Compare Option");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Change Placeholder");
        lore.add("§4SHIFT + RIGHT CLICK:");
        lore.add("§6 Change Expected Result");
        lore.add("§4SCROLL CLICK:");
        lore.add("§6 Change Input Type");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("Placeholder", placeholder);
        map.put("ExpectedResult", expectedResult);
        map.put("InputType", inputType);
        map.put("Option", option);
        return map;
    }
}

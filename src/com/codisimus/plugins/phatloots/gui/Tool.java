package com.codisimus.plugins.phatloots.gui;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Tools used for modifying Loot in the GUI
 *
 * @author Cody
 */
public enum Tool {
    NAVIGATE_AND_MOVE(0, Material.LEASH),
    MODIFY_PROBABILITY_AND_TOGGLE(1, Material.NAME_TAG),
    MODIFY_AMOUNT(2, Material.GOLD_NUGGET);

    private int id;
    private Material mat;

    /**
     * Constructs a new Tool
     *
     * @param id The id of the Tool
     * @param mat The Material that represents the Tool
     */
    private Tool(int id, Material mat) {
        this.id = id;
        this.mat = mat;
    }

    /**
     * Returns the id of the Tool
     *
     * @return The id of the Tool
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the ItemStack which displays the Tool information
     *
     * @return The Tool's ItemStack
     */
    public ItemStack getItem() {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(mat);
        List<String> lore = new ArrayList<String>();

        switch (this) {
        case NAVIGATE_AND_MOVE:
            meta.setDisplayName("§2Navigate/Move (Click to change Tool)");
            lore.add("§4LEFT CLICK:");
            lore.add("§6 Enter a Collection");
            lore.add("§4RIGHT CLICK:");
            lore.add("§6 Leave a Collection");
            lore.add("§4SHIFT + LEFT CLICK:");
            lore.add("§6 Shift a Loot to the Left");
            lore.add("§4SHIFT + RIGHT CLICK:");
            lore.add("§6 Shift a Loot to the Right");
            lore.add("§4SCROLL CLICK:");
            lore.add("§6 Remove a Loot/Add an Item (from inventory)");
            break;
        case MODIFY_PROBABILITY_AND_TOGGLE:
            meta.setDisplayName("§2Modify Probability/Toggle (Click to change Tool)");
            lore.add("§4LEFT CLICK:");
            lore.add("§6 +1 Probability");
            lore.add("§4DOUBLE LEFT CLICK:");
            lore.add("§6 +10 Probability");
            lore.add("§4RIGHT CLICK:");
            lore.add("§6 -1 Probability");
            lore.add("§4SHIFT + LEFT CLICK:");
            lore.add("§6 Toggle AutoEnchant/FromConsole");
            lore.add("§4SHIFT + RIGHT CLICK:");
            lore.add("§6 Toggle GenerateName/TempOP");
            lore.add("§4SCROLL CLICK:");
            lore.add("§6 Toggle TieredName and Loot table settings");
            break;
        case MODIFY_AMOUNT:
            meta.setDisplayName("§2Modify Amount (Click to change Tool)");
            lore.add("§4LEFT CLICK:");
            lore.add("§6 +1 Amount");
            lore.add("§4DOUBLE LEFT CLICK:");
            lore.add("§6 +10 Amount");
            lore.add("§4RIGHT CLICK:");
            lore.add("§6 -1 Amount");
            lore.add("§4SHIFT + LEFT CLICK:");
            lore.add("§6 +1 Amount (Upper Range)");
            lore.add("§4SHIFT + RIGHT CLICK:");
            lore.add("§6 -1 Amount (Upper Range)");
            lore.add("§4SCROLL CLICK:");
            lore.add("§6 Set Amount to 1 and Clear time/exp/money");
            break;
        default:
            break;
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns the previous Tool based on it's id
     *
     * @return The previous Tool based on it's id
     */
    public Tool prevTool() {
        int toolID = id - 1;
        if (toolID < 0) {
            toolID = Tool.values().length - 1;
        }
        return getToolByID(toolID);
    }

    /**
     * Returns the next Tool based on it's id
     *
     * @return The next Tool based on it's id
     */
    public Tool nextTool() {
        int toolID = id + 1;
        if (toolID >= Tool.values().length) {
            toolID = 0;
        }
        return getToolByID(toolID);
    }

    /**
     * Returns the Tool with the given id
     *
     * @param id The given Tool id
     * @return The Tool with the given id or null if there is no Tool with that id
     */
    public static Tool getToolByID(int id) {
        for (Tool tool : Tool.values()) {
            if (tool.id == id) {
                return tool;
            }
        }
        return null;
    }

    /**
     * Returns the Tool which is represented by the given ItemStack
     *
     * @param item The given ItemStack
     * @return The Tool that shares the same Material as the ItemStack
     */
    public static Tool getTool(ItemStack item) {
        Material mat = item.getType();
        for (Tool tool : Tool.values()) {
            if (tool.mat == mat) {
                return tool;
            }
        }
        return null;
    }
}

package com.codisimus.plugins.phatloots;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * A PhatLootChest is a Block location and a Map of Users with times attached to them
 *
 * @author Codisimus
 */
public class PhatLootChest {
    private String world;
    private int x, y, z;
    public boolean isDispenser;

    /**
     * Constructs a new PhatLootChest with the given Block
     *
     * @param block The given Block
     */
    public PhatLootChest(Block block) {
        world = block.getWorld().getName();
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        isDispenser = block.getTypeId() == 23;
    }

    /**
     * Constructs a new PhatLootChest with the given Block Location data
     *
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     */
    public PhatLootChest(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        Block block = PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
        isDispenser = block.getTypeId() == 23;
    }

    public PhatLootChest(String[] data) {
        world = data[0];
        x = Integer.parseInt(data[1]);
        y = Integer.parseInt(data[2]);
        z = Integer.parseInt(data[3]);
        Block block = PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
        isDispenser = block.getTypeId() == 23;
    }

    /**
     * Returns the Block that this Chest Represents
     *
     * @return The Block that this Chest Represents
     */
    public Block getBlock() {
        return PhatLoots.server.getWorld(world).getBlockAt(x, y, z);
    }

    /**
     * Returns true if the given Block is this PhatLootChest
     *
     * @param block The given Block
     * @return True if the given Block is the same Dispenser or part of the double Chest
     */
    public boolean isBlock(Block block) {
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();

            //We only link the left side of a DoubleChest
            if (inventory instanceof DoubleChestInventory) {
                chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                block = chest.getBlock();
            }
        }

        //Return false if Blocks are not in the same x-axis
        if (x != block.getX()) {
            return false;
        }

        //Return false if Blocks are not in the same y-axis
        if (y != block.getY()) {
            return false;
        }

        //Return false if Blocks are not in the same z-axis
        if (z != block.getZ()) {
            return false;
        }

        //Return false if Blocks are not in the same World
        return world.equals(block.getWorld().getName());
    }

    public boolean addLoots(List<ItemStack> itemList, Player player, Inventory inventory, boolean autoLoot) {
        boolean itemsInChest = false;
        for (ItemStack item: itemList) {
            if (addLoot(item, player, inventory, autoLoot)) {
                itemsInChest = true;
            }
        }
        return itemsInChest;
    }

    public boolean addLoot(ItemStack item, Player player, Inventory inventory, boolean autoLoot) {
        //Make sure loots do not exceed the stack size
        if (item.getAmount() > item.getMaxStackSize()) {
            int id = item.getTypeId();
            short durability = item.getDurability();

            addLoot(new ItemStack(id, item.getMaxStackSize(), durability), player, inventory, autoLoot);
            addLoot(new ItemStack(id, item.getAmount() - item.getMaxStackSize(), durability), player, inventory, autoLoot);
        }

        PlayerInventory sack = player.getInventory();

        if (isDispenser) {
            //Add the item to the Dispenser inventory
            Dispenser dispenser = (Dispenser) getBlock().getState();
            inventory.addItem(item);

            //Dispense until the Dispenser is empty
            while (inventory.firstEmpty() > 0) {
                dispenser.dispense();
            }

            return false;
        } else if (autoLoot && sack.firstEmpty() != -1) {
            //Add the Loot to the Player's Inventory
            if (PhatLootsConfig.autoLoot != null) {
                String msg = PhatLootsConfig.autoLoot.replace("<item>", PhatLoot.getItemName(item));
                int amount = item.getAmount();
                msg = amount > 1
                      ? msg.replace("<amount>", String.valueOf(item.getAmount()))
                      : msg.replace("x<amount>", "").replace("<amount>", "");
                player.sendMessage(msg);
            }
            sack.addItem(item);
            return false;
        } else {
            //Add the Loot to the Chest's Inventory
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(item);
            } else {
                overFlow(item, player);
            }
            return true;
        }
    }

    /**
     * Drops the given item outside the PhatLootChest
     *
     * @param item The ItemStack that will be dropped
     * @param player The Player (if any) that will be informed of the drop
     */
    public void overFlow(ItemStack item, Player player) {
        Block block = getBlock();
        block.getWorld().dropItemNaturally(block.getLocation(), item);
        if (player != null && PhatLootsConfig.overflow != null) {
            String msg = PhatLootsConfig.overflow.replace("<item>", PhatLoot.getItemName(item));
            int amount = item.getAmount();
            msg = amount > 1
                  ? msg.replace("<amount>", String.valueOf(item.getAmount()))
                  : msg.replace("x<amount>", "").replace("<amount>", "");
            player.sendMessage(msg);
        }
    }

    /**
     * Returns the String representation of this PhatLootChest
     * The format of the returned String is world'x'y'z
     *
     * @return The String representation of this Chest
     */
    @Override
    public String toString() {
        return world + "'" + x + "'" + y + "'" + z;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PhatLootChest) {
            PhatLootChest chest = (PhatLootChest) object;
            return chest.x == x
                   && chest.y == y
                   && chest.z == z
                   && chest.world.equals(world);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (world != null ? world.hashCode() : 0);
        hash = 47 * hash + x;
        hash = 47 * hash + y;
        hash = 47 * hash + z;
        return hash;
    }
}

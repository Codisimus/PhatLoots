package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A PhatLootChest is a Block location and a Map of Users with times attached to them
 *
 * @author Codisimus
 */
public class PhatLootChest {
    private static HashMap<String, PhatLootChest> chests = new HashMap<String, PhatLootChest>();
    static boolean soundOnAutoLoot;
    static boolean soundOnBreak;
    private String world;
    private int x, y, z;
    public boolean isDispenser;
    private BlockState state;

    /**
     * Constructs a new PhatLootChest with the given Block
     *
     * @param block The given Block
     */
    private PhatLootChest(Block block) {
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
    private PhatLootChest(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        World w = Bukkit.getWorld(world);
        if (w == null) { //The world is not currently loaded
            PhatLoots.logger.warning("The world '" + world + "' is not currently loaded, all linked chests in this world are being unlinked.");
            PhatLoots.logger.warning("THIS CHEST UNLINKING IS PERMANANT IF YOU LINK/UNLINK ANY OTHER CHESTS IN THIS PHATLOOT!");
        } else {
            Block block = w.getBlockAt(x, y, z);
            isDispenser = block.getTypeId() == 23;
        }
    }

//    /**
//     * Constructs a new PhatLootChest with the given Block Location data
//     *
//     * @param data The data in the form [world, x, y, z]
//     */
//    private PhatLootChest(String[] data) {
//        world = data[0];
//        x = Integer.parseInt(data[1]);
//        y = Integer.parseInt(data[2]);
//        z = Integer.parseInt(data[3]);
//        World w = Bukkit.getWorld(world);
//        if (w == null) { //The world is not currently loaded
//            PhatLoots.logger.warning("The world '" + world + "' is not currently loaded, all linked chests in this world are being unlinked.");
//            PhatLoots.logger.warning("THIS CHEST UNLINKING IS PERMANANT IF YOU LINK/UNLINK ANY OTHER CHESTS IN THIS PHATLOOT!");
//        } else {
//            Block block = w.getBlockAt(x, y, z);
//            isDispenser = block.getTypeId() == 23;
//            chests.put(world + "'" + x + "'" + y + "'" + z, this);
//        }
//    }

    /**
     * Constructs a new PhatLootChest with the given Block
     *
     * @param block The given Block
     */
    public static PhatLootChest getChest(Block block) {
        String key = block.getWorld().getName() + "'" + block.getX() + "'" + block.getY() + "'" + block.getZ();
        if (chests.containsKey(key)) {
            return chests.get(key);
        } else {
            PhatLootChest chest = new PhatLootChest(block);
            chests.put(chest.world + "'" + chest.x + "'" + chest.y + "'" + chest.z, chest);
            return chest;
        }
    }

    /**
     * Constructs a new PhatLootChest with the given Block Location data
     *
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     */
    public static PhatLootChest getChest(String world, int x, int y, int z) {
        String key = world + "'" + x + "'" + y + "'" + z;
        if (chests.containsKey(key)) {
            return chests.get(key);
        } else {
            PhatLootChest chest = new PhatLootChest(world, x, y, z);
            chests.put(world + "'" + x + "'" + y + "'" + z, chest);
            return chest;
        }
    }

    /**
     * Constructs a new PhatLootChest with the given Block Location data
     *
     * @param data The data in the form [world, x, y, z]
     */
    public static PhatLootChest getChest(String[] data) {
        String key = data[0] + "'" + data[1] + "'" + data[2] + "'" + data[3];
        if (chests.containsKey(key)) {
            return chests.get(key);
        } else {
            PhatLootChest chest = new PhatLootChest(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
            chests.put(chest.world + "'" + chest.x + "'" + chest.y + "'" + chest.z, chest);
            return chest;
        }
    }

    /**
     * Constructs a new PhatLootChest with the given Block
     *
     * @param block The given Block
     */
    public static PhatLootChest getTempChest(Block block) {
        String key = block.getWorld().getName() + "'" + block.getX() + "'" + block.getY() + "'" + block.getZ();
        if (chests.containsKey(key)) {
            return chests.get(key);
        } else {
            return new PhatLootChest(block);
        }
    }

    /**
     * Returns true if the given Block is linked to a PhatLoot
     *
     * @param block the given Block
     * @return true if the given Block is linked to a PhatLoot
     */
    public static boolean isPhatLootChest(Block block) {
        return chests.containsKey(block.getWorld().getName() + "'" + block.getX() + "'" + block.getY() + "'" + block.getZ());
    }

    /**
     * Returns the Block that this Chest Represents
     *
     * @return The Block that this Chest Represents
     */
    public Block getBlock() {
        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

    /**
     * Breaks the PhatLootChest and schedules it to respawn in the given amount of time
     *
     * @param time How long (in milliseconds) until the chest should respawn
     */
    public void breakChest(long time) {
        //Save the BlockState
        Block block = getBlock();
        state = block.getState();

        //Set the Block to AIR
        block.setTypeId(0);

        //Schedule the chest to respawn
        new BukkitRunnable() {
            @Override
            public void run() {
                respawn();
            }
        }.runTaskLater(PhatLoots.plugin, time / 50);

        if (soundOnBreak) {
            block.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
        }
    }

    /**
     * Respawns the chest as it was before is was broken
     */
    public void respawn() {
        if (state != null) {
            state.update(true);
            state = null;
        }
    }

    /**
     * Returns true if the given Block is this PhatLootChest
     *
     * @param block The given Block
     * @return True if the given Block is the same Dispenser or part of the double Chest
     */
    public boolean matchesBlock(Block block) {
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();

            //We only link the left side of a DoubleChest
            if (inventory instanceof DoubleChestInventory) {
                chest = (Chest) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
                block = chest.getBlock();
            }
        }

        //Return false if any of the coordinates don't match
        if (x != block.getX() || y != block.getY() || z != block.getZ()) {
            return false;
        }

        //Return false if Blocks are not in the same World
        return world.equals(block.getWorld().getName());
    }

    /**
     * Adds the list of ItemStacks to the given Inventory
     *
     * @param itemList The list of ItemStacks to add
     * @param player The Player looting the Chest
     * @param inventory The Inventory to add the items to
     * @param autoLoot True if the items should go straight to the Player's inventory
     * @return true if autoLoot is true and there are items in the inventory at the end
     */
    public boolean addLoots(List<ItemStack> itemList, Player player, Inventory inventory, boolean autoLoot) {
        boolean itemsInChest = false;
        for (ItemStack item: itemList) {
            if (addLoot(item, player, inventory, autoLoot)) {
                itemsInChest = true;
            }
        }
        return itemsInChest;
    }

    /**
     * Adds the ItemStack to the given Inventory
     *
     * @param item The ItemStack to add
     * @param player The Player looting the Chest
     * @param inventory The Inventory to add the item to
     * @param autoLoot True if the item should go straight to the Player's inventory
     * @return true if autoLoot is true and the item was added to the inventory
     */
    public boolean addLoot(ItemStack item, Player player, Inventory inventory, boolean autoLoot) {
        //Make sure loots do not exceed the stack size
        if (item.getAmount() > item.getMaxStackSize()) {
            int id = item.getTypeId();
            short durability = item.getDurability();
            int amount = item.getAmount();
            int maxStackSize = item.getMaxStackSize();
            while (amount > maxStackSize) {
                addLoot(new ItemStack(id, maxStackSize, durability), player, inventory, autoLoot);
                amount -= maxStackSize;
            }
        }

        //Get the Player's inventory in case of auto looting
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
                      : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(item.getAmount()));
                player.sendMessage(msg);
            }
            sack.addItem(item);
            if (soundOnAutoLoot) {
                player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 0.2F);
            }
            return false;
        } else {
            //Add the Loot to the Inventory
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(item);
            } else { //The item will not fit in the inventory
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
                  : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(item.getAmount()));
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
}

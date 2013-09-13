package com.codisimus.plugins.phatloots;

import com.codisimus.plugins.phatloots.events.ChestBreakEvent;
import com.codisimus.plugins.phatloots.events.ChestRespawnEvent;
import com.codisimus.plugins.phatloots.events.ChestRespawnEvent.RespawnReason;
import java.util.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * A PhatLootChest is a Block location and a Map of Users with times attached to them
 *
 * @author Cody
 */
public class PhatLootChest {
    private static EnumSet<Material> untriggeredRedstone = EnumSet.of(
        Material.REDSTONE_WIRE, Material.REDSTONE_COMPARATOR_OFF,
        Material.REDSTONE_LAMP_OFF, Material.REDSTONE_TORCH_OFF,
        Material.DIODE_BLOCK_OFF, Material.DISPENSER, Material.DROPPER,
        Material.NOTE_BLOCK, Material.PISTON_BASE, Material.TNT
    );
    private static EnumSet<Material> triggeredRedstone = EnumSet.of(
        Material.REDSTONE_WIRE, Material.REDSTONE_COMPARATOR_ON,
        Material.REDSTONE_LAMP_ON, Material.REDSTONE_TORCH_ON,
        Material.DIODE_BLOCK_ON, Material.PISTON_BASE
    );
    private static HashMap<String, PhatLootChest> chests = new HashMap<String, PhatLootChest>(); //Chest Location -> PhatLootChest
    static HashSet<PhatLootChest> chestsToRespawn = new HashSet<PhatLootChest>();
    public static HashMap<OfflinePlayer, PhatLootChest> openPhatLootChests = new HashMap<OfflinePlayer, PhatLootChest>(); //Player -> Open PhatLootChest
    static boolean useBreakAndRepawn;
    static boolean soundOnBreak;
    static boolean shuffleLoot;
    static String chestName;
    private String world;
    private int x, y, z;
    private boolean isDispenser;
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
        switch (block.getType()) {
        case DISPENSER:
        case DROPPER:
            isDispenser = true;
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
            switch (block.getType()) {
            case DISPENSER:
            case DROPPER:
                isDispenser = true;
            }
        }
    }

    /**
     * Returns the PhatLootChest of the given Block
     *
     * @param block The given Block
     * @return The found or created PhatLootChest
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
     * Returns the PhatLootChest with the given Block Location data
     *
     * @param world The name of the World
     * @param x The x-coordinate of the Block
     * @param y The y-coordinate of the Block
     * @param z The z-coordinate of the Block
     * @return The found or created PhatLootChest
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
     * returns the PhatLootChest with the given Block Location data
     *
     * @param data The data in the form [world, x, y, z]
     * @return The found or created PhatLootChest
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
     * Returns a collection of all linked chests
     *
     * @return A collection of all PhatLootChests
     */
    public static Collection<PhatLootChest> getChests() {
        return chests.values();
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
     * Returns true if the PhatLootChest is a Dispenser or Dropper
     *
     * @return true if the PhatLootChest is a Dispenser or Dropper
     */
    public boolean isDispenser() {
        return isDispenser;
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
     * Moves the chest to the given Block location
     *
     * @param target The given Block
     */
    public void moveTo(Block target) {
        //Remove the old Block
        if (state != null) {
            state.update(true);
        }
        Block block = getBlock();
        block.setTypeId(0);

        //Set the new Block
        x = target.getX();
        y = target.getY();
        z = target.getZ();
        //Only 'spawn' the new chest if it is not triggered to respawn
        if (state == null) {
            target.setType(block.getType());
            target.setData(block.getData());
        } else {
            state = target.getState();
        }
    }

    /**
     * Breaks the PhatLootChest and schedules it to respawn in the given amount of time
     *
     * @param time How long (in ticks) until the chest should respawn
     */
    public void breakChest(Player lastLooter, long time) {
        //Call the event to be modified
        ChestBreakEvent event = new ChestBreakEvent(lastLooter, this, time);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        chestsToRespawn.add(this);

        //Save the BlockState
        Block block = getBlock();
        state = block.getState();

        //Don't break the chest if it will immediately come back
        if (event.getRespawnTime() == 0) {
            return;
        }

        //Set the Block to AIR
        block.setTypeId(0);

        //Schedule the chest to respawn
        if (event.getRespawnTime() > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    respawn(RespawnReason.INITIAL);
                }
            }.runTaskLater(PhatLoots.plugin, event.getRespawnTime());
        }

        if (soundOnBreak) {
            block.getWorld().playSound(block.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
        }
    }

    /**
     * Respawns the chest as it was before is was broken
     */
    public void respawn(RespawnReason reason) {
        if (state != null) {
            //Call the event to be modified
            ChestRespawnEvent event = new ChestRespawnEvent(this, 0, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            if (event.getRespawnTime() > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        respawn(RespawnReason.DELAYED);
                    }
                }.runTaskLater(PhatLoots.plugin, event.getRespawnTime());
            } else {
                state.update(true);
                state = null;
                chestsToRespawn.remove(this);
            }
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

    public Inventory getInventory(String user, String name) {
        return getInventory(user, name, this);
    }

    public static Inventory getInventory(String user, String name, PhatLootChest chest) {
        if (chest != null && chest.isDispenser) {
            BlockState state = chest.getBlock().getState();
            switch (state.getType()) {
            case DISPENSER: return ((Dispenser) state).getInventory();
            case DROPPER: return ((Dropper) state).getInventory();
            default: chest.isDispenser = false;
            }
        }

        //Create the custom key using the user and Block location
        String key = user;
        if (chest != null) {
            key += '@' + chest.toString();
        }

        //Grab the custom Inventory belonging to the Player
        Inventory inventory;
        ForgettableInventory fInventory = ForgettableInventory.get(key);
        if (fInventory != null) {
            inventory = fInventory.getInventory();
        } else {
            name = chestName.replace("<name>", ChatColor.translateAlternateColorCodes('&', name.replace('_', ' ')));

            if (chest != null) {
                Block block = chest.getBlock();
                switch (block.getType()) {
                case TRAPPED_CHEST:
                case CHEST:
                    inventory = ((Chest) block.getState()).getInventory();
                    break;
                default:
                    inventory = null;
                    break;
                }
            } else {
                inventory = null;
            }

            //Create a new Inventory for the user
            inventory = Bukkit.createInventory(null, inventory == null ? 27 : inventory.getSize(), name);
            fInventory = new ForgettableInventory(key, inventory);
        }

        //Forget the Inventory in the scheduled time
        fInventory.schedule();
        return inventory;
    }

    /**
     * Adds the ItemStacks to the given Inventory
     *
     * @param itemList The Collection of ItemStacks to add
     * @param player The Player looting the Chest
     * @param inventory The Inventory to add the items to
     */
    public void addItems(Collection<ItemStack> itemList, Player player, Inventory inventory) {
        for (ItemStack item : itemList) {
            addItem(item, player, inventory);
        }
        if (shuffleLoot) {
            List<ItemStack> contents = Arrays.asList(inventory.getContents());
            Collections.shuffle(contents);
            inventory.setContents(contents.toArray(new ItemStack[contents.size()]));
        }
    }

    /**
     * Adds the ItemStack to the given Inventory
     *
     * @param item The ItemStack to add
     * @param player The Player looting the Chest
     * @param inventory The Inventory to add the item to
     */
    public void addItem(ItemStack item, Player player, Inventory inventory) {
        /* Bukkit should be able to handle this */
//        //Make sure loots do not exceed the stack size
//        if (item.getAmount() > item.getMaxStackSize()) {
//            int amount = item.getAmount();
//            int maxStackSize = item.getMaxStackSize();
//            while (amount > maxStackSize) {
//                ItemStack fraction = item.clone();
//                fraction.setAmount(maxStackSize);
//                addItem(item, player, inventory);
//                amount -= maxStackSize;
//            }
//        }

        Collection<ItemStack> leftOvers = inventory.addItem(item).values();
        if (!leftOvers.isEmpty()) {
            for (ItemStack stack : leftOvers) {
                overFlow(stack, player);
            }
        }

        if (isDispenser) {
            BlockState state = getBlock().getState();
            switch (state.getType()) {
            case DISPENSER:
                //Dispense until the Dispenser is empty
                Dispenser dispenser = (Dispenser) state;
                while (inventory.firstEmpty() > 0) {
                    dispenser.dispense();
                }
                break;
            case DROPPER:
                //Drop until the Dropper is empty
                Dropper dropper = (Dropper) state;
                while (inventory.firstEmpty() > 0) {
                    dropper.drop();
                }
                break;
            default:
                isDispenser = false;
                break;
            }
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
            String msg = PhatLootsConfig.overflow.replace("<item>", PhatLoots.getItemName(item));
            int amount = item.getAmount();
            msg = amount > 1
                  ? msg.replace("<amount>", String.valueOf(item.getAmount()))
                  : msg.replace("x<amount>", "").replace("<amount>", String.valueOf(item.getAmount()));
            player.sendMessage(msg);
        }
    }

    /**
     * Opens the virtual Inventory to the Player.
     * Plays animation/sound for opening a virtual Inventory
     *
     * @param player The Player opening the Inventory
     * @param inv The virtual Inventory being opened
     * @param global Whether the animation should be sent to everyone (true) or just the Player (false)
     */
    public void openInventory(Player player, Inventory inv, boolean global) {
        if (isDispenser) {
            return;
        }

        openPhatLootChests.put(player, this);
        player.openInventory(inv);

        switch (getBlock().getType()) {
        case TRAPPED_CHEST:
            //Trigger redstone
            for (Block block : findRedstone(getBlock(), false)) {
                trigger(block);
            }
        case ENDER_CHEST:
        case CHEST:
            //Play chest animations
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            if (global) {
                if (inv.getViewers().size() <= 1) { //First viewer
                    //Play for each Player in the World
                    for (Player p: player.getWorld().getPlayers()) {
                        p.playSound(loc, Sound.CHEST_OPEN, 0.75F, 0.95F);
                        p.playNote(loc, (byte) 1, (byte) 1); //Open animation
                    }
                }
            } else {
                //Play for only the individual Player
                player.playSound(loc, Sound.CHEST_OPEN, 0.75F, 0.95F);
                player.playNote(loc, (byte) 1, (byte) 1); //Open animation
            }
            break;
        default: break;
        }
    }

    /**
     * Closes the virtual Inventory for the Player.
     * Plays animation/sound for closing a virtual Inventory
     *
     * @param player The Player closing the Inventory
     * @param inv The virtual Inventory being closed
     * @param global Whether the animation should be sent to everyone (true) or just the Player (false)
     */
    public void closeInventory(Player player, Inventory inv, boolean global) {
        openPhatLootChests.remove(player);
        player.closeInventory();

        switch (getBlock().getType()) {
        case TRAPPED_CHEST:
            //Trigger redstone
            for (Block block : findRedstone(getBlock(), true)) {
                trigger(block);
            }
        case ENDER_CHEST:
        case CHEST:
            //Play chest animations
            Block block = getBlock();
            Location loc = block.getLocation();
            if (global) {
                if (inv.getViewers().size() < 1) { //Last viewer
                    //Play for each Player in the World
                    for (Player p: player.getWorld().getPlayers()) {
                        switch (block.getType()) {
                        case CHEST:
                        case TRAPPED_CHEST:
                        case ENDER_CHEST:
                            p.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                            p.playNote(loc, (byte) 1, (byte) 0); //Close animation
                            break;
                        default:
                            break;
                        }
                    }

                    //Return if not using the Break and Respawn setting
                    if (!useBreakAndRepawn) {
                        return;
                    }

                    //Return if the Inventory is not empty
                    for (ItemStack item : inv.getContents()) {
                        if (item != null && item.getTypeId() != 0) {
                            return;
                        }
                    }

                    long time = getResetTime();

                    //Don't break the chest if it will respawn in 1 second or less
                    if (time > 20) {
                        breakChest(player, time);
                    }
                }
            } else {
                switch (block.getType()) {
                case CHEST:
                case TRAPPED_CHEST:
                case ENDER_CHEST:
                    //Play for only the individual Player
                    player.playSound(loc, Sound.CHEST_CLOSE, 0.75F, 0.95F);
                    player.playNote(loc, (byte) 1, (byte) 0); //Close animation
                    break;
                default:
                    break;
                }
            }
            break;
        default: break;
        }
    }

    /**
     * Returns the shortest amount of time until one of the linked PhatLoots resets
     *
     * @return The amount of time (in ticks) that the PhatLootChest should reset
     */
    public long getResetTime() {
        return getResetTime(PhatLoots.getPhatLoots());
    }

    /**
     * Returns the shortest amount of time until one of the linked PhatLoots resets
     *
     * @param phatLoots The collection of PhatLoots to scan through
     * @return The amount of time (in ticks) that the PhatLootChest should reset
     */
    public long getResetTime(Collection<PhatLoot> phatLoots) {
        long time = -1;
        for (PhatLoot phatLoot : phatLoots) {
            //Check if this is a linked PhatLoot
            if (phatLoot.containsChest(this) && phatLoot.breakAndRespawn) {
                if (phatLoot.global) {
                    long temp = phatLoot.getTimeRemaining(this);
                    if (temp < 1) {
                        continue;
                    }
                    if (time < 0 || temp < time) {
                        time = temp;
                    }
                } else {
                    break;
                }
            }
        }
        //Convert the time from milliseconds to ticks
        return time /= 50;
    }

    /**
     * Finds Redstone Blocks that surround the given Block
     *
     * @param block The given Block which is most likely a Trapped Chest
     * @param on Whether we want blocks that are currently powered
     * @return The List of Blocks
     */
    private static LinkedList<Block> findRedstone(Block block, boolean on) {
        EnumSet redstone = on ? triggeredRedstone : untriggeredRedstone;
        LinkedList<Block> redstoneList = new LinkedList<Block>();
        Block neighbor = block.getRelative(1, 0, 0);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(2, 0, 0);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(0, 0, 1);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(0, 0, 2);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(-1, 0, 0);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(-2, 0, 0);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(0, 0, -1);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(0, 0, -2);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(0, -1, 0);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(0, -2, 0);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(1, -1, 0);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(2, -1, 0);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(0, -1, 1);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(0, -1, 2);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(-1, -11, 0);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(-2, -1, 0);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        neighbor = block.getRelative(0, -1, -1);
        if (redstone.contains(neighbor.getType())) {
            redstoneList.add(neighbor);
        } else {
            neighbor = block.getRelative(0, -1, -2);
            if (redstone.contains(neighbor.getType())) {
                redstoneList.add(neighbor);
            }
        }
        return redstoneList;
    }

    /**
     * Activates the redstone of the given Block
     *
     * @param block The given Block which has redstone qualities
     */
    private static void trigger(Block block) {
        switch (block.getType()) {
        case REDSTONE_WIRE: //Toggle the power level
            block.setData((byte) (block.getData() ^ (byte) 15), true);
            break;
        case REDSTONE_COMPARATOR_OFF: //Turn the Comparator on
            block.setTypeId(Material.REDSTONE_COMPARATOR_ON.getId(), true);
            break;
        case REDSTONE_LAMP_OFF: //Turn the Lamp on
            block.setTypeId(Material.REDSTONE_LAMP_ON.getId(), true);
            break;
        case REDSTONE_TORCH_OFF: //Turn the Torch on
            block.setTypeId(Material.REDSTONE_TORCH_ON.getId(), true);
            break;
        case DIODE_BLOCK_OFF: //Turn the Diode on
            block.setTypeId(Material.DIODE_BLOCK_ON.getId(), true);
            break;
        case DISPENSER: //Dispense
            ((Dispenser) block.getState()).dispense();
            break;
        case DROPPER: //Drop
            ((Dropper) block.getState()).drop();
            break;
        case NOTE_BLOCK: //Play note
            ((NoteBlock) block.getState()).play();
            break;
        case PISTON_BASE: //Toggle the extended bit
            block.setData((byte) (block.getData() ^ (byte) 8), true);
            break;
        case TNT: //Replace the TNT with primed TNT
            block.setTypeId(0);
            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(80);
            break;
        case REDSTONE_COMPARATOR_ON: //Turn the Comparator off
            block.setTypeId(Material.REDSTONE_COMPARATOR_OFF.getId(), true);
            break;
        case REDSTONE_LAMP_ON: //Turn the Lamp off
            block.setTypeId(Material.REDSTONE_LAMP_OFF.getId(), true);
            break;
        case REDSTONE_TORCH_ON: //Turn the Torch off
            block.setTypeId(Material.REDSTONE_TORCH_OFF.getId(), true);
            break;
        case DIODE_BLOCK_ON: //Turn the Diode off
            block.setTypeId(Material.DIODE_BLOCK_OFF.getId(), true);
            break;
        default: break;
        }
    }

    /**
     * Returns the String representation of this PhatLootChest.
     * The format of the returned String is world'x'y'z
     *
     * @return The String representation of this Chest
     */
    @Override
    public String toString() {
        return world + "'" + x + "'" + y + "'" + z;
    }
}

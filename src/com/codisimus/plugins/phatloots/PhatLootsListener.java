package com.codisimus.plugins.phatloots;

import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Listens for interactions with PhatLootChests
 *
 * @author Codisimus
 */
public class PhatLootsListener implements Listener {
    private static HashMap<Player, ForgettableInventory> inventories = new HashMap<Player, ForgettableInventory>();

    /**
     * Checks if a Player loots a PhatLootChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    //@EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        //Return if the Event was cancelled
        if (event.isCancelled())
            return;
        
        //Return if the Chest opener was not a Player
        HumanEntity chestOpener = (Player)event.getPlayer();
        if (!(chestOpener instanceof Player))
            return;
        Player player = (Player)chestOpener;
        
        //We only care about the left side because that is the Block that would be linked
        Inventory inventory = event.getInventory();
        if (inventory instanceof DoubleChestInventory)
            inventory = ((DoubleChestInventory)inventory).getLeftSide();
        else return;
        //Return if the Inventory is not from a Chest
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof Chest))
            return;
        
        //Retrieve the Block of the Inventory
        Block block = ((Chest)holder).getBlock();
        
        //Return if the Chest is not a PhatLootChest
        if (!isPhatLootChest(block))
            return;
        
        //Grab the custom Inventory belonging to the Player
        ForgettableInventory fInventory = inventories.get(player);
        if(fInventory == null) {
            inventory = null;
        }
        else {
        	inventory = fInventory.getInventory();
        }
        if (inventory == null) {
            //Create a new Inventory for the Player
            inventory = PhatLoots.server.createInventory(((DoubleChestInventory)inventory).getRightSide().getHolder(), event.getInventory().getSize(), "Loot!");
            fInventory = new ForgettableInventory(PhatLoots.pm.getPlugin("PhatLoots"), 600L) {
				
				@Override
				protected void execute() {
					inventories.remove(player);
				}
			};
            inventories.put(player, fInventory);
        }
        
        fInventory.cancel();
        fInventory.schedule();
        
        //Swap the Inventories
        player.closeInventory();
        player.openInventory(inventory);
        
        //Return if the Player does not have permission to receive loots
        if (!PhatLoots.hasPermission(player, "use")) {
            player.sendMessage("You do not have permission to receive loots.");
            return;
        }
        
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            
            if (chest != null) {
                phatLoot.getLoot(player, chest, inventory);
                phatLoot.save();
            }
        }
    }
    
    /**
     * Checks if a Player loots a PhatLootChest
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if the Event was cancelled
        if (event.isCancelled())
            return;
        
        Player player = event.getPlayer();
        Inventory inventory;
        Block block = event.getClickedBlock();
        switch (block.getType()) {
            case DISPENSER:
                //Return if the Dispenser was not punched
                if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                    return;

                //Return if the Dispenser is not a PhatLootChest
                if (!isPhatLootChest(block))
                    return;

                //Return if the Player does not have permission to receive loots
                if (!PhatLoots.hasPermission(player, "use")) {
                    player.sendMessage("You do not have permission to receive loots.");
                    return;
                }
                
                Dispenser dispenser = (Dispenser)block.getState();
                inventory = dispenser.getInventory();
                
                break;
                
            case CHEST:
                //Return if the Chest was not opened
                if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                    return;

                //Return if the Chest is not a PhatLootChest
                if (!isPhatLootChest(block))
                    return;
                
                //Grab the custom Inventory belonging to the Player
                inventory = inventories.get(player);
                if (inventory == null) {
                    //Create a new Inventory for the Player
                    Chest chest = (Chest)block.getState();
                    int size = chest.getBlockInventory().getHolder().getInventory().getSize();
                    inventory = PhatLoots.server.createInventory(chest, size, "Loot!");
                    inventories.put(player, inventory);
                }

                //Swap the Inventories
                event.setCancelled(true);
                player.openInventory(inventory);

                //Return if the Player does not have permission to receive loots
                if (!PhatLoots.hasPermission(player, "use")) {
                    player.sendMessage("You do not have permission to receive loots.");
                    return;
                }
                
                break;
                
            default: return;
        }
        
        
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots()) {
            PhatLootChest chest = phatLoot.findChest(block);
            
            if (chest != null) {
                phatLoot.getLoot(player, chest, inventory);
                phatLoot.save();
            }
        }
    }
    
    /**
     * Prevents non-admins from breaking PhatLootsChests
     * 
     * @param event The BlockBreakEvent that occurred
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        
        Block block = event.getBlock();
        
        //Return if the Material of the Block is not a Chest or Furnace
        switch (block.getType()) {
            case CHEST: break;
            case FURNACE: break;
            default: return;
        }
        
        if (!isPhatLootChest(block))
            return;
        
        //Cancel if the Block was not broken by a Player
        Player player = event.getPlayer();
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        
        //Cancel if the Block was not broken by an Admin
        if (!PhatLoots.hasPermission(player, "admin")) {
            player.sendMessage(PhatLootsMessages.permission);
            event.setCancelled(true);
        }
    }
    
    /**
     * Returns true if the given Block is linked to a PhatLoot
     * 
     * @param block the given Block
     * @return True if the given Block is linked to a PhatLoot
     */
    public boolean isPhatLootChest(Block block) {
        for (PhatLoot phatLoot: PhatLoots.getPhatLoots())
            for (PhatLootChest chest: phatLoot.chests)
                if (chest.isBlock(block))
                    return true;
        
        return false;
    }
    
    
    public abstract class ForgettableInventory implements Runnable {
    	
    	private JavaPlugin plugin;
    	private long delay;
    	private int taskId;
    	
    	private Inventory inventory;

    	public ForgettableInventory(JavaPlugin plugin, long delay, Inventory inventory) {
    		this.plugin = plugin;
    		this.delay = delay;
    		this.taskId = 0;
    		this.inventory = inventory;
    	}
    	
    	/**
    	 * @return the inventory
    	 */
    	public Inventory getInventory() {
    		return inventory;
    	}
    	
    	public void schedule() {
    		cancel();
    		taskId  = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
    	}
    	
    	public void cancel() {
    		if(taskId != 0) {
    			plugin.getServer().getScheduler().cancelTask(taskId);
    			taskId = 0;
    		}
    	}

    	@Override
    	public void run() {
    		cancel();
    		execute();
    	}

    	
    	protected abstract void execute();

    	

    }
}
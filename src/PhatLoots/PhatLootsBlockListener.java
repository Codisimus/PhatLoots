
package PhatLoots;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

/**
 *
 * @author Codisimus
 */
public class PhatLootsBlockListener extends BlockListener{

    @Override
    public void onBlockDamage (BlockDamageEvent event) {
        Block block = event.getBlock();
        if (block.getType().equals(Material.CHEST))
            if (SaveSystem.findPhatLoots(block) != null)
                event.setCancelled(true);
    }
}

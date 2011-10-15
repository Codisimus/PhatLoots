
package PhatLoots;

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
        int id = block.getTypeId();
        if (id == 54 && id == 23)
            if (SaveSystem.findPhatLoots(block) != null)
                event.setCancelled(true);
    }
}

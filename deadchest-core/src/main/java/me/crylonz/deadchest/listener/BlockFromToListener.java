package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import static me.crylonz.deadchest.utils.Utils.isGraveBlock;

public class BlockFromToListener implements Listener {

    /**
     * Disable water destruction of Deadchest (for head)
     **/
    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (isGraveBlock(e.getToBlock().getType())) {
            final ChestData chestData = DeadChestLoader.getChestData(e.getToBlock().getLocation());
            if(chestData != null){
                e.setCancelled(true);
            }
        }
    }
}

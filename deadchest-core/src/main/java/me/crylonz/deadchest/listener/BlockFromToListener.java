package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import static me.crylonz.deadchest.DeadChestLoader.chestDataList;
import static me.crylonz.deadchest.utils.Utils.isGraveBlock;

public class BlockFromToListener implements Listener {

    /**
     * Disable water destruction of Deadchest (for head)
     **/
    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (isGraveBlock(e.getToBlock().getType())) {
            for (ChestData cd : chestDataList) {
                if (cd.getChestLocation().equals(e.getToBlock().getLocation())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
}

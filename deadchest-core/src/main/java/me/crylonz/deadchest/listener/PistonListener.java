package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadChestLoader;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class PistonListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD)) {
                final ChestData chestData = DeadChestLoader.getChestData(block.getLocation());
                if (chestData == null) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
}

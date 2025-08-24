package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

import static me.crylonz.deadchest.DeadChestLoader.chestDataList;

public class PistonListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD))
                for (ChestData cd : chestDataList) {
                    if (cd.getChestLocation().equals(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
        }
    }
}

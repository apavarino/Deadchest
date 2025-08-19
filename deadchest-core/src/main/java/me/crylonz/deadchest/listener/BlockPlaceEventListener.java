package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.crylonz.deadchest.DeadChestLoader.chestData;
import static me.crylonz.deadchest.DeadChestLoader.local;

public class BlockPlaceEventListener implements Listener {

    @EventHandler
    public void onBlockPlaceEvent(org.bukkit.event.block.BlockPlaceEvent e) {
        // Disable double chest for grave chest
        if (e.getBlock().getType() == Material.CHEST) {
            for (BlockFace face : BlockFace.values()) {
                Block block = e.getBlock().getRelative(face);
                if (block.getType() == Material.CHEST) {
                    for (ChestData cd : chestData) {
                        if (cd.getChestLocation().equals(block.getLocation())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(local.get("loc_prefix") + local.get("loc_doubleDC"));
                            return;
                        }
                    }
                }
            }
        }
    }
}

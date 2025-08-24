package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static me.crylonz.deadchest.DeadChestLoader.*;
import static me.crylonz.deadchest.utils.Utils.isGraveBlock;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (isGraveBlock(e.getBlock().getType())) {
            if (config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                for (ChestData cd : chestDataList) {
                    if (cd.getChestLocation() == e.getBlock().getLocation()) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
                        break;
                    }
                }

            }
        }
    }
}

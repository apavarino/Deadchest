package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.crylonz.deadchest.DeadChestLoader.chestDataList;
import static me.crylonz.deadchest.DeadChestLoader.config;
import static me.crylonz.deadchest.utils.Utils.generateLog;
import static me.crylonz.deadchest.utils.Utils.isGraveBlock;

public class ExplosionListener implements Listener {

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        chestExplosionHandler(e);
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent e) {
        chestExplosionHandler(e);
    }

    public void chestExplosionHandler(Event e) {
        List<Block> blocklist = new ArrayList<>();
        if (e instanceof EntityExplodeEvent) {
            blocklist = ((EntityExplodeEvent) e).blockList();
        } else if (e instanceof BlockExplodeEvent) {
            blocklist = ((BlockExplodeEvent) e).blockList();
        }

        if (!blocklist.isEmpty()) {
            for (int i = 0; i < blocklist.size(); ++i) {
                Block block = blocklist.get(i);
                for (ChestData cd : chestDataList) {
                    if (isGraveBlock(block.getType()) && cd.getChestLocation().equals(block.getLocation())) {
                        if (config.getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                            blocklist.remove(block);
                            generateLog("Deadchest of [" + cd.getPlayerName() + "] was protected from explosion in " + Objects.requireNonNull(cd.getChestLocation().getWorld()).getName());
                        } else {
                            cd.removeArmorStand();
                            chestDataList.remove(cd);
                            generateLog("Deadchest of [" + cd.getPlayerName() + "] was blown up in " + Objects.requireNonNull(cd.getChestLocation().getWorld()).getName());
                        }
                        break;
                    }
                }
            }
        }
    }
}

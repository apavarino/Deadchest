package me.crylonz.deadchest.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class ArmorstandListener implements Listener {

    @EventHandler
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e) {
        if (!e.getRightClicked().isVisible() && e.getRightClicked().getMetadata("deadchest").size() != 0) {
            e.setCancelled(true);
        }
    }
}

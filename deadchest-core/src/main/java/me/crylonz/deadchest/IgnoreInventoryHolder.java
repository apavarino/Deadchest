package me.crylonz.deadchest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class IgnoreInventoryHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException("Inventory is managed externally.");
    }
}

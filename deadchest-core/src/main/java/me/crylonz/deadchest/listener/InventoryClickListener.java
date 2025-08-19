package me.crylonz.deadchest.listener;


import me.crylonz.deadchest.IgnoreInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import static me.crylonz.deadchest.DeadChestLoader.ignoreList;
import static me.crylonz.deadchest.DeadChestLoader.plugin;
import static me.crylonz.deadchest.utils.IgnoreItemListRepository.saveIgnoreIntoInventory;

public class InventoryClickListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isDeadchestGui(event.getView())) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true);

        final InventoryView view = event.getView();
        final Inventory clickedInv = event.getClickedInventory();
        final int slot = event.getSlot();

        if (clickedInv == null) return;

        // first case : removing item from ignore list
        if (clickedInv == view.getTopInventory()) {
            final ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (clicked.getAmount() <= 1) {
                    clickedInv.setItem(slot, null);
                } else {
                    ItemStack newStack = clicked.clone();
                    newStack.setAmount(clicked.getAmount() - 1);
                    clickedInv.setItem(slot, newStack);
                }
                saveIgnoreIntoInventory(ignoreList);
            });
            return;
        }

        // second case : adding item to ignore list
        if (clickedInv == view.getBottomInventory()) {
            final ItemStack src = event.getCurrentItem();
            if (src == null || src.getType().isAir()) return;

            if (!ignoreList.containsAtLeast(src, 1)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ItemStack item = src.clone();
                    item.setAmount(1);
                    ignoreList.addItem(item);
                    saveIgnoreIntoInventory(ignoreList);
                });
            }
        }
    }


    private boolean isDeadchestGui(InventoryView view) {
        return view.getTopInventory().getHolder() instanceof IgnoreInventoryHolder;
    }
}

package me.crylonz.deadchest.listener;

import me.crylonz.deadchest.ChestData;
import me.crylonz.deadchest.DeadchestPickUpEvent;
import me.crylonz.deadchest.Permission;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

import static me.crylonz.deadchest.DeadChestLoader.*;
import static me.crylonz.deadchest.utils.Utils.generateLog;
import static me.crylonz.deadchest.utils.Utils.isGraveBlock;

public class ClickListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isNearGraveChest(e)) {
                e.setCancelled(true);
                return;
            }
        }

        Block block = e.getClickedBlock();
        if (block != null && isGraveBlock(block.getType())) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                handleChestInteraction(e, block);
            }
        }
    }

    /**
     * Checks if the player clicks near a DeadChest
     */
    private boolean isNearGraveChest(PlayerInteractEvent e) {
        for (ChestData cd : chestData) {
            if (cd.getChestLocation().getWorld() == e.getPlayer().getWorld()
                    && e.getClickedBlock().getWorld() == e.getPlayer().getWorld()
                    && cd.getChestLocation().distance(e.getClickedBlock().getLocation()) <= 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the primary interaction with a DeadChest
     */
    private void handleChestInteraction(PlayerInteractEvent e, Block block) {
        Player player = e.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        boolean playerHasPermission = player.hasPermission(Permission.CHESTPASS.label);

        for (ChestData cd : chestData) {
            if (cd.getChestLocation().equals(block.getLocation())) {
                if (canOpenChest(cd, player, playerUUID, playerHasPermission)) {
                    processChestPickup(e, cd, block, player);
                } else {
                    denyChestAccess(e, player);
                }
                break;
            }
        }
    }

    /**
     * Checks if the player has the right to open the chest
     */
    private boolean canOpenChest(ChestData cd, Player player, String playerUUID, boolean hasPerm) {
        if (!config.getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST)) return true;
        if (playerUUID.equals(cd.getPlayerUUID())) return true;
        if (hasPerm) return true;
        return false;
    }

    /**
     * Denies access to DeadChest
     */
    private void denyChestAccess(PlayerInteractEvent e, Player player) {
        e.setCancelled(true);
        player.sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
    }

    /**
     * Complete DeadChest Recovery Process
     */
    private void processChestPickup(PlayerInteractEvent e, ChestData cd, Block block, Player player) {
        if (!hasGetPermission(player)) {
            e.setCancelled(true);
            return;
        }

        DeadchestPickUpEvent deadchestPickUpEvent = new DeadchestPickUpEvent(cd);
        Bukkit.getServer().getPluginManager().callEvent(deadchestPickUpEvent);

        if (deadchestPickUpEvent.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        restoreOrDropInventory(cd, player, block);
        cleanupChest(cd, block, player);
    }

    /**
     * Checks if the player has permission to retrieve a chest
     */
    private boolean hasGetPermission(Player player) {
        if (config.getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST)
                && !player.hasPermission(Permission.GET.label)) {
            generateLog("Player [" + player.getName() + "] needs deadchest.get permission");
            player.sendMessage(local.get("loc_prefix") + local.get("loc_noPermsToGet"));
            return false;
        }
        return true;
    }

    /**
     * Restore inventory or drop items depending on the mode
     */
    private void restoreOrDropInventory(ChestData cd, Player player, Block block) {
        if (config.getInt(ConfigKey.DROP_MODE) == 1) {
            restoreInventory(cd, player, block.getWorld());
        } else {
            dropInventory(cd, block);
        }
    }

    /**
     * Restores inventory directly to the player
     */
    private void restoreInventory(ChestData cd, Player player, World world) {
        PlayerInventory playerInventory = player.getInventory();
        player.giveExp(cd.getXpStored());

        ItemStack[] originalContents = cd.getInventory().toArray(new ItemStack[0]);
        List<ItemStack> slotReplacedItems = new ArrayList<>();

        // First pass: Restore items to their original inventory positions
        for (int i = 0; i < originalContents.length; i++) {
            ItemStack item = originalContents[i];
            if (item != null) {
                if (i < playerInventory.getSize()
                        && (playerInventory.getItem(i) == null || playerInventory.getItem(i).getType() == Material.AIR)) {
                    playerInventory.setItem(i, item);
                } else {
                    slotReplacedItems.add(item);
                }
            }
        }

        // Second pass: Restore items that would have replaced existing items
        // into empty slots or drop them if there are no available slots.
        for (ItemStack i : slotReplacedItems) {
            if (playerInventory.firstEmpty() != -1) {
                playerInventory.addItem(i);
            } else {
                world.dropItemNaturally(player.getLocation(), i);
            }
        }
    }

    /**
     * Drops the contents of the chest to the ground
     */
    private void dropInventory(ChestData cd, Block block) {
        World world = block.getWorld();
        for (ItemStack i : cd.getInventory()) {
            if (i != null) {
                world.dropItemNaturally(block.getLocation(), i);
            }
        }
        if (cd.getXpStored() != 0) {
            world.spawn(block.getLocation(), ExperienceOrb.class).setExperience(cd.getXpStored());
        }
    }

    /**
     * Removes chest after recovery
     */
    private void cleanupChest(ChestData cd, Block block, Player player) {
        block.setType(Material.AIR);
        chestData.remove(cd);
        fileManager.saveModification();
        block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
        player.playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
        cd.removeArmorStand();
    }
}

package me.crylonz.deadchest;

import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.DeadChestConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.crylonz.deadchest.DeadChest.*;
import static me.crylonz.deadchest.DeadChestManager.generateHologram;
import static me.crylonz.deadchest.DeadChestManager.playerDeadChestAmount;
import static me.crylonz.deadchest.Utils.*;
import static me.crylonz.deadchest.utils.ConfigKey.GENERATE_DEADCHEST_IN_CREATIVE;
import static me.crylonz.deadchest.utils.ConfigKey.KEEP_INVENTORY_ON_PVP_DEATH;

public class DeadChestListener implements Listener {

    private final DeadChest plugin;

    public DeadChestListener(DeadChest plugin) {
        this.plugin = plugin;
    }

    public DeadChestConfig getConfig() {
        return plugin.config;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent e) {

        if (e.getKeepInventory()) {
            generateLog("Keep Inventory is set to ON. No Deadchest generated");
            return;
        }

        if (checkTheEndGeneration(e.getEntity(), plugin)) {
            generateLog("Player dies in the end and " + ConfigKey.GENERATE_IN_THE_END + " is set to false. No Deadchest generated");
            return;
        }

        Player p = e.getEntity().getPlayer();

        if (p == null
                || config.getArray(ConfigKey.EXCLUDED_WORLDS).contains(p.getWorld().getName())
                || (!getConfig().getBoolean(GENERATE_DEADCHEST_IN_CREATIVE)) && p.getGameMode().equals(GameMode.CREATIVE)) {
            generateLog("Player dies in an excluded world or dies in creative with " + GENERATE_DEADCHEST_IN_CREATIVE + " set to false. No Deadchest generated");
            return;
        }

        if (config.getBoolean(KEEP_INVENTORY_ON_PVP_DEATH)) {
            if (p.getKiller() != null) {
                e.setKeepInventory(true);
                e.getDrops().clear();
                generateLog("Player dies in PVP and " + KEEP_INVENTORY_ON_PVP_DEATH + " set to true. No Deadchest generated");
                return;
            }
        }

        int experienceToStore = 0;
        if (config.getInt(ConfigKey.STORE_XP) > 0) {
            experienceToStore = e.getDroppedExp();
            e.setDroppedExp(0);
        }

        if (worldGuardCheck(p) && (p.hasPermission(Permission.GENERATE.label) || !getConfig().getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GENERATE))) {
            if ((playerDeadChestAmount(p) < getConfig().getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) ||
                    getConfig().getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) == 0) && p.getMetadata("NPC").isEmpty()) {

                Block b = p.getLocation().getBlock();

                if (!isInventoryEmpty(p.getInventory())) {

                    computeChestType(b, p);
                    String firstLine = local.replacePlayer(local.get("holo_owner"), e.getEntity().getDisplayName());
                    ArmorStand holoName = generateHologram(b.getLocation(), firstLine, 0.5f, -0.95f, 0.5f, false);
                    String secondLine = local.get("holo_loading");
                    ArmorStand holoTime = generateHologram(b.getLocation(), secondLine, 0.5f, -1.2f, 0.5f, true);

                    ItemStack[] contentsToSave = p.getInventory().getContents().clone();
                    List<String> ignoredItems = config.getArray(ConfigKey.IGNORED_ITEMS);
                    List<String> excludedItems = config.getArray(ConfigKey.EXCLUDED_ITEMS);

                    for (int i = 0; i < contentsToSave.length; i++) {
                        ItemStack item = contentsToSave[i];
                        if (item == null) continue;

                        if (item.getEnchantments().containsKey(Enchantment.VANISHING_CURSE) || excludedItems.contains(item.getType().toString().toUpperCase())) {
                            contentsToSave[i] = null;
                            continue;
                        }

                        if (config.getInt(ConfigKey.ITEM_DURABILITY_LOSS_ON_DEATH) > 0 && item.getItemMeta() instanceof Damageable) {
                            Damageable itemData = ((Damageable) item.getItemMeta());
                            int newDamage = itemData.getDamage() + (int) (item.getType().getMaxDurability() * config.getInt(ConfigKey.ITEM_DURABILITY_LOSS_ON_DEATH) / 100.0);
                            if (newDamage >= item.getType().getMaxDurability()) {
                                contentsToSave[i] = null;
                            } else {
                                itemData.setDamage(newDamage);
                                item.setItemMeta(itemData);
                            }
                        }
                    }

                    Inventory tempSavingInventory = Bukkit.createInventory(null, 45);
                    tempSavingInventory.setContents(contentsToSave);

                    chestData.add(
                            new ChestData(
                                    tempSavingInventory, b.getLocation(), p,
                                    p.hasPermission(Permission.INFINITY_CHEST.label),
                                    holoTime, holoName, experienceToStore
                            )
                    );

                    e.getDrops().removeIf(drop -> {
                        if (drop.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) return true;
                        if (excludedItems.contains(drop.getType().toString().toUpperCase())) return true;
                        return !ignoredItems.contains(drop.getType().toString());
                    });


                    if (getConfig().getBoolean(ConfigKey.DISPLAY_POSITION_ON_DEATH)) {
                        p.sendMessage(local.get("loc_prefix") + local.get("loc_chestPos") + " X: " +
                                ChatColor.WHITE + b.getX() + ChatColor.GOLD + " Y: " +
                                ChatColor.WHITE + b.getY() + ChatColor.GOLD + " Z: " +
                                ChatColor.WHITE + b.getZ());
                    }

                    fileManager.saveModification();
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null || e.getAction() != Action.LEFT_CLICK_BLOCK || !isGraveBlock(block.getType())) {
            return;
        }

        final Player player = e.getPlayer();
        for (ChestData cd : new ArrayList<>(chestData)) {
            if (cd.getChestLocation().equals(block.getLocation())) {
                if (getConfig().getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST) && !player.getUniqueId().toString().equals(cd.getPlayerUUID()) && !player.hasPermission(Permission.CHESTPASS.label)) {
                    player.sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
                    e.setCancelled(true);
                    return;
                }

                DeadchestPickUpEvent deadchestPickUpEvent = new DeadchestPickUpEvent(cd);
                Bukkit.getServer().getPluginManager().callEvent(deadchestPickUpEvent);

                if (!deadchestPickUpEvent.isCancelled()) {
                    generateLog("Deadchest of [" + cd.getPlayerName() + "] was taken by [" + player.getName() + "] in " + player.getWorld().getName());

                    int retrievalMode = config.getInt(ConfigKey.CLICK_RETRIEVAL_MODE);

                    if (retrievalMode == 2) {
                        for(ItemStack item : cd.getInventory()) {
                            if(item != null && item.getType() != Material.AIR) {
                                block.getWorld().dropItemNaturally(block.getLocation(), item);
                            }
                        }
                        if (cd.getXpStored() > 0) {
                            player.giveExp(cd.getXpStored());
                        }
                    } else {
                        int overflowMode = config.getInt(ConfigKey.CLICK_OVERFLOW_DROP_LOCATION);
                        Location dropLocation = (overflowMode == 2) ? player.getLocation() : block.getLocation();
                        boolean useSmartEquip = config.getBoolean(ConfigKey.ATTEMPT_RE_EQUIP);
                        DeadChestManager.giveItemsToPlayer(player, cd, dropLocation, useSmartEquip);
                    }

                    block.setType(Material.AIR);
                    chestData.remove(cd);
                    fileManager.saveModification();
                    block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
                    player.playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                    cd.removeArmorStand();
                } else {
                    e.setCancelled(true);
                }
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (isGraveBlock(e.getBlock().getType())) {
            if (getConfig().getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                for (ChestData cd : chestData) {
                    if (cd.getChestLocation().equals(e.getBlock().getLocation())) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
                        break;
                    }
                }
            }
        }
    }
}
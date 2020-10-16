package me.crylonz;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.crylonz.DeadChest.*;
import static me.crylonz.DeadChestManager.generateHologram;
import static me.crylonz.DeadChestManager.playerDeadChestAmount;
import static me.crylonz.Localization.PREFIX;
import static me.crylonz.Utils.*;

public class DeadChestListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeathEvent(PlayerDeathEvent e) {

        Player p = e.getEntity().getPlayer();


        if (p == null
                || excludedWorlds.contains(p.getWorld().getName())
                || (!generateDeadChestInCreative) && p.getGameMode().equals(GameMode.CREATIVE))
            return;

        if (worldGuardCheck(p) && (p.hasPermission("deadchest.generate") || !requirePermissionToGenerate)) {
            if ((playerDeadChestAmount(p) < maxDeadChestPerPlayer || maxDeadChestPerPlayer == 0) && p.getMetadata("NPC").isEmpty()) {

                World world = p.getWorld();
                Location loc = p.getLocation();

                // Handle case bottom of the world
                if (loc.getY() < 1) {
                    loc.setY(world.getHighestBlockYAt((int) loc.getX(), (int) loc.getZ()) + 1);
                    if (loc.getY() < 1)
                        loc.setY(1);
                }

                // Handle case top of the world
                else if (loc.getBlockY() >= world.getMaxHeight()) {

                    int y = world.getMaxHeight() - 1;
                    loc.setY(y);

                    while (world.getBlockAt(loc).getType() != Material.AIR && y > 0) {
                        y--;
                        loc.setY(y);
                    }

                    if (y < 1) {
                        p.sendMessage(PREFIX + local.get("loc_noDCG"));
                        return;
                    }
                }
                // Handle standard case
                else {
                    if (world.getBlockAt(loc).getType() == Material.DARK_OAK_DOOR ||
                            world.getBlockAt(loc).getType() == Material.ACACIA_DOOR ||
                            world.getBlockAt(loc).getType() == Material.BIRCH_DOOR ||
                            world.getBlockAt(loc).getType() == Material.CRIMSON_DOOR ||
                            world.getBlockAt(loc).getType() == Material.IRON_DOOR ||
                            world.getBlockAt(loc).getType() == Material.JUNGLE_DOOR ||
                            world.getBlockAt(loc).getType() == Material.OAK_DOOR ||
                            world.getBlockAt(loc).getType() == Material.SPRUCE_DOOR ||
                            world.getBlockAt(loc).getType() == Material.WARPED_DOOR ||
                            world.getBlockAt(loc).getType() == Material.VINE ||
                            world.getBlockAt(loc).getType() == Material.LADDER) {
                        Location tmpLoc = getFreeBlockAroundThisPlace(world, loc);

                        if (tmpLoc != null) {
                            loc = tmpLoc;
                        }
                    }


                    if (world.getBlockAt(loc).getType() != Material.AIR
                            && world.getBlockAt(loc).getType() != Material.CAVE_AIR
                            && world.getBlockAt(loc).getType() != Material.VOID_AIR
                            && world.getBlockAt(loc).getType() != Material.WATER
                            && world.getBlockAt(loc).getType() != Material.LAVA) {

                        while (world.getBlockAt(loc).getType() != Material.AIR &&
                                loc.getY() < world.getMaxHeight()) {
                            loc.setY(loc.getY() + 1);
                        }
                    }
                }

                Location groundLocation = loc.clone();
                groundLocation.setY(groundLocation.getY() - 1);
                if (world.getBlockAt(groundLocation).getType() == Material.GRASS_PATH || world.getBlockAt(groundLocation).getType() == Material.FARMLAND) {
                    loc.setY(loc.getY() + 1);
                }

                Block b = world.getBlockAt(loc);

                if (!isInventoryEmpty(p.getInventory())) {
                    b.setType(Material.CHEST);


                    String firstLine = local.replacePlayer(local.get("holo_owner"), e.getEntity().getDisplayName());
                    ArmorStand holoName = generateHologram(b.getLocation(), firstLine, 0.5f, -0.95f, 0.5f, false);

                    String secondLine = local.get("holo_loading");
                    ArmorStand holoTime = generateHologram(b.getLocation(), secondLine, 0.5f, -1.2f, 0.5f, true);

                    // Remove items with curse of vanishing
                    for (ItemStack is : p.getInventory().getContents()) {
                        if (is != null && is.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                            p.getInventory().remove(is);
                        }
                    }

                    if (p.getInventory().getHelmet() != null
                            && p.getInventory().getHelmet().getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                        p.getInventory().setHelmet(null);
                    }

                    if (p.getInventory().getChestplate() != null
                            && p.getInventory().getChestplate().getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                        p.getInventory().setChestplate(null);
                    }

                    if (p.getInventory().getLeggings() != null
                            && p.getInventory().getLeggings().getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                        p.getInventory().setLeggings(null);
                    }

                    if (p.getInventory().getBoots() != null
                            && p.getInventory().getBoots().getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                        p.getInventory().setBoots(null);
                    }

                    if (p.getInventory().getItemInOffHand().getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                        p.getInventory().setItemInOffHand(null);
                    }

                    chestData.add(new ChestData(p.getInventory(), b.getLocation(), p, p.hasPermission("deadChest.infinityChest"), holoTime, holoName));

                    e.getDrops().clear();
                    e.getEntity().getInventory().clear();

                    if (displayDeadChestPositionOnDeath) {
                        p.sendMessage(PREFIX + local.get("loc_chestPos") + " X: " +
                                ChatColor.WHITE + b.getX() + ChatColor.GOLD + " Y: " +
                                ChatColor.WHITE + b.getY() + ChatColor.GOLD + " Z: " +
                                ChatColor.WHITE + b.getZ());
                    }

                    fileManager.saveModification();
                    generateLog("New deadchest for [" + p.getName() + "] in " + b.getWorld().getName() + " at X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ());

                    if (logDeadChestOnConsole)
                        log.info("New deadchest for [" + p.getName() + "] at X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ());
                } else {
                    generateLog("Player [" + p.getName() + "] died without inventory : No Deadchest generated");
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        Block block = e.getClickedBlock();

        if (block != null && block.getType() == Material.CHEST) {
            // if block is a dead chest
            if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                for (ChestData cd : chestData) {
                    if (cd.getChestLocation().equals(block.getLocation())) {
                        // if everybody can open chest or if the chest is the chest of the current player
                        if (!OnlyOwnerCanOpenDeadChest || e.getPlayer().getUniqueId().toString().equals(cd.getPlayerUUID())
                                || e.getPlayer().hasPermission("deadChest.chestPass")) {

                            generateLog("Deadchest of [" + cd.getPlayerName() + "] was taken by [" + e.getPlayer().getName() + "] in " + e.getPlayer().getWorld().getName());

                            // put all item on the inventory
                            if (dropMode == 1) {
                                for (ItemStack i : cd.getInventory()) {
                                    if (i != null) {

                                        if ((i.getType() == Material.IRON_HELMET ||
                                                i.getType() == Material.GOLDEN_HELMET ||
                                                i.getType() == Material.LEATHER_HELMET ||
                                                i.getType() == Material.DIAMOND_HELMET ||
                                                i.getType() == Material.CHAINMAIL_HELMET ||
                                                i.getType() == Material.TURTLE_HELMET ||
                                                i.getType() == Material.NETHERITE_HELMET) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getHelmet() == null)
                                            e.getPlayer().getInventory().setHelmet(i);

                                        else if ((i.getType() == Material.IRON_BOOTS ||
                                                i.getType() == Material.GOLDEN_BOOTS ||
                                                i.getType() == Material.LEATHER_BOOTS ||
                                                i.getType() == Material.DIAMOND_BOOTS ||
                                                i.getType() == Material.CHAINMAIL_BOOTS ||
                                                i.getType() == Material.NETHERITE_BOOTS) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getBoots() == null)
                                            e.getPlayer().getInventory().setBoots(i);

                                        else if ((i.getType() == Material.IRON_CHESTPLATE ||
                                                i.getType() == Material.GOLDEN_CHESTPLATE ||
                                                i.getType() == Material.LEATHER_CHESTPLATE ||
                                                i.getType() == Material.DIAMOND_CHESTPLATE ||
                                                i.getType() == Material.CHAINMAIL_CHESTPLATE ||
                                                i.getType() == Material.NETHERITE_CHESTPLATE ||
                                                i.getType() == Material.ELYTRA) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getChestplate() == null)
                                            e.getPlayer().getInventory().setChestplate(i);

                                        else if ((i.getType() == Material.IRON_LEGGINGS ||
                                                i.getType() == Material.GOLDEN_LEGGINGS ||
                                                i.getType() == Material.LEATHER_LEGGINGS ||
                                                i.getType() == Material.DIAMOND_LEGGINGS ||
                                                i.getType() == Material.CHAINMAIL_LEGGINGS ||
                                                i.getType() == Material.NETHERITE_LEGGINGS) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getLeggings() == null)
                                            e.getPlayer().getInventory().setLeggings(i);

                                        else if (e.getPlayer().getInventory().firstEmpty() != -1)
                                            e.getPlayer().getInventory().addItem(i);
                                        else
                                            e.getPlayer().getWorld().dropItemNaturally(block.getLocation(), i);
                                    }
                                }
                            } else {
                                // pushed item on the ground
                                for (ItemStack i : cd.getInventory()) {
                                    if (i != null) {
                                        e.getPlayer().getWorld().dropItemNaturally(block.getLocation(), i);
                                    }
                                }

                            }

                            block.setType(Material.AIR);
                            chestData.remove(cd);
                            fileManager.saveModification();
                            block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
                            e.getPlayer().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                            cd.removeArmorStand();
                            break;
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(PREFIX + local.get("loc_not_owner"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.CHEST) {
            if (isIndestructible) {

                for (ChestData cd : chestData) {
                    if (cd.getChestLocation() == e.getBlock().getLocation()) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(PREFIX + local.get("loc_not_owner"));
                        break;
                    }
                }
            }
        }
    }

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

        if (blocklist.size() > 0) {
            for (int i = 0; i < blocklist.size(); ++i) {
                Block block = blocklist.get(i);
                for (ChestData cd : chestData) {
                    if (block.getType() == Material.CHEST && cd.getChestLocation().equals(block.getLocation())) {
                        if (isIndestructible) {
                            blocklist.remove(block);
                            generateLog("Deadchest of [" + cd.getPlayerName() + "] was protected from explosion in " + Objects.requireNonNull(cd.getChestLocation().getWorld()).getName());
                        } else {
                            cd.removeArmorStand();
                            chestData.remove(cd);
                            generateLog("Deadchest of [" + cd.getPlayerName() + "] was blown up in " + Objects.requireNonNull(cd.getChestLocation().getWorld()).getName());
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e) {
        if (!e.getRightClicked().isVisible()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.CHEST) {
            for (BlockFace face : BlockFace.values()) {
                Block block = e.getBlock().getRelative(face);
                if (block.getType() == Material.CHEST) {
                    for (ChestData cd : chestData) {
                        if (cd.getChestLocation().equals(block.getLocation())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(PREFIX + local.get("loc_doubleDC"));
                            return;
                        }
                    }
                }
            }
        }
    }
}

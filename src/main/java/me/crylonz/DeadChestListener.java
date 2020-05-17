package me.crylonz;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
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

import static me.crylonz.DeadChest.*;

public class DeadChestListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeathEvent(PlayerDeathEvent e) {

        Player p = e.getEntity().getPlayer();

        if (p == null
                || excludedWorlds.contains(p.getWorld().getName())
                || (!generateDeadChestInCreative) && p.getGameMode().equals(GameMode.CREATIVE))
            return;

        if (p.hasPermission("deadchest.generate") || !requirePermissionToGenerate) {
            if ((DeadChest.deadChestPlayerCount(p) < maxDeadChestPerPlayer || maxDeadChestPerPlayer == 0) && p.getMetadata("NPC").isEmpty()) {

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
                        p.sendMessage(ChatColor.RED + "==========[DeadChest]==========");
                        p.sendMessage(ChatColor.RED + (String) local.get("loc_noDCG"));
                        p.sendMessage(ChatColor.RED + "===============================");
                        return;
                    }
                }
                // Handle standard case
                else {
                    if (world.getBlockAt(loc).getType() != Material.AIR
                            && world.getBlockAt(loc).getType() != Material.CAVE_AIR
                            && world.getBlockAt(loc).getType() != Material.VOID_AIR
                            && world.getBlockAt(loc).getType() != Material.WATER
                            && world.getBlockAt(loc).getType() != Material.LAVA) {

                        while (world.getBlockAt(loc).getType() != Material.AIR && loc.getY() < world.getMaxHeight()) {
                            loc.setY(loc.getY() + 1);
                        }
                    }
                }

                Block b = world.getBlockAt(loc);

                if (!isInventoryEmpty(p.getInventory())) {
                    b.setType(Material.CHEST);

                    String firstLine = local.get("loc_owner") + ": " + e.getEntity().getDisplayName();
                    ArmorStand holoName = DeadChest.generateHologram(b.getLocation(), firstLine, 0.5f, -0.95f, 0.5f);

                    String secondLine = (String) local.get("loc_loading");
                    ArmorStand holoTime = DeadChest.generateHologram(b.getLocation(), secondLine, 0.5f, -1.2f, 0.5f);

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

                    fileManager.saveModification();

                    e.getDrops().clear();
                    e.getEntity().getInventory().clear();
                }
                p.sendMessage(ChatColor.GOLD + "" + local.get("loc_chestPos") + " X: " +
                        ChatColor.WHITE + b.getX() + ChatColor.GOLD + " Y: " +
                        ChatColor.WHITE + b.getY() + ChatColor.GOLD + " Z: " +
                        ChatColor.WHITE + b.getZ());
                if (logDeadChestOnConsole)
                    log.info("New deadchest for [" + p.getName() + "] at X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ());
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


                            // put all item on the inventory
                            if (dropMode == 1) {
                                for (ItemStack i : cd.getInventory()) {
                                    if (i != null) {

                                        if ((i.getType() == Material.IRON_HELMET ||
                                                i.getType() == Material.GOLDEN_HELMET ||
                                                i.getType() == Material.LEATHER_HELMET ||
                                                i.getType() == Material.DIAMOND_HELMET ||
                                                i.getType() == Material.CHAINMAIL_HELMET) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getHelmet() == null)
                                            e.getPlayer().getInventory().setHelmet(i);

                                        else if ((i.getType() == Material.IRON_BOOTS ||
                                                i.getType() == Material.GOLDEN_BOOTS ||
                                                i.getType() == Material.LEATHER_BOOTS ||
                                                i.getType() == Material.DIAMOND_BOOTS ||
                                                i.getType() == Material.CHAINMAIL_BOOTS) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getBoots() == null)
                                            e.getPlayer().getInventory().setBoots(i);

                                        else if ((i.getType() == Material.IRON_CHESTPLATE ||
                                                i.getType() == Material.GOLDEN_CHESTPLATE ||
                                                i.getType() == Material.LEATHER_CHESTPLATE ||
                                                i.getType() == Material.DIAMOND_CHESTPLATE ||
                                                i.getType() == Material.CHAINMAIL_CHESTPLATE ||
                                                i.getType() == Material.ELYTRA) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getChestplate() == null)
                                            e.getPlayer().getInventory().setChestplate(i);

                                        else if ((i.getType() == Material.IRON_LEGGINGS ||
                                                i.getType() == Material.GOLDEN_LEGGINGS ||
                                                i.getType() == Material.LEATHER_LEGGINGS ||
                                                i.getType() == Material.DIAMOND_LEGGINGS ||
                                                i.getType() == Material.CHAINMAIL_LEGGINGS) &&
                                                !i.getEnchantments().containsKey(Enchantment.BINDING_CURSE) &&
                                                e.getPlayer().getInventory().getLeggings() == null)
                                            e.getPlayer().getInventory().setLeggings(i);

                                        else if (e.getPlayer().getInventory().firstEmpty() != -1)
                                            e.getPlayer().getInventory().addItem(i);
                                        else
                                            e.getPlayer().getWorld().dropItemNaturally(block.getLocation(), i);
                                    }
                                }
                                // pushed item on the ground
                            } else {
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
                            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                            cd.removeArmorStand();
                            break;
                        } else {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.RED + "[DeadChest] " + local.get("loc_not_owner"));
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
                        e.getPlayer().sendMessage(ChatColor.RED + "[DeadChest] " + local.get("loc_not_owner"));
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        if (e.blockList().size() > 0) {
            for (int i = 0; i < e.blockList().size(); i++) {
                Block block = e.blockList().get(i);
                for (ChestData cd : chestData) {
                    if (block.getType() == Material.CHEST && cd.getChestLocation().equals(block.getLocation())) {
                        if (isIndestructible)
                            e.blockList().remove(block);
                        else {
                            cd.removeArmorStand();
                            chestData.remove(cd);
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent e) {
        if (e.blockList().size() > 0) {
            for (int i = 0; i < e.blockList().size(); i++) {
                Block block = e.blockList().get(i);
                for (ChestData cd : chestData) {
                    if (block.getType() == Material.CHEST && cd.getChestLocation().equals(block.getLocation())) {
                        if (isIndestructible)
                            e.blockList().remove(block);
                        else {
                            cd.removeArmorStand();
                            chestData.remove(cd);
                        }
                        break;
                    }
                }
            }
        }

    }

    @EventHandler
    public void manipulate(PlayerArmorStandManipulateEvent e) {
        if (!e.getRightClicked().isVisible()) {

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.CHEST) {
            for (BlockFace face : BlockFace.values()) {
                Block block = e.getBlock().getRelative(face);
                if (block.getType() == Material.CHEST) {
                    for (ChestData cd : chestData) {
                        if (cd.getChestLocation().equals(block.getLocation())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(ChatColor.RED + "[Deadchest] " + local.get("loc_doubleDC"));
                            return;
                        }
                    }
                }
            }
        }
    }
}

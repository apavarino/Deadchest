package me.crylonz;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
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

        if (p == null)
            return;

        if (excludedWorlds.contains(p.getWorld().getName()))
            return;

        if (p.hasPermission("deadchest.generate") || !requirePermissionToGenerate) {
            if ((DeadChest.deadChestPlayerCount(p) < maxDeadChestPerPlayer || maxDeadChestPerPlayer == 0) && p.getMetadata("NPC").isEmpty()) {

                World world = e.getEntity().getWorld();
                Location loc = e.getEntity().getLocation();

                if (loc.getY() < 1) {
                    loc.setY(world.getHighestBlockYAt((int) loc.getX(), (int) loc.getZ()));
                    if (loc.getY() < 1)
                        loc.setY(1);
                }


                Block b = world.getBlockAt(loc);

                if (b.getType() != Material.AIR && b.getType() != Material.CAVE_AIR && b.getType() != Material.VOID_AIR && b.getType() != Material.WATER && b.getType() != Material.LAVA) {

                    while (world.getBlockAt(loc).getType() != Material.AIR && loc.getY() < world.getMaxHeight()) {
                        loc.setY(loc.getY() + 1);
                    }

                    b = world.getBlockAt(loc);
                }

                if (!isInventoryEmpty(p.getInventory())) {
                    b.setType(Material.CHEST);

                    Location ownerLoc = new Location(world, b.getX() + 0.5f, b.getY() - 0.95f, b.getZ() + 0.5f);
                    ArmorStand ownerTag = (ArmorStand) world.spawnEntity(ownerLoc, EntityType.ARMOR_STAND);
                    ownerTag.setInvulnerable(true);
                    ownerTag.setGravity(false);
                    ownerTag.setCanPickupItems(false);
                    ownerTag.setVisible(false);
                    ownerTag.setCustomName("× " + loc_owner + ": " + e.getEntity().getDisplayName() + " ×");
                    ownerTag.setCustomNameVisible(true);

                    Location remainingLoc = new Location(world, b.getX() + 0.5f, b.getY() - 1.2f, b.getZ() + 0.5f);
                    ArmorStand remainingTag = (ArmorStand) world.spawnEntity(remainingLoc, EntityType.ARMOR_STAND);
                    remainingTag.setInvulnerable(true);
                    remainingTag.setGravity(false);
                    remainingTag.setCanPickupItems(false);
                    remainingTag.setVisible(false);
                    remainingTag.setCustomName("× " + loc_loading + " ×");
                    remainingTag.setCustomNameVisible(true);

                    chestData.add(new ChestData(p.getInventory(), b.getLocation(), p, p.hasPermission("deadChest.InfinyChest"), remainingTag, ownerTag));

                    fileManager.saveModification();

                    e.getDrops().clear();
                    e.getEntity().getInventory().clear();
                }
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


                            // put all item on the ground
                            for (ItemStack i : cd.getInventory()) {
                                if (i != null) {

                                    if ((i.getType() == Material.IRON_HELMET ||
                                            i.getType() == Material.GOLDEN_HELMET ||
                                            i.getType() == Material.LEATHER_HELMET ||
                                            i.getType() == Material.DIAMOND_HELMET ||
                                            i.getType() == Material.CHAINMAIL_HELMET) &&
                                            e.getPlayer().getInventory().getHelmet() == null)
                                        e.getPlayer().getInventory().setHelmet(i);

                                    else if ((i.getType() == Material.IRON_BOOTS ||
                                            i.getType() == Material.GOLDEN_BOOTS ||
                                            i.getType() == Material.LEATHER_BOOTS ||
                                            i.getType() == Material.DIAMOND_BOOTS ||
                                            i.getType() == Material.CHAINMAIL_BOOTS) &&
                                            e.getPlayer().getInventory().getBoots() == null)
                                        e.getPlayer().getInventory().setBoots(i);

                                    else if ((i.getType() == Material.IRON_CHESTPLATE ||
                                            i.getType() == Material.GOLDEN_CHESTPLATE ||
                                            i.getType() == Material.LEATHER_CHESTPLATE ||
                                            i.getType() == Material.DIAMOND_CHESTPLATE ||
                                            i.getType() == Material.CHAINMAIL_CHESTPLATE ||
                                            i.getType() == Material.ELYTRA) &&
                                            e.getPlayer().getInventory().getChestplate() == null)
                                        e.getPlayer().getInventory().setChestplate(i);

                                    else if ((i.getType() == Material.IRON_LEGGINGS ||
                                            i.getType() == Material.GOLDEN_LEGGINGS ||
                                            i.getType() == Material.LEATHER_LEGGINGS ||
                                            i.getType() == Material.DIAMOND_LEGGINGS ||
                                            i.getType() == Material.CHAINMAIL_LEGGINGS) &&
                                            e.getPlayer().getInventory().getLeggings() == null)
                                        e.getPlayer().getInventory().setLeggings(i);

                                    else if (e.getPlayer().getInventory().firstEmpty() != -1)
                                        e.getPlayer().getInventory().addItem(i);
                                    else
                                        e.getPlayer().getWorld().dropItemNaturally(block.getLocation(), i);
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
                            e.getPlayer().sendMessage(ChatColor.RED + "[DeadChest] " + loc_not_owner);
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
                        e.getPlayer().sendMessage(ChatColor.RED + "[DeadChest] " + loc_not_owner);
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


}

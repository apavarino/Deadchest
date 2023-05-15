package me.crylonz;

import me.crylonz.deadchest.ChestData;
import me.crylonz.utils.ConfigKey;
import me.crylonz.utils.DeadChestConfig;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static me.crylonz.DeadChest.*;
import static me.crylonz.DeadChestManager.generateHologram;
import static me.crylonz.DeadChestManager.playerDeadChestAmount;
import static me.crylonz.Utils.*;

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
            return;
        }

        Player p = e.getEntity().getPlayer();

        if (p == null
                || config.getArray(ConfigKey.EXCLUDED_WORLDS).contains(p.getWorld().getName())
                || (!getConfig().getBoolean(ConfigKey.GENERATE_DEADCHEST_IN_CREATIVE)) && p.getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }

        if (worldGuardCheck(p) && (p.hasPermission(Permission.GENERATE.label) || !getConfig().getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GENERATE))) {
            if ((playerDeadChestAmount(p) < getConfig().getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) ||
                    getConfig().getInt(ConfigKey.MAX_DEAD_CHEST_PER_PLAYER) == 0) && p.getMetadata("NPC").isEmpty()) {

                World world = p.getWorld();
                Location loc = p.getLocation();

                if (!getConfig().getBoolean(ConfigKey.GENERATE_ON_LAVA) && loc.getBlock().getType().equals(Material.LAVA)) {
                    generateLog("Player dies in lava : No deadchest generated");
                    return;
                }

                if (!getConfig().getBoolean(ConfigKey.GENERATE_ON_WATER) && loc.getBlock().getType().equals(Material.WATER)) {
                    generateLog("Player dies in water : No deadchest generated");
                    return;
                }

                if (!getConfig().getBoolean(ConfigKey.GENERATE_ON_RAILS) &&
                        loc.getBlock().getType().equals(Material.RAIL) ||
                        loc.getBlock().getType().equals(Material.ACTIVATOR_RAIL) ||
                        loc.getBlock().getType().equals(Material.DETECTOR_RAIL) ||
                        loc.getBlock().getType().equals(Material.POWERED_RAIL)) {
                    generateLog("Player dies on rails : No deadchest generated");
                    return;
                }

                if (!getConfig().getBoolean(ConfigKey.GENERATE_IN_MINECART) && p.getVehicle() != null) {
                    if (p.getVehicle().getType().equals(EntityType.MINECART)) {
                        generateLog("Player dies in a minecart : No deadchest generated");
                        return;
                    }
                }


                // Handle case bottom of the world
                int minHeight = computeMinHeight();
                if (loc.getY() < minHeight) {
                    loc.setY(world.getHighestBlockYAt((int) loc.getX(), (int) loc.getZ()) + 1);
                    if (loc.getY() < minHeight)
                        loc.setY(minHeight);
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
                        p.sendMessage(local.get("loc_prefix") + local.get("loc_noDCG"));
                        return;
                    }
                }
                // Handle standard case
                else {

                    if (world.getBlockAt(loc).getType() == Material.DARK_OAK_DOOR ||
                            world.getBlockAt(loc).getType() == Material.ACACIA_DOOR ||
                            world.getBlockAt(loc).getType() == Material.BIRCH_DOOR ||
                            (!Utils.isBefore1_16() && world.getBlockAt(loc).getType() == Material.CRIMSON_DOOR) ||
                            world.getBlockAt(loc).getType() == Material.IRON_DOOR ||
                            world.getBlockAt(loc).getType() == Material.JUNGLE_DOOR ||
                            world.getBlockAt(loc).getType() == Material.OAK_DOOR ||
                            world.getBlockAt(loc).getType() == Material.SPRUCE_DOOR ||
                            (!Utils.isBefore1_16() && world.getBlockAt(loc).getType() == Material.WARPED_DOOR) ||
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
                if (isBefore1_17() && world.getBlockAt(groundLocation).getType() == Material.valueOf("GRASS_PATH") ||
                        !isBefore1_17() && world.getBlockAt(groundLocation).getType() == Material.DIRT_PATH ||
                        world.getBlockAt(groundLocation).getType() == Material.FARMLAND) {
                    loc.setY(loc.getY() + 1);
                }

                Block b = world.getBlockAt(loc);

                if (!isInventoryEmpty(p.getInventory())) {

                    computeChestType(b, p);
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

                    for (String item : config.getArray(ConfigKey.EXCLUDED_ITEMS)) {
                        if (item != null && Material.getMaterial(item.toUpperCase()) != null) {
                            p.getInventory().remove(Material.getMaterial(item.toUpperCase()));
                        }
                    }

                    if (config.getBoolean(ConfigKey.STORE_XP)) {
                        e.setDroppedExp(0);
                    }

                    log.warning(String.valueOf(b.getLocation()));
                    chestData.add(
                            new ChestData(
                                    p.getInventory(),
                                    b.getLocation(),
                                    p,
                                    p.hasPermission(Permission.INFINITY_CHEST.label),
                                    holoTime,
                                    holoName,
                                    computeXpToStore(p)
                            )
                    );

                    ItemStack[] backupInv = p.getInventory().getContents();
                    e.getDrops().clear();
                    p.getInventory().clear();

                    if (getConfig().getBoolean(ConfigKey.DISPLAY_POSITION_ON_DEATH)) {
                        p.sendMessage(local.get("loc_prefix") + local.get("loc_chestPos") + " X: " +
                                ChatColor.WHITE + b.getX() + ChatColor.GOLD + " Y: " +
                                ChatColor.WHITE + b.getY() + ChatColor.GOLD + " Z: " +
                                ChatColor.WHITE + b.getZ());
                    }

                    fileManager.saveModification();

                    generateLog("New deadchest for [" + p.getName() + "] in " + b.getWorld().getName() + " at X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ());
                    generateLog("Chest content : " + Arrays.asList(backupInv));

                    if (getConfig().getBoolean(ConfigKey.LOG_DEADCHEST_ON_CONSOLE))
                        log.info("New deadchest for [" + p.getName() + "] at X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ());
                } else {
                    generateLog("Player [" + p.getName() + "] died without inventory : No Deadchest generated");
                }
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            for (ChestData cd : chestData) {
                if (cd.getChestLocation().getWorld() == e.getPlayer().getWorld() &&
                        cd.getChestLocation().distance(e.getClickedBlock().getLocation()) <= 1) {
                    e.setCancelled(true);
                    break;
                }
            }
        }

        Block block = e.getClickedBlock();

        if (block != null && isGraveBlock(block.getType())) {
            final Player player = e.getPlayer();
            final String playerUUID = player.getUniqueId().toString();
            final boolean playerHasPermission = player.hasPermission(Permission.CHESTPASS.label);
            final World playerWorld = player.getWorld();
            // if block is a dead chest
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                for (ChestData cd : chestData) {
                    if (cd.getChestLocation().equals(block.getLocation())) {
                        // if everybody can open chest or if the chest is the chest of the current player
                        if (!getConfig().getBoolean(ConfigKey.ONLY_OWNER_CAN_OPEN_CHEST) || playerUUID.equals(cd.getPlayerUUID()) || playerHasPermission) {

                            if (!player.hasPermission(Permission.GET.label) && getConfig().getBoolean(ConfigKey.REQUIRE_PERMISSION_TO_GET_CHEST)) {
                                generateLog(String.format("Player [%s] need to have deadchest.get permission to generate", player.getName()));
                                player.sendMessage(local.get("loc_prefix") + local.get("loc_noPermsToGet"));
                                e.setCancelled(true);
                                return;
                            }
                            DeadchestPickUpEvent deadchestPickUpEvent = new DeadchestPickUpEvent(cd);
                            Bukkit.getServer().getPluginManager().callEvent(deadchestPickUpEvent);

                            if (!deadchestPickUpEvent.isCancelled()) {
                                generateLog("Deadchest of [" + cd.getPlayerName() + "] was taken by [" + player.getName() + "] in " + playerWorld.getName());

                                // put all item on the inventory
                                if (getConfig().getInt(ConfigKey.DROP_MODE) == 1) {
                                    final PlayerInventory playerInventory = player.getInventory();
                                    player.giveExp(cd.getXpStored());
                                    for (ItemStack i : cd.getInventory()) {
                                        if (i != null) {

                                            if (Utils.isHelmet(i) && playerInventory.getHelmet() == null)
                                                playerInventory.setHelmet(i);

                                            else if (Utils.isBoots(i) && playerInventory.getBoots() == null)
                                                playerInventory.setBoots(i);

                                            else if (Utils.isChestplate(i) && playerInventory.getChestplate() == null)
                                                playerInventory.setChestplate(i);

                                            else if (Utils.isLeggings(i) && playerInventory.getLeggings() == null)
                                                playerInventory.setLeggings(i);

                                            else if (playerInventory.firstEmpty() != -1)
                                                playerInventory.addItem(i);
                                            else
                                                playerWorld.dropItemNaturally(block.getLocation(), i);
                                        }
                                    }
                                } else {
                                    // pushed item on the ground
                                    for (ItemStack i : cd.getInventory()) {
                                        if (i != null) {
                                            playerWorld.dropItemNaturally(block.getLocation(), i);
                                        }
                                        if (cd.getXpStored() != 0) {
                                            playerWorld.spawn(block.getLocation(), ExperienceOrb.class).setExperience(cd.getXpStored());
                                        }
                                    }
                                }

                                block.setType(Material.AIR);
                                chestData.remove(cd);
                                fileManager.saveModification();
                                block.getWorld().playEffect(block.getLocation(), Effect.MOBSPAWNER_FLAMES, 10);
                                player.playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                                cd.removeArmorStand();
                                break;
                            } else {
                                e.setCancelled(true);
                            }
                        } else {
                            e.setCancelled(true);
                            player.sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (isGraveBlock(e.getBlock().getType())) {
            if (getConfig().getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
                for (ChestData cd : chestData) {
                    if (cd.getChestLocation() == e.getBlock().getLocation()) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(local.get("loc_prefix") + local.get("loc_not_owner"));
                        break;
                    }
                }

            }
        }
    }

    /**
     * Disable water destruction of Deadchest (for head)
     **/
    @EventHandler
    public void onBlockFromToEvent(BlockFromToEvent e) {
        if (isGraveBlock(e.getToBlock().getType())) {
            for (ChestData cd : chestData) {
                if (cd.getChestLocation().equals(e.getToBlock().getLocation())) {
                    e.setCancelled(true);
                    break;
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
                    if (isGraveBlock(block.getType()) && cd.getChestLocation().equals(block.getLocation())) {
                        if (getConfig().getBoolean(ConfigKey.INDESTRUCTIBLE_CHEST)) {
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
        if (!e.getRightClicked().isVisible() && e.getRightClicked().getMetadata("deadchest").size() != 0) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        // Disable double chest for grave chest
        if (e.getBlock().getType() == Material.CHEST) {
            for (BlockFace face : BlockFace.values()) {
                Block block = e.getBlock().getRelative(face);
                if (block.getType() == Material.CHEST) {
                    for (ChestData cd : chestData) {
                        if (cd.getChestLocation().equals(block.getLocation())) {
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(local.get("loc_prefix") + local.get("loc_doubleDC"));
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block != null && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD))
                for (ChestData cd : chestData) {
                    if (cd.getChestLocation().equals(block.getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
        }
    }
}



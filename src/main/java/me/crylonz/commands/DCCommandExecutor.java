package me.crylonz.commands;

import me.crylonz.ChestData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static me.crylonz.DeadChest.*;
import static me.crylonz.DeadChestManager.cleanAllDeadChests;

public class DCCommandExecutor implements CommandExecutor {

    private final Plugin p;

    public DCCommandExecutor(Plugin p) {
        this.p = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("dc")) {

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            reloadPlugin();
                            p.sendMessage(local.get("loc_prefix") + local.get("loc_reload"));
                            log.info(local.get("loc_prefix") + " Plugin is reloading");
                        } else
                            p.sendMessage(local.get("loc_prefix") + local.get("loc_noperm") + " deadchest.admin");

                    } else {
                        reloadPlugin();
                        log.info(local.get("loc_prefix") + "Plugin is reloading");
                    }
                } else if (args[0].equalsIgnoreCase("repair")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            Collection<Entity> entities = p.getWorld().getNearbyEntities(
                                    p.getLocation(), 100.0D, 25.0D, 100.0D);

                            int holoRemoved = 0;
                            for (Entity entity : entities) {
                                if (entity.getType() == EntityType.ARMOR_STAND) {
                                    ArmorStand as = (ArmorStand) entity;

                                    if (as.hasMetadata("deadchest")) {
                                        holoRemoved++;
                                        entity.remove();
                                    }

                                    // Deprecated (support for deadchest 3.X and lower
                                    else if (as.getCustomName() != null && as.getCustomName().contains("×")) {
                                        holoRemoved++;
                                        entity.remove();
                                    }

                                }
                            }
                            p.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" + holoRemoved + "] hologram(s) removed");
                        } else {
                            p.sendMessage(local.get("loc_prefix") + local.get("loc_noperm") + " deadchest.admin");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeinfinite")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            int cpt = 0;

                            if (chestData != null && !chestData.isEmpty()) {

                                Iterator<ChestData> chestDataIt = chestData.iterator();
                                while (chestDataIt.hasNext()) {

                                    ChestData cd = chestDataIt.next();

                                    if (cd.getChestLocation().getWorld() != null) {

                                        if (cd.isInfinity() || chestDuration == 0) {
                                            // remove chest
                                            Location loc = cd.getChestLocation();
                                            loc.getWorld().getBlockAt(loc).setType(Material.AIR);

                                            // remove holographics
                                            cd.removeArmorStand();

                                            // remove in memory
                                            chestDataIt.remove();

                                            cpt++;
                                        }
                                    }
                                }
                                fileManager.saveModification();
                            }
                            p.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeall")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            int cpt = cleanAllDeadChests();
                            p.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;


                        if ((args.length == 2 && p.hasPermission("deadchest.remove.other")) ||
                                (args.length == 1 && p.hasPermission("deadchest.remove.own"))) {

                            String pname = args.length == 2 ? args[1] : p.getName();
                            int cpt = 0;
                            if (chestData != null && !chestData.isEmpty()) {

                                Iterator<ChestData> chestDataIt = chestData.iterator();
                                while (chestDataIt.hasNext()) {

                                    ChestData cd = chestDataIt.next();

                                    if (cd.getChestLocation().getWorld() != null) {

                                        if (cd.getPlayerName().equalsIgnoreCase(pname)) {
                                            // remove chest
                                            Location loc = cd.getChestLocation();
                                            loc.getWorld().getBlockAt(loc).setType(Material.AIR);

                                            // remove holographics
                                            cd.removeArmorStand();

                                            // remove in memory
                                            chestDataIt.remove();

                                            cpt++;
                                        }
                                    }
                                }
                                fileManager.saveModification();
                            }
                            p.sendMessage(local.get("loc_prefix") + ChatColor.GOLD + "Operation complete. [" +
                                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
                        } else {
                            p.sendMessage(local.get("loc_prefix") + ChatColor.RED + "Usage : /dc remove <PlayerName>");
                        }


                    }
                } else if (args[0].equalsIgnoreCase("list")) {

                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.hasPermission("deadchest.list.own") || !permissionRequiredToListOwn) {

                            if (args.length == 1) {
                                Date now = new Date();
                                if (!chestData.isEmpty()) {
                                    p.sendMessage(local.get("loc_prefix") + local.get("loc_dclistown") + " :");
                                    for (ChestData cd : chestData) {
                                        if (cd.getPlayerUUID().equalsIgnoreCase(p.getUniqueId().toString())) {

                                            if (cd.isInfinity() || chestDuration == 0) {
                                                p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " |"
                                                        + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                        + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                        + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                        + " | "
                                                        + "∞ " + local.get("loc_endtimer"));
                                            } else {
                                                long diff = now.getTime() - (cd.getChestDate().getTime() + chestDuration * 1000);
                                                long diffSeconds = Math.abs(diff / 1000 % 60);
                                                long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                                                long diffHours = Math.abs(diff / (60 * 60 * 1000));
                                                p.sendMessage("-" + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                        + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                        + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                        + " | " +
                                                        +diffHours + "h "
                                                        + diffMinutes + "m "
                                                        + diffSeconds + "s " + local.get("loc_endtimer"));
                                            }
                                        }
                                    }
                                } else {
                                    p.sendMessage(local.get("loc_prefix") + local.get("loc_nodc"));
                                }

                            } else if (args.length == 2) {
                                if (p.hasPermission("deadchest.list.other")) {
                                    if (args[1].equalsIgnoreCase("all")) {
                                        Date now = new Date();
                                        if (!chestData.isEmpty()) {
                                            p.sendMessage(local.get("loc_prefix") + local.get("loc_dclistall") + ":");
                                            for (ChestData cd : chestData) {


                                                if (cd.isInfinity() || chestDuration == 0) {
                                                    p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " | "
                                                            + ChatColor.GOLD + cd.getPlayerName() + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                            + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                            + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                            + " | "
                                                            + "∞ " + local.get("loc_endtimer"));
                                                } else {
                                                    long diff = now.getTime() - (cd.getChestDate().getTime() + chestDuration * 1000);
                                                    long diffSeconds = Math.abs(diff / 1000 % 60);
                                                    long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                                                    long diffHours = Math.abs(diff / (60 * 60 * 1000));
                                                    p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " | "
                                                            + ChatColor.GOLD + cd.getPlayerName() + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                            + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                            + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                            + " | " +
                                                            +diffHours + "h "
                                                            + diffMinutes + "m "
                                                            + diffSeconds + "s " + local.get("loc_endtimer"));
                                                }
                                            }
                                        } else {
                                            p.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                                        }

                                    } else {
                                        Date now = new Date();
                                        if (!chestData.isEmpty()) {
                                            p.sendMessage(local.get("loc_prefix") + ChatColor.GREEN + args[1] + " deadchests :");
                                            for (ChestData cd : chestData) {
                                                if (cd.getPlayerName().equalsIgnoreCase(args[1])) {

                                                    if (cd.isInfinity() || chestDuration == 0) {
                                                        p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " |"
                                                                + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                                + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                                + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                                + " | "
                                                                + "∞ " + local.get("loc_endtimer"));
                                                    } else {
                                                        long diff = now.getTime() - (cd.getChestDate().getTime() + chestDuration * 1000);
                                                        long diffSeconds = Math.abs(diff / 1000 % 60);
                                                        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                                                        long diffHours = Math.abs(diff / (60 * 60 * 1000));
                                                        p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " |"
                                                                + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                                + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                                + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                                + " | "
                                                                + diffHours + "h "
                                                                + diffMinutes + "m "
                                                                + diffSeconds + "s " + local.get("loc_endtimer"));
                                                    }
                                                }
                                            }
                                        } else {
                                            p.sendMessage(local.get("loc_prefix") + local.get("loc_nodcs"));
                                        }
                                    }
                                } // deadchest.list.other
                            }

                        } else
                            p.sendMessage(local.get("loc_prefix") + local.get("loc_noperm") + " deadchest.list.own");
                    }
                } else if (args[0].equalsIgnoreCase("giveback") || args[0].equalsIgnoreCase("gb")) {
                    if (sender instanceof Player) {

                        Player p = (Player) sender;
                        Player targetP = null;

                        if (p.hasPermission("deadchest.giveBack")) {
                            if (args.length == 2) {

                                for (ChestData cd : chestData) {
                                    if (cd.getPlayerName().equalsIgnoreCase(args[1])) {

                                        targetP = Bukkit.getPlayer(UUID.fromString(cd.getPlayerUUID()));

                                        if (targetP != null && p.isOnline()) {
                                            for (ItemStack i : cd.getInventory()) {
                                                if (i != null) {
                                                    targetP.getWorld().dropItemNaturally(targetP.getLocation(), i);
                                                }
                                            }

                                            // Remove chest and hologram
                                            targetP.getWorld().getBlockAt(cd.getChestLocation()).setType(Material.AIR);
                                            cd.removeArmorStand();
                                            chestData.remove(cd);
                                        }
                                        break;
                                    }
                                }
                                if (targetP != null) {
                                    p.sendMessage(local.get("loc_prefix") + local.get("loc_dcgbsuccess"));
                                    targetP.sendMessage(local.get("loc_prefix") + local.get("loc_gbplayer"));
                                } else {
                                    p.sendMessage(local.get("loc_prefix") + local.get("loc_givebackInfo"));
                                }
                            } else {
                                p.sendMessage(local.get("loc_prefix") + ChatColor.WHITE + "Usage : /dc giveback <PlayerName>");
                            }
                        }
                    }
                } else {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(local.get("loc_prefix") + ChatColor.WHITE + "Type " + ChatColor.GREEN + "/help dc" + ChatColor.WHITE + " for help");
                    }
                }

            } else {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.sendMessage(local.get("loc_prefix") + ChatColor.WHITE + "Type " + ChatColor.GREEN + "/help dc" + ChatColor.WHITE + " for help");
                }
            }


        }
        return true;
    }

    public void reloadPlugin() {

        fileManager.reloadChestDataConfig();
        fileManager.reloadLocalizationConfig();
        p.reloadConfig();
        @SuppressWarnings("unchecked")
        ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getChestDataConfig().get("chestData");

        @SuppressWarnings("unchecked")
        ArrayList<String> tmpExludedWorld = (ArrayList<String>) p.getConfig().get("ExcludedWorld");
        ArrayList<String> tmpExludedItems = (ArrayList<String>) p.getConfig().get("ExcludedItems");


        if (tmp != null) {
            chestData = (List<ChestData>) fileManager.getChestDataConfig().get("chestData");
        }

        if (tmpExludedWorld != null)
            excludedWorlds = tmpExludedWorld;

        if (tmpExludedItems != null)
            excludedItems = tmpExludedItems;

        isIndestructible = p.getConfig().getBoolean("IndestuctibleChest");
        OnlyOwnerCanOpenDeadChest = p.getConfig().getBoolean("OnlyOwnerCanOpenDeadChest");
        chestDuration = p.getConfig().getInt("DeadChestDuration");
        maxDeadChestPerPlayer = (int) p.getConfig().get("maxDeadChestPerPlayer");
        logDeadChestOnConsole = (boolean) p.getConfig().get("logDeadChestOnConsole");
        requirePermissionToGenerate = (boolean) p.getConfig().get("RequirePermissionToGenerate");
        requirePermissionToGetChest = (boolean) p.getConfig().get("RequirePermissionToGetChest");
        permissionRequiredToListOwn = (boolean) p.getConfig().get("RequirePermissionToListOwn");
        autoCleanUpOnStart = (boolean) p.getConfig().get("AutoCleanupOnStart");
        generateDeadChestInCreative = (boolean) p.getConfig().get("GenerateDeadChestInCreative");
        displayDeadChestPositionOnDeath = (boolean) p.getConfig().get("DisplayDeadChestPositionOnDeath");
        itemsDroppedAfterTimeOut = (boolean) p.getConfig().get("ItemsDroppedAfterTimeOut");
        enableWorldGuardDetection = (boolean) p.getConfig().get("EnableWorldGuardDetection");
        dropMode = (int) p.getConfig().get("DropMode");
        dropBlock = (int) p.getConfig().get("DropBlock");
        generateOnLava = (boolean) p.getConfig().get("GenerateOnLava");
        generateOnWater = (boolean) p.getConfig().get("GenerateOnWater");
        generateOnRails = (boolean) p.getConfig().get("GenerateOnRails");
        generateInMinecart = (boolean) p.getConfig().get("GenerateInMinecart");

        local.set(fileManager.getLocalizationConfig().getConfigurationSection("localisation").getValues(true));
    }
}

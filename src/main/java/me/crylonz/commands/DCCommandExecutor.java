package me.crylonz.commands;

import me.crylonz.ChestData;
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
import org.bukkit.plugin.Plugin;

import java.util.*;

import static me.crylonz.DeadChest.*;


public class DCCommandExecutor implements CommandExecutor {

    private Plugin p;

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
                            p.sendMessage(ChatColor.GOLD + "[DeadChest] " + loc_reload);
                            log.info("[DeadChest] Plugin is reloading");
                        } else
                            p.sendMessage(ChatColor.RED + "[DeadChest] " + loc_noperm + " deadchest.admin");

                    } else {
                        reloadPlugin();
                        log.info("[DeadChest] Plugin is reloading");
                    }
                } else if (args[0].equalsIgnoreCase("repair")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            Collection<Entity> entities = p.getWorld().getNearbyEntities(
                                    p.getLocation(), 100.0D, 25.0D, 100.0D);

                            for (Entity entity : entities) {
                                if (entity.getType() == EntityType.ARMOR_STAND) {
                                    ArmorStand as = (ArmorStand) entity;
                                    if (as.getCustomName() != null && as.getCustomName().contains("×"))
                                        entity.remove();
                                }
                            }
                            p.sendMessage(ChatColor.GOLD + "[DeadChest] Operation complete");
                        } else {
                            p.sendMessage(ChatColor.RED + "[DeadChest] " + loc_noperm + " deadchest.admin");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeinfinate")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            int cpt = 0;

                            if (chestData != null && !chestData.isEmpty()) {

                                Iterator<ChestData> chestDataIt = chestData.iterator();
                                while (chestDataIt.hasNext()) {

                                    ChestData cd = chestDataIt.next();

                                    if (cd.getChestLocation().getWorld() != null) {

                                        if (cd.isInfiny() || chestDuration == 0) {
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
                            p.sendMessage(ChatColor.GOLD + "[DeadChest] Operation complete. [" +
                                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("removeall")) {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;

                        if (p.hasPermission("deadchest.admin")) {
                            int cpt = cleanAllDeadChests();
                            p.sendMessage(ChatColor.GOLD + "[DeadChest] Operation complete. [" +
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
                            p.sendMessage(ChatColor.GOLD + "[DeadChest] Operation complete. [" +
                                    ChatColor.GREEN + cpt + ChatColor.GOLD + "] deadchest(s) removed");
                        } else {
                            p.sendMessage(ChatColor.RED + "[DeadChest] Usage : /dc remove <PlayerName>");
                        }


                    }
                } else if (args[0].equalsIgnoreCase("list")) {

                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        if (p.hasPermission("deadchest.list.own") || !permissionRequiredToListOwn) {

                            if (args.length == 1) {
                                Date now = new Date();
                                if (!chestData.isEmpty()) {
                                    p.sendMessage(ChatColor.GOLD + "[DeadChest] " + ChatColor.GREEN + loc_dclistown + " :");
                                    for (ChestData cd : chestData) {
                                        if (cd.getPlayerUUID().equalsIgnoreCase(p.getUniqueId().toString())) {

                                            if (cd.isInfiny() || chestDuration == 0) {
                                                p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " |"
                                                        + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                        + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                        + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                        + " | "
                                                        + "∞ " + loc_endtimer);
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
                                                        + diffSeconds + "s " + loc_endtimer);
                                            }


                                        }
                                    }
                                } else {
                                    p.sendMessage(ChatColor.GOLD + "[DeadChest] " + ChatColor.GREEN + loc_nodc);
                                }

                            } else if (args.length == 2) {
                                if (p.hasPermission("deadchest.list.other")) {
                                    if (args[1].equalsIgnoreCase("all")) {
                                        Date now = new Date();
                                        if (!chestData.isEmpty()) {
                                            p.sendMessage(ChatColor.GOLD + "[DeadChest] " + ChatColor.GREEN + loc_dclistall + ":");
                                            for (ChestData cd : chestData) {


                                                if (cd.isInfiny() || chestDuration == 0) {
                                                    p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " | "
                                                            + ChatColor.GOLD + cd.getPlayerName() + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                            + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                            + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                            + " | "
                                                            + "∞ " + loc_endtimer);
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
                                                            + diffSeconds + "s " + loc_endtimer);
                                                }
                                            }
                                        }

                                    } else {
                                        Date now = new Date();
                                        if (!chestData.isEmpty()) {
                                            p.sendMessage(ChatColor.GOLD + "[DeadChest] " + ChatColor.GREEN + args[1] + " deadchests :");
                                            for (ChestData cd : chestData) {
                                                if (cd.getPlayerName().equalsIgnoreCase(args[1])) {

                                                    if (cd.isInfiny() || chestDuration == 0) {
                                                        p.sendMessage("-" + ChatColor.AQUA + " World: " + ChatColor.WHITE + cd.getChestLocation().getWorld().getName() + " |"
                                                                + ChatColor.AQUA + " X: " + ChatColor.WHITE + cd.getChestLocation().getX()
                                                                + ChatColor.AQUA + " Y: " + ChatColor.WHITE + cd.getChestLocation().getY()
                                                                + ChatColor.AQUA + " Z: " + ChatColor.WHITE + cd.getChestLocation().getZ()
                                                                + " | "
                                                                + "∞ " + loc_endtimer);
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
                                                                + +diffHours + "h "
                                                                + diffMinutes + "m "
                                                                + diffSeconds + "s " + loc_endtimer);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } // deadchest.list.other

                            }

                        } else
                            p.sendMessage(ChatColor.RED + "[DeadChest] " + loc_noperm + " deadchest.list.own");
                    }
                } else {
                    if (sender instanceof Player) {
                        Player p = (Player) sender;
                        p.sendMessage(ChatColor.WHITE + "[DeadChest] Type " + ChatColor.GREEN + "/help dc" + ChatColor.WHITE + " for help");
                    }
                }

            } else {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.sendMessage(ChatColor.WHITE + "[DeadChest] Type " + ChatColor.GREEN + "/help dc" + ChatColor.WHITE + " for help");
                }
            }

        }
        return true;
    }


    public void reloadPlugin() {

        fileManager.reloadConfig2();
        p.reloadConfig();
        ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getConfig2().get("chestData");
        ArrayList<String> tmpExludedWorld = (ArrayList<String>) p.getConfig().get("ExcludedWorld");

        if (tmp != null)
            chestData = (List<ChestData>) fileManager.getConfig2().get("chestData");

        if (tmpExludedWorld != null)
            excludedWorlds = tmpExludedWorld;

        isIndestructible = p.getConfig().getBoolean("IndestuctibleChest");
        OnlyOwnerCanOpenDeadChest = p.getConfig().getBoolean("OnlyOwnerCanOpenDeadChest");
        chestDuration = p.getConfig().getInt("DeadChestDuration");
        maxDeadChestPerPlayer = (int) p.getConfig().get("maxDeadChestPerPlayer");
        logDeadChestOnConsole = (boolean) p.getConfig().get("logDeadChestOnConsole");
        requirePermissionToGenerate = (boolean) p.getConfig().get("RequirePermissionToGenerate");
        permissionRequiredToListOwn = (boolean) p.getConfig().get("RequirePermissionToListOwn");
        autocleanupOnStart = (boolean) p.getConfig().get("AutoCleanupOnStart");
    }
}

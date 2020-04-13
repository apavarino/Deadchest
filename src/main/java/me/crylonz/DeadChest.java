package me.crylonz;

import me.crylonz.commands.DCCommandExecutor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public class DeadChest extends JavaPlugin {

    static {
        ConfigurationSerialization.registerClass(ChestData.class, "ChestData");
    }

    private boolean isChanged = false;

    public static FileManager fileManager;

    public static List<ChestData> chestData;
    public static boolean isIndestructible = true;
    public static boolean OnlyOwnerCanOpenDeadChest = true;
    public static int chestDuration = 600;
    public static int maxDeadChestPerPlayer = 5;
    public static boolean logDeadChestOnConsole = false;
    public static boolean requirePermissionToGenerate = false;
    public static boolean permissionRequiredToListOwn = false;
    public static boolean autocleanupOnStart = false;
    public static boolean generateDeadChestInCreative = true;
    public static int dropMode = 1;
    public static ArrayList<String> excludedWorlds = new ArrayList<>();

    public static Map<String, Object> local = new HashMap<>();

    public final static Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new DeadChestListener(), this);

        chestData = new ArrayList<>();
        fileManager = new FileManager(this);

        local.put("loc_owner", "Owner");
        local.put("loc_loading", "Loading...");
        local.put("loc_not_owner", "This is not your Deadchest !");
        local.put("loc_infinityChest", "Infinity chest");
        local.put("loc_endtimer", "left");
        local.put("loc_reload", "Reload successfull..");
        local.put("loc_noperm", "You need permission");
        local.put("loc_nodc", "You don't have any deadchest");
        local.put("loc_nodcs", "There is currently no deadchest");
        local.put("loc_dclistall", "List of all dead chests");
        local.put("loc_dclistown", "List of your dead chests");
        local.put("loc_doubleDC", "You can't put a chest next to a Deadchest");
        local.put("loc_maxHeight", "You are dead above the maximum height.");
        local.put("loc_noDCG", "No deadchest generated.");
        local.put("loc_givebackInfo", "This player is offline or don't have any active deadchest");
        local.put("loc_dcgbsuccess", "The oldest deadchest content of this player returned to him");
        local.put("loc_gbplayer", "You have retrieved the content of your deadchest");

        Objects.requireNonNull(this.getCommand("dc"), "Command dc not found")
                .setExecutor(new DCCommandExecutor(this));

        initializeConfig();

        if (autocleanupOnStart) {
            cleanAllDeadChests();
        }

        launchRepeatingTask();
    }

    public void onDisable() {

        if (fileManager.getConfig2File().exists()) {
            fileManager.saveConfig2();
        }
    }

    private void initializeConfig() {

        // plugin config file
        if (!fileManager.getConfigFile().exists()) {
            saveDefaultConfig();

        } else {
            ArrayList<ChestData> tmp = (ArrayList<ChestData>) fileManager.getConfig2().get("chestData");
            ArrayList<String> tmpExludedWorld = (ArrayList<String>) getConfig().get("ExcludedWorld");

            if (tmp != null)
                chestData = tmp;

            if (tmpExludedWorld != null)
                excludedWorlds = tmpExludedWorld;

            isIndestructible = getConfig().getBoolean("IndestuctibleChest");
            OnlyOwnerCanOpenDeadChest = getConfig().getBoolean("OnlyOwnerCanOpenDeadChest");
            chestDuration = getConfig().getInt("DeadChestDuration");
            maxDeadChestPerPlayer = (int) getConfig().get("maxDeadChestPerPlayer");
            logDeadChestOnConsole = (boolean) getConfig().get("logDeadChestOnConsole");
            requirePermissionToGenerate = (boolean) getConfig().get("RequirePermissionToGenerate");
            permissionRequiredToListOwn = (boolean) getConfig().get("RequirePermissionToListOwn");
            autocleanupOnStart = (boolean) getConfig().get("AutoCleanupOnStart");
            generateDeadChestInCreative = (boolean) getConfig().get("GenerateDeadChestInCreative");
            dropMode = (int) getConfig().get("DropMode");
        }

        // database (chestData.yml
        if (!fileManager.getConfig2File().exists()) {
            fileManager.saveConfig2();
        }

        // locale file for translation
        if (!fileManager.getConfig3File().exists()) {
            fileManager.saveConfig3();
            fileManager.getConfig3().options().header("PLEASE REMOVE ALL EXISTING DEADCHESTS BEFORE EDITING THIS FILE");
            fileManager.getConfig3().createSection("localisation", local);
            fileManager.saveConfig3();
        } else {
            local = (Map<String, Object>) fileManager.getConfig3().getConfigurationSection("localisation").getValues(true);
        }
    }

    private void launchRepeatingTask() {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {

                if (chestData != null && !chestData.isEmpty()) {
                    Date now = new Date();

                    Iterator<ChestData> chestDataIt = chestData.iterator();
                    while (chestDataIt.hasNext()) {
                        ChestData cd = chestDataIt.next();
                        Location as = cd.getHolographicTimer();

                        if (as.getWorld() != null) {

                            ArrayList<Entity> entityList = (ArrayList<Entity>) as.getWorld().getNearbyEntities(as, 1.0, 1.0, 1.0);
                            for (Entity entity : entityList) {
                                if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                                    if (entity.getCustomName() != null && !entity.getCustomName().contains((CharSequence) local.get("loc_owner"))) {
                                        long diff = now.getTime() - (cd.getChestDate().getTime() + chestDuration * 1000);
                                        long diffSeconds = Math.abs(diff / 1000 % 60);
                                        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                                        long diffHours = Math.abs(diff / (60 * 60 * 1000));

                                        if (!cd.isInfinity() && chestDuration != 0)
                                            entity.setCustomName("× " + diffHours + "h " + diffMinutes + "m " + diffSeconds + "s " + local.get("loc_endtimer") + " ×");
                                        else
                                            entity.setCustomName("× " + local.get("loc_infinityChest") + " ×");
                                    }
                                }
                            }

                            if (cd.getChestDate().getTime() + chestDuration * 1000 < now.getTime() && !cd.isInfinity()
                                    && chestDuration != 0) {

                                Location loc = cd.getChestLocation();
                                loc.getWorld().getBlockAt(loc).setType(Material.AIR);

                                cd.removeArmorStand();

                                chestDataIt.remove();
                                isChanged = true;
                            }
                        }
                    }

                    if (isChanged) {
                        fileManager.saveModification();
                        isChanged = false;
                    }
                }
            }
        }, 20, 20);
    }


    static int deadChestPlayerCount(Player p) {

        int count = 0;
        if (p != null) {
            for (ChestData cd : chestData) {
                if (p.getUniqueId().toString().equals(cd.getPlayerUUID()))
                    count++;
            }
        }
        return count;
    }

    static boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack it : inv.getContents()) {
            if (it != null) return false;
        }
        return true;
    }

    public static int cleanAllDeadChests() {

        int cpt = 0;
        if (chestData != null && !chestData.isEmpty()) {

            Iterator<ChestData> chestDataIt = chestData.iterator();
            while (chestDataIt.hasNext()) {

                ChestData cd = chestDataIt.next();

                if (cd.getChestLocation().getWorld() != null) {

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
            fileManager.saveModification();
        }
        return cpt;
    }

    public static ArmorStand generateHologram(Location location, String text, float shiftX, float shiftY, float shiftZ) {
        if (location != null && location.getWorld() != null) {
            Location holoLoc = new Location(location.getWorld(),
                    location.getX() + shiftX,
                    location.getY() + shiftY,
                    location.getZ() + shiftZ);

            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
            armorStand.setInvulnerable(true);
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setCustomName("× " + text + " ×");
            armorStand.setCustomNameVisible(true);

            return armorStand;
        }

        return null;
    }
}
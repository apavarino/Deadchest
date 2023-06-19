package me.crylonz;


import com.sk89q.worldguard.protection.flags.BooleanFlag;
import me.crylonz.utils.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static me.crylonz.DeadChest.*;

public class Utils {

    public static BooleanFlag DEADCHEST_GUEST_FLAG;
    public static BooleanFlag DEADCHEST_OWNER_FLAG;
    public static BooleanFlag DEADCHEST_MEMBER_FLAG;

    static boolean isInventoryEmpty(Inventory inv) {
        for (ItemStack it : inv.getContents()) {
            if (it != null) return false;
        }
        return true;
    }

    static void generateLog(String message) {
        File log = new File("plugins/DeadChest/deadchest.log");
        try {
            log.createNewFile();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String finalMsg = "[" + formatter.format(date) + "] " + message + "\n";
            Files.write(Paths.get("plugins/DeadChest/deadchest.log"), finalMsg.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            DeadChest.log.warning("Can't write log for Deadchest : " + e);
        }
    }

    public static Location getFreeBlockAroundThisPlace(World world, Location loc) {
        Location newLoc = loc.clone();
        if (world.getBlockAt(loc).isEmpty()) {
            return newLoc;
        }

        if (world.getBlockAt(newLoc.clone().add(1, 0, 0)).isEmpty()) {
            return newLoc.add(1, 0, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(-1, 0, 0)).isEmpty()) {
            return newLoc.add(-1, 0, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(0, 1, 0)).isEmpty()) {
            return newLoc.add(0, 1, 0);
        }

        if (world.getBlockAt(newLoc.clone().add(0, -1, 0)).isEmpty()) {
            return newLoc.add(0, -1, 0);
        }

        return null;

    }

    public static boolean worldGuardCheck(Player p) {
        if (wgsdc != null) {
            return wgsdc.worldGuardChecker(p);
        }
        return true;
    }

    public static boolean isBefore1_18() {
        return Bukkit.getVersion().contains("1.17") || isBefore1_17();
    }

    public static boolean isBefore1_17() {
        return Bukkit.getVersion().contains("1.16") || isBefore1_16();
    }

    public static boolean isBefore1_16() {
        return (Bukkit.getVersion().contains("1.15")
                || Bukkit.getVersion().contains("1.14")
                || Bukkit.getVersion().contains("1.13")
                || Bukkit.getVersion().contains("1.12"));
    }

    public static boolean isGraveBlock(Material material) {
        return graveBlocks.contains(material);
    }

    public static boolean isHelmet(ItemStack i) {
        final Material[] helmetList = {Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.LEATHER_HELMET,
                Material.DIAMOND_HELMET, Material.CHAINMAIL_HELMET, Material.TURTLE_HELMET};
        return isGear(i, helmetList, Material.NETHERITE_HELMET);
    }

    public static boolean isLeggings(ItemStack i) {
        final Material[] leggingList = {Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS,
                Material.DIAMOND_LEGGINGS, Material.CHAINMAIL_LEGGINGS};
        return isGear(i, leggingList, Material.NETHERITE_LEGGINGS);
    }

    public static boolean isChestplate(ItemStack i) {
        final Material[] chestplateList = {Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.ELYTRA};
        return isGear(i, chestplateList, Material.NETHERITE_CHESTPLATE);
    }

    public static boolean isBoots(ItemStack i) {
        final Material[] bootList = {Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS,
                Material.DIAMOND_BOOTS, Material.CHAINMAIL_BOOTS};
        return isGear(i, bootList, Material.NETHERITE_BOOTS);
    }

    public static boolean isGear(ItemStack i, Material[] gearList, Material netheriteGear) {
        final boolean isStandardGear = Arrays.asList(gearList).contains(i.getType());
        final boolean isNetheriteGear = isNetherite(i, netheriteGear);
        final boolean hasCurseOfBinding = i.getEnchantments().containsKey(Enchantment.BINDING_CURSE);

        return (isStandardGear || isNetheriteGear) && !hasCurseOfBinding;
    }

    public static boolean isNetherite(ItemStack i, Material netheriteGear) {
        return !isBefore1_16() && i.getType() == netheriteGear;
    }

    public static int computeMinHeight() {
        if (isBefore1_18()) {
            return 1;
        } else {
            return -64;
        }
    }

    public static void computeChestType(Block b, Player p) {
        switch (config.getInt(ConfigKey.DROP_BLOCK)) {
            case 2:
                b.setType(Material.PLAYER_HEAD);
                b.setMetadata("deadchest", new FixedMetadataValue(plugin, true));
                BlockState state = b.getState();
                Skull skull = (Skull) state;
                if (p != null) {
                    skull.setOwningPlayer(p);
                }
                skull.update();
                break;
            case 3:
                b.setType(Material.BARREL);
                break;
            case 4:
                b.setType(Material.SHULKER_BOX);
                break;
            case 5:
                b.setType(Material.ENDER_CHEST);
                break;
            default:
                b.setType(Material.CHEST);
        }
    }

    public static int computeXpToStore(PlayerDeathEvent entity) {
        if (config.getBoolean(ConfigKey.STORE_XP)) {
            // This does give the player 7 1/2 levels on first death above level 8
            // if player dies again with 7 1/2 levels, player drops to 4 levels and so on.
            // this is normal minecraft behaviour
            return entity.getDroppedExp();
        }
        return 0;
    }

}


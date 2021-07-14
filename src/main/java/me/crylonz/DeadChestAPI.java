package me.crylonz;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.crylonz.DeadChest.chestData;

public class DeadChestAPI {

    public static List<ChestData> getChests(Player player) {

        List<ChestData> chestData = new ArrayList<>();
        DeadChest.chestData.forEach(chest -> {
            if (chest.getPlayerName().equalsIgnoreCase(player.getName())) {
                chestData.add(new ChestData(chest));
            }
        });
        return chestData;
    }

    public static boolean giveBackChest(Player player, ChestData chest) {

        if (player.isOnline()) {
            for (ItemStack i : chest.getInventory()) {
                if (i != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), i);
                }
            }

            World world = Bukkit.getWorld(chest.getWorldName());

            if (world != null) {
                world.getBlockAt(chest.getChestLocation()).setType(Material.AIR);
                chest.removeArmorStand();
                chestData.remove(chest);
                return true;
            }
        }
        return false;
    }
}

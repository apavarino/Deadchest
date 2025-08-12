package me.crylonz.deadchest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.crylonz.deadchest.DeadChestLoader.chestData;

public class DeadChestAPI {

    /**
     * Get a list of chest for the given player
     *
     * @param player
     * @return List<ChestData>
     */
    public static List<ChestData> getChests(Player player) {

        List<ChestData> chestData = new ArrayList<>();
        DeadChestLoader.chestData.forEach(chest -> {
            if (chest.getPlayerName().equalsIgnoreCase(player.getName())) {
                chestData.add(new ChestData(chest));
            }
        });
        return chestData;
    }

    /**
     * Give back the last Deadchest of a player. The player need to be online
     *
     * @param player
     * @param chest
     * @return true if chest is returned to his owner
     */
    public static boolean giveBackChest(Player player, ChestData chest) {

        if (player.isOnline()) {
            for (ItemStack i : chest.getInventory()) {
                if (i != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), i);
                }
            }
            return removeChest(chest);
        }
        return false;
    }

    /**
     * Remove a player chest
     *
     * @param chest
     * @return true is the chest is correctly removed
     */
    public static boolean removeChest(ChestData chest) {
        World world = Bukkit.getWorld(chest.getWorldName());

        if (world != null && chestData.contains(chest)) {
            world.getBlockAt(chest.getChestLocation()).setType(Material.AIR);
            chest.removeArmorStand();
            chestData.remove(chest);
            return true;
        }
        return false;
    }

}

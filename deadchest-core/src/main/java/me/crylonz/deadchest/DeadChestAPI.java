package me.crylonz.deadchest;

import me.crylonz.deadchest.cache.DeadChestCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DeadChestAPI {

    /**
     * Get a list of chest for the given player
     *
     * @param player
     * @return List<ChestData>
     */
    public static List<ChestData> getChests(Player player) {

        List<ChestData> chestDataList = new ArrayList<>();
        DeadChestLoader.getChestDataCache().getPlayerLinkedDeadChestData(player, chestData ->
                chestDataList.add(new ChestData(chestData))
        );
        return chestDataList;
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
        final DeadChestCache chestData = DeadChestLoader.getChestDataCache();

        if (world != null && chestData.getChestData(chest.getChestLocation()) != null) {
            world.getBlockAt(chest.getChestLocation()).setType(Material.AIR);

            chestData.removeChestData(chest);
            return true;
        }
        return false;
    }

}

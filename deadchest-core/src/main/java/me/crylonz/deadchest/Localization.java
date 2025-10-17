package me.crylonz.deadchest;

import java.util.HashMap;
import java.util.Map;


public class Localization {

    public static Map<String, Object> local = new HashMap<>();

    public Localization() {
        local.put("loc_prefix", "[DeadChest] ");
        local.put("holo_owner", "Deadchest of §6%player%");
        local.put("holo_timer", "§f%hours%§7h §f%min%§7m §f%sec%§7s left");
        local.put("holo_loading", "Loading...");
        local.put("loc_not_owner", "§cThis is not your Deadchest !");
        local.put("loc_infinityChest", "Infinity chest");
        local.put("loc_endtimer", "left");
        local.put("loc_reload", "§6Reload successfull..");
        local.put("loc_noperm", "§cYou need permission");
        local.put("loc_nodc", "§aYou don't have any deadchest");
        local.put("loc_nodcs", "§aThere is currently no deadchest");
        local.put("loc_dclistall", "§aList of all dead chests");
        local.put("loc_dclistown", "§aList of your dead chests");
        local.put("loc_doubleDC", "§cYou can't put a chest next to a Deadchest");
        local.put("loc_maxHeight", "§cYou are dead above the maximum height.");
        local.put("loc_noDCG", "§cNo deadchest generated.");
        local.put("loc_givebackInfo", "§cThis player is offline or don't have any active deadchest");
        local.put("loc_dcgbsuccess", "§aThe oldest deadchest content of this player returned to them");
        local.put("loc_gbplayer", "§aYou have retrieved the content of your deadchest");
        local.put("loc_dcgbsuccess_overflow", "§eThe player's inventory was full. Remaining items were dropped around them");
        local.put("loc_gbplayer_overflow", "§eYour inventory was full. Some items have been dropped on the ground around you");
        local.put("loc_chestPos", "§6Your deadchest is at");
        local.put("loc_noPermsToGet", "§cYou are not allowed to open this chest");
    }

    public String get(String key) {
        return local.get(key).toString();
    }

    public Map<String, Object> get() {
        return local;
    }

    public void set(Map<String, Object> local) {
        Localization.local = local;
    }

    public String replacePlayer(String localisation, String playerName) {
        return localisation.replace("%player%", playerName);
    }

    public String replaceTimer(String localisation, long hours, long min, long sec) {
        localisation = localisation.replace("%hours%", Long.toString(hours));
        localisation = localisation.replace("%min%", Long.toString(min));
        localisation = localisation.replace("%sec%", Long.toString(sec));
        return localisation;
    }
}

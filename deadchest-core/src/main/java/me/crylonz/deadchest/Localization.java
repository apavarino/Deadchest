package me.crylonz.deadchest;

import java.util.HashMap;
import java.util.Map;


public class Localization {

    public static Map<String, Object> local = new HashMap<>();

    public Localization() {
        local.put("loc_prefix", "[DeadChest] ");
        local.put("holo_owner", "§6%player%§f's grave");
        local.put("holo_timer", "§f%hours%§7h §f%min%§7m §f%sec%§7s left");
        local.put("holo_loading", "Loading...");
        local.put("loc_not_owner", "§cThis is not your grave!");
        local.put("loc_infinityChest", "Never expires");
        local.put("loc_endtimer", "left");
        local.put("loc_reload", "§6Configuration reloaded successfully.");
        local.put("loc_noperm", "§cYou do not have permission to do that.");
        local.put("loc_nodc", "§aYou have no graves");
        local.put("loc_nodcs", "§aThere are no graves");
        local.put("loc_dclistall", "§aShowing all active graves:");
        local.put("loc_dclistown", "§aYour active graves:");
        local.put("loc_doubleDC", "§cYou can't put a chest next to a grave!");
        local.put("loc_maxHeight", "§cCould not generate a grave; you died above the world's maximum height.");
        local.put("loc_noDCG", "§cNo grave generated.");
        local.put("loc_givebackInfo", "§cThat player is either offline or has no active graves.");
        local.put("loc_dcgbsuccess", "§aThe player's oldest grave has been retrieved.");
        local.put("loc_gbplayer", "§aGrave retrieved!");
        local.put("loc_chestPos", "§6Your grave is at");
        local.put("loc_noPermsToGet", "§cYou do not have permission to open this grave.");
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

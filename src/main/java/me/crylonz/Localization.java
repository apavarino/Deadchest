package me.crylonz;

import java.util.HashMap;
import java.util.Map;

public class Localization {

    public static Map<String, Object> local = new HashMap<>();

    public Localization() {
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
        local.put("loc_chestPos", "Your deadchest is at");
    }

    public Map<String, Object> get() {
        return local;
    }

    public void set(Map<String, Object> local) {
        Localization.local = local;
    }
}

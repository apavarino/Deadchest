package me.crylonz.deadchest

open class Localization {

    private var local: MutableMap<String, Any> = HashMap()

    fun Localization() {
        local["loc_prefix"] = "[DeadChest] "
        local["holo_owner"] = "Deadchest of §6%player%"
        local["holo_timer"] = "§f%hours%§7h §f%min%§7m §f%sec%§7s left"
        local["holo_loading"] = "Loading..."
        local["loc_not_owner"] = "§cThis is not your Deadchest !"
        local["loc_infinityChest"] = "Infinity chest"
        local["loc_endtimer"] = "left"
        local["loc_reload"] = "§6Reload successfull.."
        local["loc_noperm"] = "§cYou need permission"
        local["loc_nodc"] = "§aYou don't have any deadchest"
        local["loc_nodcs"] = "§aThere is currently no deadchest"
        local["loc_dclistall"] = "§aList of all dead chests"
        local["loc_dclistown"] = "§aList of your dead chests"
        local["loc_doubleDC"] = "§cYou can't put a chest next to a Deadchest"
        local["loc_maxHeight"] = "§cYou are dead above the maximum height."
        local["loc_noDCG"] = "§cNo deadchest generated."
        local["loc_givebackInfo"] = "§cThis player is offline or don't have any active deadchest"
        local["loc_dcgbsuccess"] = "§aThe oldest deadchest content of this player returned to him"
        local["loc_gbplayer"] = "§aYou have retrieved the content of your deadchest"
        local["loc_chestPos"] = "§6Your deadchest is at"
        local["loc_noPermsToGet"] = "§cYou are not allowed to open this chest"
    }

    fun set(localToSet: MutableMap<String, Any>) {
        local = localToSet
    }

    fun get(): MutableMap<String, Any> = local

    fun get(key: String): String = local[key].toString()

    fun replacePlayer(localisation: String, playerName: String): String {
        return localisation.replace("%player%", playerName)
    }

    fun replaceTimer(loc: String, hours: Long, min: Long, sec: Long): String {
        var localisation = loc
        localisation = localisation.replace("%hours%", hours.toString())
        localisation = localisation.replace("%min%", min.toString())
        localisation = localisation.replace("%sec%", sec.toString())
        return localisation
    }
}
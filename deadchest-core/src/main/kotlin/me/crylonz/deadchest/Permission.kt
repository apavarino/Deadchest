package me.crylonz.deadchest

enum class Permission(val label: String) {
    CHESTPASS("deadchest.chestPass"),
    GET("deadchest.get"),
    GENERATE("deadchest.generate"),
    GIVEBACK("deadchest.giveback"),
    INFINITY_CHEST("deadchest.infinityChest"),
    REMOVE_OWN("deadchest.remove.own"),
    REMOVE_OTHER("deadchest.remove.other"),
    LIST_OWN("deadchest.list.own"),
    LIST_OTHER("deadchest.list.other"),
    ADMIN("deadchest.admin")
}

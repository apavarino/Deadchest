package me.crylonz;

public enum Permission {
    CHESTPASS("deadchest.chestPass"),
    GET("deadchest.get"),
    GENERATE("deadchest.generate"),
    GIVEBACK("deadchest.giveback"),
    INFINITY_CHEST("deadchest.infityChest"),
    REMOVE_OWN("deadchest.remove.own"),
    REMOVE_OTHER("deadchest.remove.other"),
    LIST_OWN("deadchest.list.own"),
    LIST_OTHER("deadchest.list.other"),
    ADMIN("deadchest.admin");

    public final String label;
    public static final Permission[] REMOVE = {REMOVE_OWN, REMOVE_OTHER};
    public static final Permission[] LIST = {LIST_OWN, LIST_OTHER};

    private Permission(String label) {
        this.label = label;
    }
}

package me.crylonz.deadchest.scheduler;

import org.bukkit.plugin.Plugin;

final class FoliaDetection {
    private static final String FOLIA_SERVER_CLASS = "io.papermc.paper.threadedregions.RegionizedServer";

    private FoliaDetection() {
    }

    static boolean isFolia(Plugin plugin) {
        if (plugin == null || plugin.getServer() == null) {
            return false;
        }

        try {
            Class.forName(FOLIA_SERVER_CLASS);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}

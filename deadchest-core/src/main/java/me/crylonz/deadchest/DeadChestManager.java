package me.crylonz.deadchest;

import me.crylonz.deadchest.db.InMemoryChestStore;
import me.crylonz.deadchest.utils.ConfigKey;
import me.crylonz.deadchest.utils.EffectAnimationStyle;
import me.crylonz.deadchest.utils.ExpiredActionType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static me.crylonz.deadchest.DeadChestLoader.*;
import static me.crylonz.deadchest.utils.Utils.*;

public class DeadChestManager {
    private static final Map<EffectAnimationStyle, Particle> styleParticles = new ConcurrentHashMap<>();
    private static final Set<EffectAnimationStyle> unresolvedParticleStyles = EnumSet.noneOf(EffectAnimationStyle.class);

    /**
     * Remove all active deadchests
     *
     * @return number of deadchests removed
     */
    public static int cleanAllDeadChests() {

        int chestDataRemoved = 0;
        final InMemoryChestStore inMemoryChestStore = DeadChestLoader.getChestDataCache();
        final Map<Location, ChestData> chestDataList = inMemoryChestStore.getAllChestData();

        if (chestDataList != null && !chestDataList.isEmpty()) {
            final List<ChestData> chestDataRemove = new ArrayList<>();
            for (final ChestData chestData : chestDataList.values()) {
                if (chestData.getChestLocation().getWorld() != null) {

                    Location loc = chestData.getChestLocation();
                    loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                    chestData.removeArmorStand();
                    chestDataRemove.add(chestData);
                    chestDataRemoved++;
                }
            }
            inMemoryChestStore.removeChestDataList(chestDataRemove);
        }
        return chestDataRemoved;
    }

    /**
     * Generate a hologram at the given position
     *
     * @param location position to place
     * @param text     text to display
     * @param shiftX   x shifting
     * @param shiftY   y shifting
     * @param shiftZ   z shifting
     * @return the generated armorstand
     */
    public static ArmorStand generateHologram(Location location, String text, float shiftX, float shiftY, float shiftZ, boolean isTimer) {
        if (location != null && location.getWorld() != null) {
            Location holoLoc = new Location(location.getWorld(),
                    location.getX() + shiftX,
                    location.getY() + shiftY + 2,
                    location.getZ() + shiftZ);

            ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
            armorStand.setInvulnerable(true);
            armorStand.setSmall(true);
            armorStand.setGravity(false);
            armorStand.setCanPickupItems(false);
            armorStand.setVisible(false);
            armorStand.setCollidable(false);
            armorStand.setMetadata("deadchest", new FixedMetadataValue(plugin, isTimer));
            armorStand.setCustomName(text);
            armorStand.setSilent(true);
            armorStand.setMarker(true);
            armorStand.setCustomNameVisible(true);

            return armorStand;
        }
        return null;
    }

    /**
     * get the number of deadchest for a player
     *
     * @param p player
     * @return number of deadchests
     */
    public static int playerDeadChestAmount(Player p) {
        int count = 0;
        if (p != null) {
            count = DeadChestLoader.getChestDataCache().getPlayerChestAmount(p);
        }
        return count;
    }

    /**
     * Regeneration of metaData for holos
     */
    static void reloadMetaData() {

        final Map<Location, ChestData> chestDataList = getChestDataCache().getAllChestData();
        for (ChestData cdata : chestDataList.values()) {
            World world = cdata.getChestLocation().getWorld();

            if (world != null) {
                Collection<Entity> nearbyEntities =
                        world.getNearbyEntities(cdata.getHolographicTimer(), 1, 1, 1);

                for (Entity ne : nearbyEntities) {
                    if (ne.getUniqueId().equals(cdata.getHolographicOwnerId())) {
                        ne.setMetadata("deadchest", new FixedMetadataValue(DeadChestLoader.plugin, false));
                    } else if (ne.getUniqueId().equals(cdata.getHolographicTimerId())) {
                        ne.setMetadata("deadchest", new FixedMetadataValue(DeadChestLoader.plugin, true));
                    }
                }
            }
        }
    }

    public static boolean replaceDeadChestIfItDisappears(ChestData chestData) {
        World world = chestData.getChestLocation().getWorld();

        if (world == null) {
            return false;
        }

        Collection<Entity> entityList = world.getNearbyEntities(chestData.getHolographicTimer(), 1.0, 1.0, 1.0);
        boolean isLinkedToDeadchest = entityList.stream().anyMatch(entity ->
                entity.getUniqueId().equals(chestData.getHolographicOwnerId()) ||
                        entity.getUniqueId().equals(chestData.getHolographicTimerId())
        );

        boolean needToUpdateData = false;

        Block block = world.getBlockAt(chestData.getChestLocation());
        if (!isGraveBlock(block.getType())) {
            generateDeadChest(block, Bukkit.getPlayer(chestData.getPlayerUUID()));
            generateLog("Deadchest of [" + chestData.getPlayerName() + "] was corrupted. Deadchest fixed!");
            needToUpdateData = true;
        }

        if (!isLinkedToDeadchest) {
            for (Entity entity : entityList) {
                if (entity instanceof ArmorStand) {
                    entity.remove();
                }
            }

            ArmorStand[] holos = createHolograms(block, chestData.getPlayerName());
            chestData.setHolographicTimerId(holos[0].getUniqueId());
            chestData.setHolographicOwnerId(holos[1].getUniqueId());
            generateLog("Hologram Deadchest of [" + chestData.getPlayerName() + "] was corrupted. Hologram fixed!");
            needToUpdateData = true;
        }

        return needToUpdateData;
    }


    public static ExpiredActionType handleExpirateDeadChest(ChestData chestData, Date date) {
        if (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L < date.getTime() && !chestData.isInfinity()
                && config.getInt(ConfigKey.DEADCHEST_DURATION) != 0) {

            Location loc = chestData.getChestLocation();

            if (loc.getWorld() != null) {
                if (!chestData.isRemovedBlock()) {
                    chestData.setRemovedBlock(true);
                    loc.getWorld().getBlockAt(loc).setType(Material.AIR);
                }
                if (config.getBoolean(ConfigKey.ITEMS_DROPPED_AFTER_TIMEOUT)) {
                    for (ItemStack itemStack : chestData.getInventory()) {
                        if (itemStack != null) {
                            loc.getWorld().dropItemNaturally(loc, itemStack);
                        }
                    }
                    chestData.cleanInventory();
                }
            }
            if (chestData.removeArmorStand())
                return ExpiredActionType.REMOVED_ARMORSTAND;
            return ExpiredActionType.FAIL_REMOVE_ARMORSTAND;
        }
        return ExpiredActionType.NOT_EXPIRED;
    }

    public static void updateTimer(ChestData chestData, Date date) {
        Location chestTimer = chestData.getHolographicTimer();

        if (chestTimer.getWorld() != null && chestData.isChunkLoaded()) {

            ArrayList<Entity> entityList = (ArrayList<Entity>) chestTimer.getWorld().getNearbyEntities(chestTimer, 1.0, 1.0, 1.0);
            for (Entity entity : entityList) {
                if (entity.getType().equals(EntityType.ARMOR_STAND)) {
                    if (!entity.hasMetadata("deadchest")) {
                        reloadMetaData();
                    }
                    if (entity.getMetadata("deadchest").size() > 0 && entity.getMetadata("deadchest").get(0).asBoolean()) {
                        long diff = date.getTime() - (chestData.getChestDate().getTime() + config.getInt(ConfigKey.DEADCHEST_DURATION) * 1000L);
                        long diffSeconds = Math.abs(diff / 1000 % 60);
                        long diffMinutes = Math.abs(diff / (60 * 1000) % 60);
                        long diffHours = Math.abs(diff / (60 * 60 * 1000));

                        if (!chestData.isInfinity() && config.getInt(ConfigKey.DEADCHEST_DURATION) != 0) {
                            entity.setCustomName(local.format("hologram.timer", diffHours, diffMinutes, diffSeconds));
                        } else {
                            entity.setCustomName(local.get("chest.infinity"));
                        }
                    }
                }
            }
        }
    }

    public static void animateSoulOrbit(ChestData chestData, long nowMs) {
        if (chestData.isRemovedBlock()) {
            return;
        }

        final Location chestLocation = chestData.getChestLocation();
        final World world = chestLocation.getWorld();
        if (world == null || !isGraveBlock(world.getBlockAt(chestLocation).getType())) {
            return;
        }

        final EffectAnimationStyle style = DeadChestLoader.getConfiguredAnimationStyle();
        final Particle soulParticle = resolveStyleParticle(style);
        if (soulParticle == null) {
            return;
        }

        final double radius = clamp(config.getDouble(ConfigKey.EFFECT_ANIMATION_RADIUS), 0.25D, 2.0D);
        final double speed = clamp(config.getDouble(ConfigKey.EFFECT_ANIMATION_SPEED), 0.1D, 8.0D);

        final double centerX = chestLocation.getX() + 0.5D;
        final double minY = chestLocation.getY() + 0.06D;
        final double maxY = chestLocation.getY() + 1.58D;
        final double centerZ = chestLocation.getZ() + 0.5D;

        final double basePhase = (nowMs / 1000.0D) * speed
                + (Math.abs(chestData.getPlayerUUID().hashCode()) % 360) * Math.PI / 180.0D;

        // Spawn two opposite "souls" to create an orbit effect around the chest.
        for (int i = 0; i < 2; i++) {
            final double angle = basePhase + (i * Math.PI);
            final double x = centerX + (Math.cos(angle) * radius);
            final double oscillation = (Math.sin(basePhase * 1.15D + i) + 1.0D) * 0.5D;
            final double y = minY + (maxY - minY) * oscillation;
            final double z = centerZ + (Math.sin(angle) * radius);

            world.spawnParticle(soulParticle, x, y, z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
        }

    }

    private static Particle resolveStyleParticle(EffectAnimationStyle style) {
        if (unresolvedParticleStyles.contains(style)) {
            return null;
        }
        if (styleParticles.containsKey(style)) {
            return styleParticles.get(style);
        }

        for (String particleName : style.particleCandidates()) {
            Particle resolved = valueOfOrNull(Particle.class, particleName);
            if (resolved != null) {
                styleParticles.put(style, resolved);
                return resolved;
            }
        }
        unresolvedParticleStyles.add(style);
        return null;
    }

    private static <T extends Enum<T>> T valueOfOrNull(Class<T> type, String name) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

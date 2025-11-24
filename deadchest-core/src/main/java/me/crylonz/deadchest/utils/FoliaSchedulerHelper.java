package me.crylonz.deadchest.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for Folia regionalized scheduling
 * Provides methods to schedule tasks in the correct region
 */
public class FoliaSchedulerHelper {

    /**
     * Run a task on the region that owns the given location
     * @param plugin The plugin
     * @param location The location
     * @param task The task to run
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Bukkit.getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
    }

    /**
     * Run a delayed task on the region that owns the given location
     * @param plugin The plugin
     * @param location The location
     * @param task The task to run
     * @param delayTicks Delay in ticks (20 ticks = 1 second)
     */
    public static void runAtLocationDelayed(Plugin plugin, Location location, Runnable task, long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delayTicks);
    }

    /**
     * Run a repeating task on the region that owns the given location
     * @param plugin The plugin
     * @param location The location
     * @param task The task to run
     * @param initialDelayTicks Initial delay in ticks
     * @param periodTicks Period in ticks (how often to repeat)
     */
    public static void runAtLocationRepeating(Plugin plugin, Location location, Runnable task, long initialDelayTicks, long periodTicks) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, scheduledTask -> task.run(), initialDelayTicks, periodTicks);
    }

    /**
     * Run a task on the entity's scheduler (follows the entity)
     * @param plugin The plugin
     * @param entity The entity
     * @param task The task to run
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }

    /**
     * Run a delayed task on the entity's scheduler
     * @param plugin The plugin
     * @param entity The entity
     * @param task The task to run
     * @param delayTicks Delay in ticks
     */
    public static void runForEntityDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, delayTicks);
    }

    /**
     * Run an async task (not tied to any region)
     * Use this for database operations, file I/O, etc.
     * @param plugin The plugin
     * @param task The task to run
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    /**
     * Run a delayed async task
     * @param plugin The plugin
     * @param task The task to run
     * @param delayMillis Delay in milliseconds
     */
    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delayMillis) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a repeating async task
     * @param plugin The plugin
     * @param task The task to run
     * @param initialDelayMillis Initial delay in milliseconds
     * @param periodMillis Period in milliseconds
     */
    public static void runAsyncRepeating(Plugin plugin, Runnable task, long initialDelayMillis, long periodMillis) {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a task on the global region scheduler
     * This is for tasks that need to be executed but don't have a specific location
     * @param plugin The plugin
     * @param task The task to run
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    /**
     * Run a repeating task on the global region scheduler
     * @param plugin The plugin
     * @param task The task to run
     * @param initialDelayTicks Initial delay in ticks
     * @param periodTicks Period in ticks
     */
    public static void runGlobalRepeating(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelayTicks, periodTicks);
    }
}

package me.crylonz.deadchest.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Compatibility layer between the classic Bukkit scheduler model and Folia's
 * region/entity schedulers.
 * <p>
 * This adapter keeps the historic classic behavior on Bukkit, Spigot and Paper,
 * and only switches to region-safe dispatch when the plugin is running on an
 * actual Folia server.
 * <p>
 * There are two execution families exposed by this class:
 * <ul>
 *     <li>{@code run...}: schedule work through the platform scheduler.
 *     On classic servers this means "next tick on main thread". On Folia this
 *     dispatches through the matching global, region or entity scheduler.</li>
 *     <li>{@code execute...}: preserve classic synchronous semantics when
 *     possible. On Bukkit/Spigot/Paper the runnable executes immediately in the
 *     current thread. On Folia the runnable is routed through the appropriate
 *     scheduler because direct cross-region access is unsafe.</li>
 * </ul>
 */
public final class SchedulerAdapter {
    private final SchedulerBackend backend;

    /**
     * Creates a scheduler bridge for the given plugin.
     *
     * @param plugin plugin owning scheduled tasks
     */
    public SchedulerAdapter(Plugin plugin) {
        this.backend = createBackend(plugin);
    }

    private SchedulerBackend createBackend(Plugin plugin) {
        if (plugin == null || plugin.getServer() == null) {
            return new ClassicSchedulerBackend(plugin);
        }

        try {
            return FoliaDetection.isFolia(plugin) ? new FoliaSchedulerBackend(plugin) : new ClassicSchedulerBackend(plugin);
        } catch (NoClassDefFoundError | NoSuchMethodError | ExceptionInInitializerError ignored) {
            return new ClassicSchedulerBackend(plugin);
        }
    }

    /**
     * Indicates whether the current runtime requires Folia-style region-safe
     * dispatch.
     *
     * @return {@code true} on Folia runtimes
     */
    public boolean supportsRegionSchedulers() {
        return this.backend.isFoliaLikeRuntime();
    }

    /**
     * Indicates whether the server should be treated as Folia-like for scheduling
     * purposes.
     *
     * @return {@code true} on Folia runtimes
     */
    public boolean isFoliaLikeRuntime() {
        return this.backend.isFoliaLikeRuntime();
    }

    /**
     * Schedules a task on the global scheduler.
     *
     * @param task task to run
     */
    public void runGlobal(Runnable task) {
        this.backend.runGlobal(task);
    }

    /**
     * Executes a task with classic synchronous semantics when possible.
     *
     * @param task task to execute
     */
    public void executeGlobal(Runnable task) {
        this.backend.executeGlobal(task);
    }

    /**
     * Schedules a task for the location owner thread.
     *
     * @param location target location
     * @param task     task to run
     */
    public void runAtLocation(Location location, Runnable task) {
        this.backend.runAtLocation(location, task);
    }

    /**
     * Executes a location-bound task with classic synchronous semantics when
     * possible.
     *
     * @param location target location
     * @param task     task to execute
     */
    public void executeAtLocation(Location location, Runnable task) {
        this.backend.executeAtLocation(location, task);
    }

    /**
     * Schedules a task for an entity owner thread.
     *
     * @param entity target entity
     * @param task   task to run
     */
    public void runForEntity(Entity entity, Runnable task) {
        this.backend.runForEntity(entity, task);
    }

    /**
     * Executes an entity-bound task with classic synchronous semantics when
     * possible.
     *
     * @param entity target entity
     * @param task   task to execute
     */
    public void executeForEntity(Entity entity, Runnable task) {
        this.backend.executeForEntity(entity, task);
    }

    /**
     * Creates a repeating global task.
     *
     * @param task        task body
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return opaque cancellable task handle
     */
    public SchedulerTaskHandle runGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        return this.backend.runGlobalRepeating(task, delayTicks, periodTicks);
    }

    /**
     * Cancels a task previously returned by {@link #runGlobalRepeating(Runnable, long, long)}.
     *
     * @param taskHandle opaque task handle, may be {@code null}
     */
    public void cancelTask(SchedulerTaskHandle taskHandle) {
        if (taskHandle != null) {
            taskHandle.cancel();
        }
    }
}

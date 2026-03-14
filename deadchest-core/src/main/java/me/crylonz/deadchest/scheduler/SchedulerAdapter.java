package me.crylonz.deadchest.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Compatibility layer between the classic Bukkit scheduler model and Folia's
 * region/entity schedulers.
 * <p>
 * This adapter is intentionally runtime-driven:
 * it detects Folia-like schedulers via reflection so the plugin can still load
 * on Bukkit, Spigot and Paper with a single jar.
 * <p>
 * There are two execution families exposed by this class:
 * <ul>
 *     <li>{@code run...}: schedule work through the platform scheduler.
 *     On classic servers this means "next tick on main thread". On Folia this
 *     means dispatching to the matching global, region or entity scheduler.</li>
 *     <li>{@code execute...}: preserve legacy synchronous semantics when possible.
 *     On Bukkit/Spigot/Paper the runnable executes immediately in the current
 *     thread. On Folia the runnable is routed through the appropriate scheduler
 *     because direct cross-region access is unsafe.</li>
 * </ul>
 * This distinction is important for code paths that historically ran
 * synchronously on classic servers and must remain behaviorally equivalent there,
 * while still being safe on Folia.
 */
public final class SchedulerAdapter {
    private static final String METHOD_GET_GLOBAL_REGION_SCHEDULER = "getGlobalRegionScheduler";
    private static final String METHOD_GET_REGION_SCHEDULER = "getRegionScheduler";
    private static final String METHOD_GET_ENTITY_SCHEDULER = "getScheduler";
    private static final String METHOD_RUN = "run";
    private static final String METHOD_RUN_AT_FIXED_RATE = "runAtFixedRate";
    private static final String METHOD_EXECUTE = "execute";
    private static final String METHOD_CANCEL = "cancel";

    private final Plugin plugin;
    private final Object globalRegionScheduler;
    private final Object regionScheduler;
    private final Method globalRunMethod;
    private final Method globalRunAtFixedRateMethod;
    private final Method regionExecuteMethod;
    private final Method entityGetSchedulerMethod;
    private final Method entityRunMethod;

    /**
     * Creates a scheduler bridge for the given plugin.
     * <p>
     * If the runtime does not expose Folia schedulers, or if the provided plugin
     * cannot expose a server instance in tests/mocks, the adapter transparently
     * falls back to classic behavior.
     *
     * @param plugin plugin owning scheduled tasks
     */
    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;

        if (plugin == null) {
            this.globalRegionScheduler = null;
            this.regionScheduler = null;
            this.globalRunMethod = null;
            this.globalRunAtFixedRateMethod = null;
            this.regionExecuteMethod = null;
            this.entityGetSchedulerMethod = null;
            this.entityRunMethod = null;
            return;
        }

        Object resolvedGlobalScheduler;
        Object resolvedRegionScheduler;
        Method resolvedGlobalRunMethod;
        Method resolvedGlobalRunAtFixedRateMethod;
        Method resolvedRegionExecuteMethod;
        Method resolvedEntityGetSchedulerMethod;
        Method resolvedEntityRunMethod;

        try {
            Server server = plugin.getServer();
            if (server == null) {
                resolvedGlobalScheduler = null;
                resolvedRegionScheduler = null;
                resolvedGlobalRunMethod = null;
                resolvedGlobalRunAtFixedRateMethod = null;
                resolvedRegionExecuteMethod = null;
                resolvedEntityGetSchedulerMethod = null;
                resolvedEntityRunMethod = null;
            } else {
                Method getGlobalRegionScheduler = server.getClass().getMethod(METHOD_GET_GLOBAL_REGION_SCHEDULER);
                resolvedGlobalScheduler = getGlobalRegionScheduler.invoke(server);
                resolvedGlobalRunMethod = resolvedGlobalScheduler.getClass().getMethod(METHOD_RUN, Plugin.class, Consumer.class);
                resolvedGlobalRunAtFixedRateMethod = resolvedGlobalScheduler.getClass().getMethod(
                        METHOD_RUN_AT_FIXED_RATE,
                        Plugin.class,
                        Consumer.class,
                        long.class,
                        long.class
                );

                Method getRegionScheduler = server.getClass().getMethod(METHOD_GET_REGION_SCHEDULER);
                resolvedRegionScheduler = getRegionScheduler.invoke(server);
                resolvedRegionExecuteMethod = resolvedRegionScheduler.getClass().getMethod(
                        METHOD_EXECUTE,
                        Plugin.class,
                        Location.class,
                        Runnable.class
                );

                resolvedEntityGetSchedulerMethod = Entity.class.getMethod(METHOD_GET_ENTITY_SCHEDULER);
                Class<?> entitySchedulerClass = resolvedEntityGetSchedulerMethod.getReturnType();
                resolvedEntityRunMethod = entitySchedulerClass.getMethod(
                        METHOD_RUN,
                        Plugin.class,
                        Consumer.class,
                        Runnable.class
                );
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            resolvedGlobalScheduler = null;
            resolvedRegionScheduler = null;
            resolvedGlobalRunMethod = null;
            resolvedGlobalRunAtFixedRateMethod = null;
            resolvedRegionExecuteMethod = null;
            resolvedEntityGetSchedulerMethod = null;
            resolvedEntityRunMethod = null;
        }

        this.globalRegionScheduler = resolvedGlobalScheduler;
        this.regionScheduler = resolvedRegionScheduler;
        this.globalRunMethod = resolvedGlobalRunMethod;
        this.globalRunAtFixedRateMethod = resolvedGlobalRunAtFixedRateMethod;
        this.regionExecuteMethod = resolvedRegionExecuteMethod;
        this.entityGetSchedulerMethod = resolvedEntityGetSchedulerMethod;
        this.entityRunMethod = resolvedEntityRunMethod;
    }

    /**
     * Indicates whether the current runtime exposes Folia-style global and region
     * schedulers.
     *
     * @return {@code true} when region-aware schedulers are available
     */
    public boolean supportsRegionSchedulers() {
        return this.plugin != null && this.globalRegionScheduler != null && this.regionScheduler != null;
    }

    /**
     * Indicates whether the server should be treated as Folia-like for scheduling
     * purposes.
     *
     * @return {@code true} when region-safe dispatch is required
     */
    public boolean isFoliaLikeRuntime() {
        return supportsRegionSchedulers();
    }

    /**
     * Schedules a task on the global scheduler.
     * <p>
     * Classic behavior:
     * next tick on the Bukkit main thread.
     * <p>
     * Folia behavior:
     * dispatch through the global region scheduler.
     *
     * @param task task to run
     */
    public void runGlobal(Runnable task) {
        if (this.plugin == null) {
            task.run();
            return;
        }

        if (!supportsRegionSchedulers()) {
            Bukkit.getScheduler().runTask(this.plugin, task);
            return;
        }

        try {
            invokeConsumerMethod(this.globalRegionScheduler, this.globalRunMethod, task);
        } catch (IllegalStateException ex) {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    /**
     * Executes a task with legacy synchronous semantics when possible.
     * <p>
     * On Bukkit/Spigot/Paper this runs immediately.
     * On Folia it falls back to {@link #runGlobal(Runnable)} because the work
     * must still be routed through a scheduler.
     *
     * @param task task to execute
     */
    public void executeGlobal(Runnable task) {
        if (!supportsRegionSchedulers()) {
            task.run();
            return;
        }

        runGlobal(task);
    }

    /**
     * Schedules a task for the region owning the given location.
     * <p>
     * Legacy behavior:
     * next tick on the Bukkit main thread.
     * <p>
     * Folia behavior:
     * dispatch to the region scheduler bound to {@code location}.
     *
     * @param location target location
     * @param task     task to run
     */
    public void runAtLocation(Location location, Runnable task) {
        if (location == null || location.getWorld() == null || !supportsRegionSchedulers()) {
            runGlobal(task);
            return;
        }

        try {
            this.regionExecuteMethod.invoke(this.regionScheduler, this.plugin, location, task);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    /**
     * Executes a task with legacy synchronous semantics when possible for a
     * location-bound operation.
     * <p>
     * On Bukkit/Spigot/Paper this runs immediately.
     * On Folia it is routed to the location region scheduler.
     *
     * @param location target location
     * @param task     task to execute
     */
    public void executeAtLocation(Location location, Runnable task) {
        if (location == null || location.getWorld() == null || !supportsRegionSchedulers()) {
            task.run();
            return;
        }

        runAtLocation(location, task);
    }

    /**
     * Schedules a task for the scheduler associated with an entity.
     * <p>
     * Legacy behavior:
     * next tick on the Bukkit main thread.
     * <p>
     * Folia behavior:
     * dispatch through the entity scheduler.
     *
     * @param entity target entity
     * @param task   task to run
     */
    public void runForEntity(Entity entity, Runnable task) {
        if (entity == null || this.entityGetSchedulerMethod == null || this.entityRunMethod == null) {
            runGlobal(task);
            return;
        }

        try {
            Object entityScheduler = this.entityGetSchedulerMethod.invoke(entity);
            Consumer<Object> consumer = ignored -> task.run();
            this.entityRunMethod.invoke(entityScheduler, this.plugin, consumer, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            runGlobal(task);
        }
    }

    /**
     * Executes a task with legacy synchronous semantics when possible for an
     * entity-bound operation.
     * <p>
     * On Bukkit/Spigot/Paper this runs immediately.
     * On Folia it is routed to the entity scheduler.
     *
     * @param entity target entity
     * @param task   task to execute
     */
    public void executeForEntity(Entity entity, Runnable task) {
        if (entity == null || this.entityGetSchedulerMethod == null || this.entityRunMethod == null || !supportsRegionSchedulers()) {
            task.run();
            return;
        }

        runForEntity(entity, task);
    }

    /**
     * Creates a repeating global task.
     * <p>
     * On legacy runtimes this delegates to {@code BukkitScheduler#runTaskTimer}.
     * On Folia it delegates to the global region scheduler fixed-rate API.
     *
     * @param task        task body
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return opaque platform-specific task handle
     */
    public Object runGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (this.plugin == null) {
            return null;
        }

        if (!supportsRegionSchedulers()) {
            return Bukkit.getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks);
        }

        try {
            return invokeConsumerMethod(this.globalRegionScheduler, this.globalRunAtFixedRateMethod, task, delayTicks, periodTicks);
        } catch (IllegalStateException ex) {
            return Bukkit.getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks);
        }
    }

    /**
     * Cancels a task previously returned by {@link #runGlobalRepeating(Runnable, long, long)}.
     * <p>
     * The provided handle may be either a Bukkit task or a Folia scheduler task.
     * Cancellation is best-effort for unknown task types.
     *
     * @param taskHandle opaque task handle, may be {@code null}
     */
    public void cancelTask(Object taskHandle) {
        if (taskHandle == null) {
            return;
        }

        if (taskHandle instanceof BukkitTask) {
            ((BukkitTask) taskHandle).cancel();
            return;
        }

        try {
            Method cancel = taskHandle.getClass().getMethod(METHOD_CANCEL);
            cancel.setAccessible(true);
            cancel.invoke(taskHandle);
        } catch (NoSuchMethodException ignored) {
            // Best-effort cancellation only.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to cancel scheduled task", e);
        }
    }

    /**
     * Invokes a reflected scheduler method that accepts a plugin and a task
     * consumer, optionally followed by extra arguments such as delay/period.
     *
     * @param scheduler reflected scheduler instance
     * @param method    reflected scheduling method
     * @param task      task to wrap
     * @param extraArgs optional trailing method arguments
     * @return opaque platform-specific task handle
     */
    private Object invokeConsumerMethod(Object scheduler, Method method, Runnable task, Object... extraArgs) {
        try {
            Consumer<Object> consumer = ignored -> task.run();
            Object[] arguments = new Object[2 + extraArgs.length];
            arguments[0] = this.plugin;
            arguments[1] = consumer;
            System.arraycopy(extraArgs, 0, arguments, 2, extraArgs.length);
            return method.invoke(scheduler, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to schedule task", e);
        }
    }
}

package me.crylonz.deadchest.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class ClassicSchedulerBackend implements SchedulerBackend {
    private final Plugin plugin;

    ClassicSchedulerBackend(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFoliaLikeRuntime() {
        return false;
    }

    @Override
    public void runGlobal(Runnable task) {
        if (this.plugin == null) {
            task.run();
            return;
        }
        Bukkit.getScheduler().runTask(this.plugin, task);
    }

    @Override
    public void executeGlobal(Runnable task) {
        task.run();
    }

    @Override
    public void runAtLocation(Location location, Runnable task) {
        runGlobal(task);
    }

    @Override
    public void executeAtLocation(Location location, Runnable task) {
        task.run();
    }

    @Override
    public void runForEntity(Entity entity, Runnable task) {
        runGlobal(task);
    }

    @Override
    public void executeForEntity(Entity entity, Runnable task) {
        task.run();
    }

    @Override
    public SchedulerTaskHandle runGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (this.plugin == null) {
            return () -> {
            };
        }

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(this.plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }
}

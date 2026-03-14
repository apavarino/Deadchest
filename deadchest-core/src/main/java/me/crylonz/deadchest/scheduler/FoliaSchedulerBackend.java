package me.crylonz.deadchest.scheduler;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

final class FoliaSchedulerBackend implements SchedulerBackend {
    private final Plugin plugin;
    private final GlobalRegionScheduler globalRegionScheduler;
    private final RegionScheduler regionScheduler;

    FoliaSchedulerBackend(Plugin plugin) {
        this.plugin = plugin;
        this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
    }

    @Override
    public boolean isFoliaLikeRuntime() {
        return true;
    }

    @Override
    public void runGlobal(Runnable task) {
        if (this.plugin == null) {
            task.run();
            return;
        }

        try {
            this.globalRegionScheduler.run(this.plugin, ignored -> task.run());
        } catch (IllegalStateException ex) {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    @Override
    public void executeGlobal(Runnable task) {
        runGlobal(task);
    }

    @Override
    public void runAtLocation(Location location, Runnable task) {
        if (location == null || location.getWorld() == null) {
            runGlobal(task);
            return;
        }

        try {
            this.regionScheduler.execute(this.plugin, location, task);
        } catch (IllegalStateException ex) {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    @Override
    public void executeAtLocation(Location location, Runnable task) {
        runAtLocation(location, task);
    }

    @Override
    public void runForEntity(Entity entity, Runnable task) {
        if (entity == null) {
            runGlobal(task);
            return;
        }

        EntityScheduler entityScheduler = entity.getScheduler();
        Consumer<ScheduledTask> consumer = ignored -> task.run();

        try {
            entityScheduler.run(this.plugin, consumer, null);
        } catch (IllegalStateException ex) {
            runGlobal(task);
        }
    }

    @Override
    public void executeForEntity(Entity entity, Runnable task) {
        runForEntity(entity, task);
    }

    @Override
    public SchedulerTaskHandle runGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (this.plugin == null) {
            return () -> {
            };
        }

        try {
            ScheduledTask scheduledTask = this.globalRegionScheduler.runAtFixedRate(this.plugin, ignored -> task.run(), delayTicks, periodTicks);
            return scheduledTask::cancel;
        } catch (IllegalStateException ex) {
            return new ClassicSchedulerBackend(this.plugin).runGlobalRepeating(task, delayTicks, periodTicks);
        }
    }
}

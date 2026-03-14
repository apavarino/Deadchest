package me.crylonz.deadchest.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

interface SchedulerBackend {
    boolean isFoliaLikeRuntime();

    void runGlobal(Runnable task);

    void executeGlobal(Runnable task);

    void runAtLocation(Location location, Runnable task);

    void executeAtLocation(Location location, Runnable task);

    void runForEntity(Entity entity, Runnable task);

    void executeForEntity(Entity entity, Runnable task);

    SchedulerTaskHandle runGlobalRepeating(Runnable task, long delayTicks, long periodTicks);
}

package me.crylonz.deadchest.scheduler;

@FunctionalInterface
public interface SchedulerTaskHandle {
    void cancel();
}

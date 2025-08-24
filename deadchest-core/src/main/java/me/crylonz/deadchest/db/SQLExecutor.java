package me.crylonz.deadchest.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SQLExecutor {
    private final ExecutorService pool;

    public SQLExecutor() {
        this.pool = Executors.newFixedThreadPool(2);
    }

    public void runAsync(Runnable task) {
        pool.submit(task);
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }
    }
}

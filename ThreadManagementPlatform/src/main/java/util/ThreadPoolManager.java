package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {

    private static ExecutorService threadPool =
            Executors.newFixedThreadPool(3);

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public static void shutdown() {
        threadPool.shutdown();
    }
}
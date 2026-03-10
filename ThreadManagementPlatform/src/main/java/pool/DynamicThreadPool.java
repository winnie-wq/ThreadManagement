//动态线程池
package pool;

import java.util.concurrent.*;

public class DynamicThreadPool {

    private ThreadPoolExecutor executor;

    public DynamicThreadPool(int taskCount){
        int coreSize, maxSize;
        if(taskCount <= 5){ coreSize = 2; maxSize = 4; }
        else if(taskCount <= 10){ coreSize = 4; maxSize = 8; }
        else{ coreSize = 6; maxSize = 12; }

        executor = new ThreadPoolExecutor(coreSize, maxSize,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        System.out.println("线程池初始化: 核心线程=" + coreSize + ", 最大线程=" + maxSize);
    }

    public void executeTask(Runnable task){ executor.execute(task); }

    public ThreadPoolExecutor getExecutor(){ return executor; }

    public void shutdown(){ executor.shutdown(); }
}
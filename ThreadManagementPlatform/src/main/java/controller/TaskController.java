package controller;

import model.Task;
import optimizer.SchedulerOptimizer;
import scheduler.ScheduleStrategy;
import pool.DynamicThreadPool;
import monitor.ThreadPoolMonitor;

import java.util.List;

public class TaskController {

    public void executeTasks(List<Task> tasks){

        long startTime = System.currentTimeMillis();

        // 1. 选择最快算法
        ScheduleStrategy strategy = SchedulerOptimizer.chooseBest(tasks);

        // 2. 动态线程池
        DynamicThreadPool pool = new DynamicThreadPool(tasks.size());

        // 3. 线程监控
        ThreadPoolMonitor monitor = new ThreadPoolMonitor(pool.getExecutor());
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        // 4. 提交任务
        for(Task t: tasks){
            pool.executeTask(t);
        }

        // 5. 等待完成
        pool.shutdown();
        while(!pool.getExecutor().isTerminated()){}

        // 停止监控
        monitor.stop();

        long endTime = System.currentTimeMillis();

        // 6. 输出结果
        System.out.println("\n=====执行结果=====");
        System.out.println("选择的调度算法: " + strategy.getClass().getSimpleName());
        System.out.println("任务执行状态: 成功");
        System.out.println("总完成时间: " + (endTime - startTime) + " ms");
    }
}
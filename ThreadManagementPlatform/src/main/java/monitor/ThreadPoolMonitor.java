package monitor;


import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitor implements Runnable {

    private ThreadPoolExecutor executor;
    private boolean running = true;

    public ThreadPoolMonitor(ThreadPoolExecutor executor){ this.executor = executor; }

    @Override
    public void run(){
        while(running){
            System.out.println("\n=====线程池监控=====");
            System.out.println("当前线程数: " + executor.getPoolSize());
            System.out.println("活跃线程数: " + executor.getActiveCount());
            System.out.println("已完成任务数: " + executor.getCompletedTaskCount());
            System.out.println("等待任务数: " + executor.getQueue().size());
            try{ Thread.sleep(2000); } catch(Exception e){ e.printStackTrace(); }
        }
    }

    public void stop(){ running = false; }
}
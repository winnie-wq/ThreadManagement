import controller.TaskController;
import queue.TaskQueue;
import scheduler.MLFQScheduler;
import scheduler.PriorityScheduler;
import scheduler.RoundRobinScheduler;
import scheduler.ScheduleStrategy;
import scheduler.Scheduler;
import util.ThreadPoolManager;

import java.util.Scanner;

public class Main {

    public static void main(String[] args){

        TaskQueue queue=new TaskQueue();

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("选择调度算法:");
            System.out.println("1 优先级调度");
            System.out.println("2 时间片轮转");
            System.out.println("3 多级反馈队列");

            int choice = scanner.nextInt();

            ScheduleStrategy strategy = null;

            switch(choice){

                case 1:
                    strategy = new PriorityScheduler(queue);
                    break;

                case 2:
                    strategy = new RoundRobinScheduler();
                    break;

                case 3:
                    strategy = new MLFQScheduler();
                    break;

                default:
                    System.out.println("输入无效，默认使用优先级调度");
                    strategy = new PriorityScheduler(queue);
                    break;
            }

            Scheduler scheduler = new Scheduler(strategy);

            TaskController controller =
                    new TaskController(scheduler);

            while(true){

                System.out.println("1 提交任务");
                System.out.println("2 执行任务");
                System.out.println("3 退出");

                int c=scanner.nextInt();

                if(c==1){

                    System.out.print("任务名称:");

                    String name=scanner.next();

                    System.out.print("优先级:");

                    int p=scanner.nextInt();

                    System.out.print("执行时间:");

                    int t=scanner.nextInt();

                    controller.createTask(name,p,t);

                }

                if(c==2){

                    scheduler.schedule();

                }

                if(c==3){

                    ThreadPoolManager.shutdown();
                    break;

                }

            }
        }

    }

}
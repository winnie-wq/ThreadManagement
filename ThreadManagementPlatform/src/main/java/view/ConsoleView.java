package view;

import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 控制台视图：交互式录入任务，或提供内置演示数据集便于验收。
 */
public class ConsoleView {

    /**
     * 从标准输入读取任务数量及每个任务的名称、优先级、执行时间（毫秒）。
     *
     * @return 新建的任务列表
     */
    public List<Task> inputTasks() {
        Scanner sc = new Scanner(System.in);
        List<Task> tasks = new ArrayList<>();

        System.out.print("请输入任务数量: ");
        int n = sc.nextInt();
        if (n <= 0) {
            return tasks;
        }

        for (int i = 1; i <= n; i++) {
            System.out.print("任务" + i + " 名称: ");
            String name = sc.next();
            System.out.print("任务" + i + " 优先级(整数越大越高): ");
            int p = sc.nextInt();
            System.out.print("任务" + i + " 执行时间(ms，建议演示用 100~800): ");
            int t = sc.nextInt();
            tasks.add(new Task(i, name, p, t));
        }
        return tasks;
    }

    /**
     * 内置一组短任务，用于快速演示（配合 Main 参数 {@code demo}）。
     * 执行时间刻意较短，避免优化器四次仿真等待过久。
     */
    public static List<Task> buildDemoTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(1, "Demo-A", 8, 200));
        tasks.add(new Task(2, "Demo-B", 3, 350));
        tasks.add(new Task(3, "Demo-C", 5, 250));
        tasks.add(new Task(4, "Demo-D", 2, 400));
        tasks.add(new Task(5, "Demo-E", 6, 150));
        return tasks;
    }
}

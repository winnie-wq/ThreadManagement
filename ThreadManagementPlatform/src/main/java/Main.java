import controller.TaskController;
import model.Task;
import view.ConsoleView;

import java.util.List;

/**
 * 多线程管理平台入口：无参时控制台录入；{@code demo} 参数使用内置短任务集。
 */
public class Main {

    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        TaskController controller = new TaskController();

        List<Task> tasks;
        if (args.length > 0 && "demo".equalsIgnoreCase(args[0].trim())) {
            System.out.println("=== 演示模式：内置 5 个短任务（执行时间 150~400ms）===");
            tasks = ConsoleView.buildDemoTasks();
        } else {
            tasks = view.inputTasks();
        }

        controller.executeTasks(tasks);
    }
}

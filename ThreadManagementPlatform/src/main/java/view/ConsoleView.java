package view;


import model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleView {

    public List<Task> inputTasks(){

        Scanner sc = new Scanner(System.in);
        List<Task> tasks = new ArrayList<>();

        System.out.print("请输入任务数量: ");
        int n = sc.nextInt();

        for(int i=1;i<=n;i++){
            System.out.print("任务名: ");
            String name = sc.next();
            System.out.print("优先级(整数越大优先级越高): ");
            int p = sc.nextInt();
            System.out.print("执行时间(ms): ");
            int t = sc.nextInt();
            tasks.add(new Task(i, name, p, t));
        }
        return tasks;
    }
}
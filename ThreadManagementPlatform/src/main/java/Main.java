
import view.ConsoleView;
import controller.TaskController;
import model.Task;
import java.util.List;

public class Main {
    public static void main(String[] args){

        ConsoleView view = new ConsoleView();
        TaskController controller = new TaskController();

        List<Task> tasks = view.inputTasks();
        controller.executeTasks(tasks);
    }
}
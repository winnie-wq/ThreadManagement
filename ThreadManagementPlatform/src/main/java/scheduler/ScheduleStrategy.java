//算法
package scheduler;

import model.Task;

public interface ScheduleStrategy {
    void addTask(Task task);
    Task getNextTask();
}
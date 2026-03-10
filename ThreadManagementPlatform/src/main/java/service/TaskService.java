package service;

import model.Task;
import queue.TaskQueue;

public class TaskService {

    private TaskQueue taskQueue;

    public TaskService(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void submitTask(int id, String name, int priority, int time) {

        Task task = new Task(id, name, priority, time);

        taskQueue.addTask(task);
    }
}
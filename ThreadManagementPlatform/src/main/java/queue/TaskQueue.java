package queue;

import model.Task;

import java.util.PriorityQueue;
import java.util.Queue;

public class TaskQueue {

    private Queue<Task> queue;

    public TaskQueue(){

        queue=new PriorityQueue<>(
                (a,b)->b.getPriority()-a.getPriority()
        );

    }

    public synchronized void addTask(Task task){

        queue.add(task);

        System.out.println("任务加入队列:"+task);

    }

    public synchronized Task getTask(){

        return queue.poll();

    }

    public synchronized int size(){

        return queue.size();

    }

    public synchronized boolean isEmpty(){

        return queue.isEmpty();

    }

}
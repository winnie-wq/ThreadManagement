package queue;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskHistoryQueue {

    private List<Task> history=new ArrayList<>();

    public synchronized void addHistory(Task task){

        history.add(task);

    }

    public void showHistory(){

        for(Task t:history){

            System.out.println(t);

        }

    }

}
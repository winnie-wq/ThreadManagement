package controller;

import model.Task;
import util.IDGenerator;
import scheduler.Scheduler;

public class TaskController {

    private Scheduler scheduler;

    public TaskController(Scheduler scheduler){

        this.scheduler = scheduler;

    }

    public void createTask(String name,int priority,int time){

        int id=IDGenerator.next();

        Task task=new Task(id,name,priority,time);

        scheduler.submitTask(task);

    }

}
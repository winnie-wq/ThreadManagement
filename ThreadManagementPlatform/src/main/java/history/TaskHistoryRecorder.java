package history;

import model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 记录任务执行结束后的快照字符串，用于结果追溯与报告中的“测试输出”整理。
 */
public class TaskHistoryRecorder {

    /** 与 ArrayList 组合，多线程追加时由 synchronizedList 保证可见性与原子追加 */
    private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    /**
     * 在任务 {@link Task#run()} 返回后调用，写入一行可读摘要。
     *
     * @param task 已执行完毕（或失败）的任务
     */
    public void record(Task task) {
        long submit = task.getSubmitTimeMs();
        long start = task.getStartTimeMs();
        long finish = task.getFinishTimeMs();
        long turnaround = (submit >= 0 && finish >= 0) ? (finish - submit) : -1;
        long wait = (submit >= 0 && start >= 0) ? (start - submit) : -1;
        String line = String.format(
                "id=%d name=%s status=%s priority=%d execMs=%d waitMs=%s turnaroundMs=%s",
                task.getTaskId(),
                task.getTaskName(),
                task.getStatus(),
                task.getPriority(),
                task.getExecuteTime(),
                wait >= 0 ? Long.toString(wait) : "n/a",
                turnaround >= 0 ? Long.toString(turnaround) : "n/a");
        lines.add(line);
    }

    /** 返回当前记录的只读快照（用于一次性打印） */
    public List<String> snapshot() {
        synchronized (lines) {
            return new ArrayList<>(lines);
        }
    }
}

package model;

import java.util.List;

/**
 * 从一批已执行任务的提交/开始/结束时间戳中计算汇总指标，用于调试分析与报告中的“统计输出”小节。
 */
public final class TaskMetricsSummary {

    private TaskMetricsSummary() {
    }

    /**
     * 将各任务的等待时间、周转时间的最大值、最小值与平均值打印到标准输出。
     * 仅统计时间戳齐全且已成功结束（{@link TaskStatus#FINISHED}）的任务。
     *
     * @param tasks 任务列表
     */
    public static void printAggregate(List<Task> tasks) {
        long minWait = Long.MAX_VALUE;
        long maxWait = Long.MIN_VALUE;
        long sumWait = 0;
        int countWait = 0;

        long minTurn = Long.MAX_VALUE;
        long maxTurn = Long.MIN_VALUE;
        long sumTurn = 0;
        int countTurn = 0;

        for (Task t : tasks) {
            if (t.getStatus() != TaskStatus.FINISHED) {
                continue;
            }
            if (t.getSubmitTimeMs() >= 0 && t.getStartTimeMs() >= 0) {
                long w = t.getStartTimeMs() - t.getSubmitTimeMs();
                minWait = Math.min(minWait, w);
                maxWait = Math.max(maxWait, w);
                sumWait += w;
                countWait++;
            }
            if (t.getSubmitTimeMs() >= 0 && t.getFinishTimeMs() >= 0) {
                long tr = t.getFinishTimeMs() - t.getSubmitTimeMs();
                minTurn = Math.min(minTurn, tr);
                maxTurn = Math.max(maxTurn, tr);
                sumTurn += tr;
                countTurn++;
            }
        }

        System.out.println("\n---------- 成功任务统计(等待/周转, ms) ----------");
        if (countWait == 0) {
            System.out.println("无可用等待时间样本（检查任务是否完成及时间戳是否记录）");
        } else {
            double avgWait = (double) sumWait / countWait;
            System.out.printf("等待时间: min=%d max=%d avg=%.2f (n=%d)%n", minWait, maxWait, avgWait, countWait);
        }
        if (countTurn == 0) {
            System.out.println("无可用周转时间样本");
        } else {
            double avgTurn = (double) sumTurn / countTurn;
            System.out.printf("周转时间: min=%d max=%d avg=%.2f (n=%d)%n", minTurn, maxTurn, avgTurn, countTurn);
        }
    }
}

package resource;

import java.util.concurrent.Semaphore;

/**
 * 模拟系统中的有限资源（如 I/O 槽位、许可证）。任务在执行前 acquire，结束后 release，
 * 体现操作系统中同步与互斥、资源竞争下的阻塞行为。
 */
public class ResourceManager {

    /** 信号量：许可数 = 可同时占用资源的任务上限 */
    private final Semaphore permits;

    /**
     * @param permitCount 资源份数，必须为正数
     */
    public ResourceManager(int permitCount) {
        if (permitCount <= 0) {
            throw new IllegalArgumentException("permitCount 必须大于 0");
        }
        this.permits = new Semaphore(permitCount, true);
    }

    /**
     * 申请占用一份资源；若无可用许可则阻塞等待（公平队列顺序由 Semaphore 构造参数决定）。
     */
    public void acquire() throws InterruptedException {
        permits.acquire();
    }

    /** 释放一份资源，唤醒可能在等待的线程 */
    public void release() {
        permits.release();
    }

    /** 当前瞬间剩余可用许可（近似可读指标，严格一致性不作为并发契约） */
    public int availablePermits() {
        return permits.availablePermits();
    }
}

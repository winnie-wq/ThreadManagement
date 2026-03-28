package config;

import java.util.concurrent.TimeUnit;

/**
 * 平台运行期可调常量集中定义，避免魔法数散落在控制层与资源模块中，便于课设报告中说明参数含义。
 */
public final class ExecutionConfig {

    /** 模拟系统中可同时占用的资源份数（与任务数取 min，保证至少 1） */
    public static final int DEFAULT_RESOURCE_PERMITS = 3;

    /** 主线程等待线程池终止的上限 */
    public static final long POOL_AWAIT_MINUTES = 60L;

    public static final TimeUnit POOL_AWAIT_UNIT = TimeUnit.MINUTES;

    /** 监控线程 join 的超时，避免 stop 后无限等待 */
    public static final long MONITOR_JOIN_MS = 2000L;

    private ExecutionConfig() {
    }
}

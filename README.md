ThreadManagementPlatform（多线程管理平台）功能概览

- 任务建模与生命周期
  - Task 实现 Runnable，包含任务 id/名称/优先级/执行时长（ms）
  - 记录提交/开始/结束时间戳，状态覆盖 NEW/WAITING/RUNNING/FINISHED/FAILED
- 四类调度策略（用于仿真对比）
  - FCFS：先来先服务（FIFO）
  - Priority：优先级调度（堆/优先队列）
  - RR：时间片轮转（简化实现：单次时间片推进）
  - MLFQ：多级反馈队列（三级队列，时间片递增，简化降级规则）
- 策略优化器（墙钟仿真择优）
  - 对同一任务集副本分别运行四种策略（真实 sleep 推进墙钟）
  - 输出每种策略仿真耗时并自动选择最短策略（用于展示与提交顺序参考）
- 动态线程池执行
  - 基于 ThreadPoolExecutor，按任务规模设置 core/max 线程数
  - 提供 shutdown / awaitTermination / shutdownNow 等生命周期控制
- 有限资源与同步互斥
  - Semaphore 模拟有限资源许可：任务执行前 acquire，结束后 release
  - 在许可不足时阻塞等待，体现资源竞争下的同步行为
- 线程池监控与结果汇总
  - 监控线程周期输出：poolSize、active、queue、completed 等指标
  - 输出每个任务等待时间/周转时间，打印历史快照与汇总统计（min/max/avg）
- 自动化测试（JUnit5）
  - 功能/容错/性能/集成测试覆盖主要链路（含 500 任务规模的性能用例）

项目结构
    src/main/java
      controller/TaskController.java        # 一次实验编排与报表输出
      view/ConsoleView.java                 # 控制台录入 + demo 数据
      model/Task.java                       # 任务模型（时间戳、状态）
      model/TaskStatus.java                 # 任务状态枚举
      model/TaskMetricsSummary.java         # 等待/周转时间聚合统计
      scheduler/ScheduleStrategy.java       # 调度策略接口
      scheduler/FCFSScheduler.java          # FCFS 仿真
      scheduler/PriorityScheduler.java      # 优先级仿真（PriorityQueue）
      scheduler/RoundRobinScheduler.java    # RR 仿真
      scheduler/MLFQScheduler.java          # MLFQ 仿真
      optimizer/SchedulerOptimizer.java     # 墙钟仿真择优
      service/TaskService.java              # 提交顺序 + 资源包装执行
      resource/ResourceManager.java         # Semaphore 资源管理
      pool/DynamicThreadPool.java           # ThreadPoolExecutor 封装
      monitor/ThreadPoolMonitor.java        # 周期采样线程池指标
      history/TaskHistoryRecorder.java      # 任务完成快照记录
      config/ExecutionConfig.java           # 运行参数常量
      util/LogUtil.java                     # 时间戳日志
      Main.java                             # 程序入口（支持 demo）
    
    src/test/java/threadmanagement
      FunctionalTests.java                  #功能测试
      FaultToleranceTests.java              #容错性测试
      PerformanceTests.java                 #性能测试（含 500 任务）
      IntegrationTests.java                 #集成与改进方向
      TestUtils.java                        #测试辅助工具

输出说明
- 优化器会打印四种策略的墙钟仿真耗时与最终选择策略
- 监控线程会周期输出线程池关键指标（活跃线程数、队列长度、完成任务数等）
- 执行结束会输出：
  - 选定调度策略与墙钟总时间
  - 每个任务的等待/周转时间
  - 等待/周转统计汇总（min/max/avg）
  - 历史快照（每个任务一行）


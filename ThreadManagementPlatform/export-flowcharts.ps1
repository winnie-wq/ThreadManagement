$ErrorActionPreference = "Stop"

$outDir = Join-Path $PSScriptRoot "flowcharts-export"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$charts = @(
    @{
        Name = "5_1_system_overview"
        Code = @"
flowchart TD
    A([开始])
    B[启动 Main]
    C[任务来源选择]
    D[demo 构造任务]
    E[控制台录入任务]
    F[TaskController 编排]
    G{任务为空?}
    H[标记提交时间]
    I[优化器墙钟择优]
    J[创建池与资源器]
    K[启动监控线程]
    L[生成提交顺序]
    M[提交包装任务]
    N[等待池结束]
    O{等待超时?}
    P[shutdownNow]
    Q[停止监控]
    R[汇总输出报表]
    S([结束])

    A --> B --> C
    C -->|demo| D --> F
    C -->|手动| E --> F
    F --> G
    G -->|是| S
    G -->|否| H --> I --> J --> K --> L --> M --> N --> O
    O -->|是| P --> Q
    O -->|否| Q
    Q --> R --> S
"@
    },
    @{
        Name = "5_2_1_fcfs"
        Code = @"
flowchart TD
    S([开始])
    A{队列空?}
    B[poll 队首]
    C[子线程 sleep 总时长]
    D[join 子线程]
    E([结束:返回任务])
    Z([结束:返回空])

    S --> A
    A -->|是| Z
    A -->|否| B --> C --> D --> E
"@
    },
    @{
        Name = "5_2_2_priority"
        Code = @"
flowchart TD
    S([开始])
    A{堆空?}
    B[poll 最高优先]
    C[子线程 sleep 总时长]
    D[join]
    E([结束:返回任务])
    Z([结束:返回空])

    S --> A
    A -->|是| Z
    A -->|否| B --> C --> D --> E
"@
    },
    @{
        Name = "5_2_3_rr"
        Code = @"
flowchart TD
    S([开始])
    A{队列空?}
    B[poll 队首]
    C[子线程 sleep 片长]
    D[join]
    E([结束:返回任务])
    Z([结束:返回空])

    S --> A
    A -->|是| Z
    A -->|否| B --> C --> D --> E
"@
    },
    @{
        Name = "5_2_4_mlfq"
        Code = @"
flowchart TD
    S([开始])
    A{L1 非空?}
    B[取 L1 并睡眠]
    C{需降级?}
    D[入 L2]
    E{L2 非空?}
    F[取 L2 并睡眠]
    G{需降级?}
    H[入 L3]
    I{L3 非空?}
    J[取 L3 并睡眠]
    K[join]
    L([结束:返回任务])
    Z([结束:返回空])

    S --> A
    A -->|是| B --> C
    C -->|是| D --> K
    C -->|否| K
    A -->|否| E
    E -->|是| F --> G
    G -->|是| H --> K
    G -->|否| K
    E -->|否| I
    I -->|是| J --> K
    I -->|否| Z
    K --> L
"@
    },
    @{
        Name = "5_3_optimizer"
        Code = @"
flowchart TD
    S([开始])
    A[测 FCFS 耗时]
    B[测 Priority 耗时]
    C[测 RR 耗时]
    D[测 MLFQ 耗时]
    E[取最小策略]
    F[输出选中日志]
    G([结束:返回策略])

    S --> A --> B --> C --> D --> E --> F --> G
"@
    },
    @{
        Name = "5_4_order_tasks"
        Code = @"
flowchart TD
    S([开始])
    A{是否优先策略?}
    B[按优先降序排序]
    C[保持原顺序]
    D([结束:返回列表])

    S --> A
    A -->|是| B --> D
    A -->|否| C --> D
"@
    },
    @{
        Name = "5_5_resource"
        Code = @"
flowchart TD
    S([开始])
    A[申请许可]
    B{许可可用?}
    C[阻塞等待]
    D[进入临界执行]
    E[释放许可]
    F([结束])

    S --> A --> B
    B -->|否| C --> B
    B -->|是| D --> E --> F
"@
    },
    @{
        Name = "5_6_pool_lifecycle"
        Code = @"
flowchart TD
    S([开始])
    A[execute 投递]
    B[队列排队或执行]
    C[shutdown]
    D[await 终止]
    E{超时?}
    F[shutdownNow]
    G([结束:池停止])

    S --> A --> B --> C --> D --> E
    E -->|是| F --> G
    E -->|否| G
"@
    },
    @{
        Name = "5_7_monitor"
        Code = @"
flowchart TD
    S([开始])
    A{running?}
    B[打印快照]
    C[sleep 周期]
    D{中断?}
    E([结束])

    S --> A
    A -->|否| E
    A -->|是| B --> C --> D
    D -->|是| E
    D -->|否| A
"@
    },
    @{
        Name = "5_8_task_chain"
        Code = @"
flowchart TD
    S([开始])
    A[池线程取 Runnable]
    B[等待许可]
    C[获许可]
    D[Task.run]
    E[写历史]
    F{需释放?}
    G[release]
    H([结束])

    S --> A --> B --> C --> D --> E --> F
    F -->|是| G --> H
    F -->|否| H
"@
    },
    @{
        Name = "5_9_controller"
        Code = @"
flowchart TD
    S([开始])
    A{列表空?}
    B[标记提交]
    C[择优策略]
    D[装配资源与池]
    E[启动监控]
    F[排序并提交]
    G[等待结束]
    H{超时?}
    I[强停兜底]
    J[停监控]
    K[输出报表]
    L([结束])

    S --> A
    A -->|是| L
    A -->|否| B --> C --> D --> E --> F --> G --> H
    H -->|是| I --> J
    H -->|否| J
    J --> K --> L
"@
    }
)

foreach ($chart in $charts) {
    $mmdPath = Join-Path $outDir ($chart.Name + ".mmd")
    $pngPath = Join-Path $outDir ($chart.Name + ".png")
    $svgPath = Join-Path $outDir ($chart.Name + ".svg")

    @("%%{init: {'flowchart': {'htmlLabels': false, 'curve': 'linear'}} }%%", $chart.Code) -join "`n" | Set-Content -Path $mmdPath -Encoding UTF8
    npx -y @mermaid-js/mermaid-cli -i $mmdPath -o $pngPath -t neutral -b transparent
    npx -y @mermaid-js/mermaid-cli -i $mmdPath -o $svgPath -t neutral -b transparent
}

Write-Host "Export done: $outDir"

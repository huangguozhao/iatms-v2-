# Quartz调度流程图

## 1. 定时任务创建流程

```mermaid
flowchart TD
    subgraph 任务创建
        A1(["开始"]) --> A2[Controller接收请求<br/>createScheduledTask]
        A2 --> A3[Service.createScheduledTask<br/>创建任务实体]
        A3 --> A4[构建ScheduledTestTask<br/>设置任务参数]
        A4 --> A5[scheduledTaskMapper.insert<br/>保存到数据库]
        A5 --> A6[createQuartzJob<br/>创建Quartz任务]
        A6 --> A7[buildTrigger<br/>构建触发器]
        A7 --> A8{触发器类型}
        
        A8 -->|cron| A9[CronScheduleBuilder<br/>Cron表达式]
        A8 -->|simple| A10[SimpleScheduleBuilder<br/>简单重复]
        A8 -->|daily| A11[生成cron: 0 minute hour ? * *<br/>每日定时]
        A8 -->|weekly| A12[生成cron: 0 minute hour ? * days<br/>每周定时]
        A8 -->|monthly| A13[生成cron: 0 minute hour day * ?<br/>每月定时]
        
        A9 --> A14[scheduler.scheduleJob<br/>注册JobDetail+Trigger]
        A10 --> A14
        A11 --> A14
        A12 --> A14
        A13 --> A14
        
        A14 --> A15[updateNextTriggerTime<br/>更新下次触发时间]
        A15 --> A16([结束])
    end

    style A1 fill:#e1f5fe
    style A16 fill:#c8e6c9
```

## 2. Quartz触发执行流程

```mermaid
flowchart TD
    subgraph Quartz触发执行
        B1(["到达触发时间"]) --> B2[Quartz Scheduler<br/>调度器触发]
        B2 --> B3[ScheduledTestTaskJob.execute<br/>Job执行器]
        B3 --> B4[从JobDataMap获取<br/>taskId + userId]
        B4 --> B5[scheduledTaskService<br/>.executeScheduledTask]
        B5 --> B6[executeTestTask<br/>执行任务]
        
        B6 --> B7{检查任务状态}
        B7 -->|任务不存在/已删除| B8[抛出异常]
        B8 --> B20([结束])
        
        B7 -->|任务未启用| B9[检查isTriggered参数]
        B9 -->|非手动触发| B8
        B9 -->|手动触发| B10
        
        B7 -->|任务启用| B10[检查skipIfPreviousFailed<br/>上次失败是否跳过]
        B10 --> B11{上次执行状态}
        B11 -->|failed且skip=true| B12[跳过执行]
        B12 --> B20
        B11 -->|其他| B13[继续执行]
        
        B13 --> B14[创建执行记录<br/>ScheduledTaskExecution]
        B14 --> B15[scheduledTaskExecutionMapper.insert]
        B15 --> B16[更新状态=running<br/>设置actualStartTime]
        B16 --> B17[更新任务状态<br/>lastExecutionStatus=running<br/>totalExecutions++]
        B17 --> B18[executeTest<br/>执行测试]
        B18 --> B19[更新结果统计<br/>success/failed<br/>发送通知]
        B19 --> B20
    end

    style B1 fill:#e1f5fe
    style B20 fill:#c8e6c9
    style B8 fill:#ffcdd2
```

## 3. 测试执行详细流程

```mermaid
flowchart TD
    subgraph 测试执行
        C1[executeTest<br/>开始执行] --> C2{执行模式}
        
        C2 -->|有caseIds列表| C3[解析caseIds JSON]
        C3 --> C4[testExecutionService<br/>.executeTestCases]
        C4 --> C16[收集执行结果]
        
        C2 -->|无caseIds| C5{根据taskType执行}
        
        C5 -->|single_case| C6[executeTestCase<br/>执行单用例]
        C5 -->|api| C7[executeApi<br/>执行接口]
        C5 -->|module| C8[executeModule<br/>执行模块]
        C5 -->|project| C9[executeProject<br/>执行项目]
        C5 -->|test_suite| C10[executeTestSuite<br/>执行测试套件]
        
        C6 --> C11[extractExecutionResults<br/>提取执行结果]
        C7 --> C11
        C8 --> C11
        C9 --> C11
        C10 --> C11
        
        C11 --> C12{是否有caseIds?}
        C12 -->|是| C13[批量执行已有报告]
        C12 -->|否| C14[generateTestReport<br/>生成测试报告]
        
        C14 --> C16
        C13 --> C16
        
        C16 --> C17[更新执行记录<br/>status/duration<br/>passed/failed/total]
        C17 --> C18[更新任务统计<br/>successfulExecutions<br/>failedExecutions]
        C18 --> C19[updateNextTriggerTime<br/>更新下次触发时间]
        C19 --> C20[sendNotification<br/>发送通知]
        C20 --> C21([执行完成])
    end

    style C1 fill:#e1f5fe
    style C21 fill:#c8e6c9
```

## 4. 任务管理流程（更新/删除/暂停）

```mermaid
flowchart TD
    subgraph 任务管理
        D1(["开始"]) --> D2{操作类型}
        
        D2 -->|更新任务| D3[updateScheduledTask]
        D3 --> D4[查询任务是否存在]
        D4 --> D5{存在?}
        D5 -->|否| D6[抛出异常]
        D5 -->|是| D7[更新任务实体]
        D7 --> D8[updateQuartzJob<br/>更新Quartz任务]
        D8 --> D9[deleteJob + createJob<br/>重建任务]
        D9 --> D10[updateNextTriggerTime]
        
        D2 -->|删除任务| D11[deleteQuartzJob]
        D11 --> D12[deleteJob删除任务]
        D12 --> D13[scheduledTaskMapper.delete<br/>逻辑删除]
        
        D2 -->|暂停任务| D14[pauseQuartzJob]
        D14 --> D15[pauseJob暂停任务]
        
        D2 -->|恢复任务| D16[resumeQuartzJob]
        D16 --> D17[resumeJob恢复任务]
        
        D2 -->|立即执行| D18[executeScheduledTaskNow]
        D18 --> D19[executeTestTask<br/>isTriggered=false]
        
        D10 --> D20([结束])
        D13 --> D20
        D15 --> D20
        D17 --> D20
        D19 --> D20
    end

    style D1 fill:#e1f5fe
    style D20 fill:#c8e6c9
    style D6 fill:#ffcdd2
```

## 5. 核心组件关系

```mermaid
flowchart TD
    subgraph 核心组件
        E1[ScheduledTaskController]
        E2[ScheduledTaskService]
        E3[Scheduler<br/>Quartz调度器]
        E4[ScheduledTestTaskJob<br/>Job执行器]
        E5[TestExecutionService]
    end
    
    subgraph 数据表
        E6[ScheduledTestTask<br/>定时任务表]
        E7[ScheduledTaskExecution<br/>执行记录表]
        E8[QRTZ_JOB_DETAILS<br/>Quartz任务表]
        E9[QRTZ_TRIGGERS<br/>Quartz触发器表]
    end

    E1 -->|CRUD| E2
    E2 -->|管理| E6
    E2 -->|调度| E3
    E3 -->|触发| E4
    E4 -->|执行| E2
    E2 -->|执行测试| E5
    E5 -->|结果| E2
    E2 -->|记录| E7
    E3 -->|存储| E8
    E3 -->|存储| E9
```

## 6. 执行记录流程

```mermaid
flowchart TD
    subgraph 执行记录
        F1([测试执行]) --> F2[创建记录<br/>status=pending]
        F2 --> F3[更新状态=running<br/>actualStartTime]
        F3 --> F4[执行测试]
        
        F4 --> F5{执行成功?}
        F5 -->|是| F6[更新status=success<br/>actualEndTime<br/>duration]
        F5 -->|否| F7[更新status=failed<br/>errorMessage]
        F7 --> F8{启用重试?}
        F8 -->|是| F9[handleRetry<br/>重试逻辑]
        F8 -->|否| F10
        
        F9 --> F4
        F6 --> F10
        F10 --> F11[更新任务统计<br/>totalExecutions<br/>successful/failed]
        F11 --> F12[更新下次触发时间<br/>updateNextTriggerTime]
        F12 --> F13[sendNotification<br/>发送通知]
        F13 --> F14([结束])
    end

    style F1 fill:#e1f5fe
    style F14 fill:#c8e6c9
```

## 流程说明

### 1. 任务创建
1. 用户通过Controller创建定时任务
2. Service构建`ScheduledTestTask`实体，保存到数据库
3. 根据触发器类型（cron/simple/daily/weekly/monthly）构建Quartz Trigger
4. 调用`scheduler.scheduleJob()`注册JobDetail和Trigger
5. 更新下次触发时间

### 2. Quartz触发执行
1. Quartz Scheduler在触发时间点触发任务
2. `ScheduledTestTaskJob.execute()`执行
3. 从JobDataMap获取taskId和userId
4. 调用`scheduledTaskService.executeScheduledTask()`
5. 创建执行记录`ScheduledTaskExecution`
6. 检查任务状态和跳过条件
7. 执行测试

### 3. 测试执行
- 支持多种执行模式：有caseIds列表、single_case、api、module、project、test_suite
- 调用`TestExecutionService`执行具体测试
- 提取执行结果统计
- 单独执行时生成测试报告
- 更新任务统计和执行记录

### 4. 任务管理
- **更新**：先删除旧任务，再创建新任务
- **删除**：删除Quartz任务 + 逻辑删除数据库记录
- **暂停/恢复**：暂停或恢复Quartz任务
- **立即执行**：直接调用executeTestTask执行

### 5. 执行记录
- 每次执行创建`ScheduledTaskExecution`记录
- 记录执行状态、时间、结果统计
- 支持重试机制
- 执行完成后发送通知
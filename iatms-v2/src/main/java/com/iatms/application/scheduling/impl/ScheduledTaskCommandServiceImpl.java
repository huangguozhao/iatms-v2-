package com.iatms.application.scheduling.impl;

import com.iatms.application.scheduling.ScheduledTaskCommandService;
import com.iatms.application.scheduling.dto.command.CreateScheduledTaskCmd;
import com.iatms.application.testing.TestExecutionCommandService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.domain.model.entity.ScheduledTask;
import com.iatms.domain.model.vo.ScheduledTaskDetailVO;
import com.iatms.infrastructure.job.TestExecutionJob;
import com.iatms.infrastructure.persistence.mapper.ScheduledTaskMapper;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 定时任务命令服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskCommandServiceImpl implements ScheduledTaskCommandService {

    private final ScheduledTaskMapper taskMapper;
    private final Scheduler scheduler;
    private final TestExecutionCommandService executionCommandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduledTaskDetailVO createTask(CreateScheduledTaskCmd cmd, Long userId) {
        ScheduledTask task = new ScheduledTask();
        task.setTaskName(cmd.getName());
        task.setDescription(cmd.getDescription());
        task.setTaskType(cmd.getTaskType());
        task.setTriggerType(cmd.getTriggerType());
        task.setCronExpression(cmd.getCronExpression());

        // 设置目标ID和名称（根据任务类型）
        if ("test_suite".equals(cmd.getTaskType())) {
            task.setTargetId(cmd.getTargetId() != null ? cmd.getTargetId().intValue() : null);
            task.setTargetName("TEST_SUITE");
        } else if ("api".equals(cmd.getTaskType())) {
            task.setTargetId(cmd.getTargetId() != null ? cmd.getTargetId().intValue() : null);
            task.setTargetName("API_REQUEST");
        }

        task.setExecutionEnvironment(cmd.getEnvironmentId() != null ? "test" : "test");
        task.setIsEnabled(true);
        task.setNotifyOnSuccess(cmd.getNotifyOnSuccess());
        task.setNotifyOnFailure(cmd.getNotifyOnFailure());
        task.setTotalExecutions(0);
        task.setSuccessfulExecutions(0);
        task.setFailedExecutions(0);
        task.setCreatedBy(userId);

        // 计算下次触发时间
        task.setNextTriggerTime(task.calculateNextRun());

        taskMapper.insert(task);

        // 调度 Quartz 任务
        scheduleQuartzJob(task);

        log.info("创建定时任务成功: id={}, name={}, userId={}", task.getId(), task.getTaskName(), userId);

        return getTaskDetail(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduledTaskDetailVO updateTask(Long taskId, CreateScheduledTaskCmd cmd, Long userId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        boolean triggerTypeChanged = false;
        if (cmd.getName() != null) task.setTaskName(cmd.getName());
        if (cmd.getDescription() != null) task.setDescription(cmd.getDescription());
        if (cmd.getTriggerType() != null) {
            task.setTriggerType(cmd.getTriggerType());
            triggerTypeChanged = true;
        }
        if (cmd.getCronExpression() != null) {
            task.setCronExpression(cmd.getCronExpression());
            triggerTypeChanged = true;
        }
        if (cmd.getNotifyOnSuccess() != null) task.setNotifyOnSuccess(cmd.getNotifyOnSuccess());
        if (cmd.getNotifyOnFailure() != null) task.setNotifyOnFailure(cmd.getNotifyOnFailure());

        // 重新计算下次触发时间
        task.setNextTriggerTime(task.calculateNextRun());

        // 如果触发类型改变，重新调度 Quartz 任务
        if (triggerTypeChanged && task.getIsEnabled() != null && task.getIsEnabled()) {
            rescheduleQuartzJob(task);
        }

        taskMapper.updateById(task);

        log.info("更新定时任务成功: id={}, userId={}", taskId, userId);

        return getTaskDetail(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long taskId, Long userId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        // 取消 Quartz 调度
        unscheduleQuartzJob(taskId);

        task.setDeleted(true);
        taskMapper.updateById(task);

        log.info("删除定时任务成功: id={}, userId={}", taskId, userId);
    }

    @Override
    public void runTaskNow(Long taskId, Long userId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        log.info("立即执行定时任务: id={}, userId={}", taskId, userId);

        // 构建执行命令
        StartExecutionCmd cmd = new StartExecutionCmd();
        cmd.setExecutionType(mapTaskTypeToExecutionType(task.getTaskType()));
        cmd.setTargetId(task.getTargetId() != null ? task.getTargetId().longValue() : null);
        cmd.setEnvironmentId(null);
        cmd.setTriggerType("MANUAL");

        // 异步执行测试
        executionCommandService.startExecutionAsync(cmd, userId);

        // 更新任务统计
        task.setTotalExecutions(task.getTotalExecutions() != null ? task.getTotalExecutions() + 1 : 1);
        task.setLastExecutionTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Override
    public void pauseTask(Long taskId, Long userId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        task.setIsEnabled(false);
        taskMapper.updateById(task);

        // 暂停 Quartz 任务
        pauseQuartzJob(taskId);

        log.info("暂停定时任务: id={}, userId={}", taskId, userId);
    }

    @Override
    public void resumeTask(Long taskId, Long userId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        task.setIsEnabled(true);
        task.setNextTriggerTime(task.calculateNextRun());
        taskMapper.updateById(task);

        // 恢复 Quartz 任务
        resumeQuartzJob(taskId);

        log.info("恢复定时任务: id={}, userId={}", taskId, userId);
    }

    // ========== Quartz 调度方法 ==========

    private void scheduleQuartzJob(ScheduledTask task) {
        try {
            String jobIdentity = "scheduledTask-" + task.getId();
            String triggerIdentity = "scheduledTaskTrigger-" + task.getId();

            // 创建 JobDetail
            JobDetail jobDetail = JobBuilder.newJob(TestExecutionJob.class)
                    .usingJobData("taskId", task.getId())
                    .withIdentity(jobIdentity)
                    .storeDurably()
                    .build();

            // 创建 Trigger
            Trigger trigger;
            if ("cron".equals(task.getTriggerType()) && task.getCronExpression() != null) {
                // Cron 表达式触发
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerIdentity)
                        .forJob(jobDetail)
                        .withSchedule(CronScheduleBuilder.cronSchedule(task.getCronExpression())
                                .withMisfireHandlingInstructionDoNothing())
                        .startNow()
                        .build();
            } else if ("simple".equals(task.getTriggerType()) && task.getSimpleRepeatInterval() != null) {
                // 简单重复触发
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerIdentity)
                        .forJob(jobDetail)
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMilliseconds(task.getSimpleRepeatInterval())
                                .withRepeatCount(task.getSimpleRepeatCount() != null ? task.getSimpleRepeatCount() : -1)
                                .withMisfireHandlingInstructionFireNow())
                        .startNow()
                        .build();
            } else if ("daily".equals(task.getTriggerType())) {
                // 每日定时触发
                String cron = String.format("0 %d %d ? * *",
                        task.getDailyMinute() != null ? task.getDailyMinute() : 0,
                        task.getDailyHour() != null ? task.getDailyHour() : 0);
                trigger = TriggerBuilder.newTrigger()
                        .withIdentity(triggerIdentity)
                        .forJob(jobDetail)
                        .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                                .withMisfireHandlingInstructionDoNothing())
                        .build();
            } else {
                log.warn("不支持的触发类型或缺少触发配置: triggerType={}", task.getTriggerType());
                return;
            }

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Quartz任务调度成功: taskId={}", task.getId());

        } catch (Exception e) {
            log.error("Quartz任务调度失败: taskId={}", task.getId(), e);
        }
    }

    private void rescheduleQuartzJob(ScheduledTask task) {
        // 先取消旧任务
        unscheduleQuartzJob(task.getId());
        // 重新调度
        scheduleQuartzJob(task);
    }

    private void unscheduleQuartzJob(Long taskId) {
        try {
            String jobIdentity = "scheduledTask-" + taskId;
            scheduler.deleteJob(new JobKey(jobIdentity));
            log.info("Quartz任务取消成功: taskId={}", taskId);
        } catch (Exception e) {
            log.error("Quartz任务取消失败: taskId={}", taskId, e);
        }
    }

    private void pauseQuartzJob(Long taskId) {
        try {
            String jobIdentity = "scheduledTask-" + taskId;
            scheduler.pauseJob(new JobKey(jobIdentity));
            log.info("Quartz任务暂停成功: taskId={}", taskId);
        } catch (Exception e) {
            log.error("Quartz任务暂停失败: taskId={}", taskId, e);
        }
    }

    private void resumeQuartzJob(Long taskId) {
        try {
            String jobIdentity = "scheduledTask-" + taskId;
            scheduler.resumeJob(new JobKey(jobIdentity));
            log.info("Quartz任务恢复成功: taskId={}", taskId);
        } catch (Exception e) {
            log.error("Quartz任务恢复失败: taskId={}", taskId, e);
        }
    }

    private String mapTaskTypeToExecutionType(String taskType) {
        if (taskType == null) {
            return "TEST_SUITE";
        }
        return switch (taskType) {
            case "single_case" -> "TEST_CASE";
            case "test_suite" -> "TEST_SUITE";
            case "project" -> "PROJECT";
            case "api" -> "TEST_CASE";
            default -> "TEST_SUITE";
        };
    }

    private ScheduledTaskDetailVO getTaskDetail(Long taskId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        return ScheduledTaskDetailVO.builder()
                .id(task.getId())
                .name(task.getTaskName())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .triggerType(task.getTriggerType())
                .cronExpression(task.getCronExpression())
                .targetId(task.getTargetId() != null ? task.getTargetId().longValue() : null)
                .environmentId(null)
                .status(task.getIsEnabled() != null && task.getIsEnabled() ? "ACTIVE" : "PAUSED")
                .lastRunTime(task.getLastExecutionTime())
                .nextRunTime(task.getNextTriggerTime())
                .totalRuns(task.getTotalExecutions())
                .successRuns(task.getSuccessfulExecutions())
                .failedRuns(task.getFailedExecutions())
                .notifyOnSuccess(task.getNotifyOnSuccess())
                .notifyOnFailure(task.getNotifyOnFailure())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .build();
    }
}

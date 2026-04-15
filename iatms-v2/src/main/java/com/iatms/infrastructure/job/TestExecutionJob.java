package com.iatms.infrastructure.job;

import com.iatms.application.testing.TestExecutionCommandService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.domain.model.entity.ScheduledTask;
import com.iatms.infrastructure.config.SpringContextHolder;
import com.iatms.infrastructure.persistence.mapper.ScheduledTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

/**
 * 测试执行定时任务 Job
 */
@Slf4j
public class TestExecutionJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        // 从 JobDataMap 获取数据
        String taskIdStr = dataMap.getString("taskId");
        if (taskIdStr == null || taskIdStr.isEmpty()) {
            log.error("JobDataMap 中缺少 taskId，请检查任务配置");
            throw new JobExecutionException("Missing taskId in JobDataMap");
        }
        Long taskId = Long.valueOf(taskIdStr);

        // 通过 SpringContextHolder 获取 Spring Bean
        TestExecutionCommandService executionCommandService =
                SpringContextHolder.getBean(TestExecutionCommandService.class);
        ScheduledTaskMapper taskMapper =
                SpringContextHolder.getBean(ScheduledTaskMapper.class);

        if (executionCommandService == null || taskMapper == null) {
            log.error("无法从 Spring 上下文获取服务依赖");
            throw new JobExecutionException("Missing service dependencies in Spring Context");
        }

        log.info("Quartz定时任务触发: taskId={}", taskId);

        try {
            // 获取定时任务配置
            ScheduledTask task = taskMapper.selectById(taskId);
            if (task == null) {
                log.error("定时任务不存在: taskId={}", taskId);
                return;
            }

            if (task.getIsEnabled() == null || !task.getIsEnabled()) {
                log.info("定时任务已禁用: taskId={}", taskId);
                return;
            }

            // 构建执行命令
            StartExecutionCmd cmd = new StartExecutionCmd();
            cmd.setExecutionType(mapTaskTypeToExecutionType(task.getTaskType()));
            cmd.setTargetId(task.getTargetId() != null ? task.getTargetId().longValue() : null);
            cmd.setEnvironmentId(null);
            cmd.setTriggerType("SCHEDULED");

            // 异步执行测试
            executionCommandService.startExecutionAsync(cmd, task.getCreatedBy());

            // 更新任务统计
            task.setTotalExecutions(task.getTotalExecutions() != null ? task.getTotalExecutions() + 1 : 1);
            task.setLastExecutionTime(LocalDateTime.now());
            taskMapper.updateById(task);

            log.info("定时任务提交执行成功: taskId={}", taskId);

        } catch (Exception e) {
            log.error("定时任务执行失败: taskId={}", taskId, e);

            // 更新失败统计
            ScheduledTask task = taskMapper.selectById(taskId);
            if (task != null) {
                task.setFailedExecutions(task.getFailedExecutions() != null ? task.getFailedExecutions() + 1 : 1);
                task.setLastExecutionTime(LocalDateTime.now());
                task.setLastExecutionStatus("FAILED");
                taskMapper.updateById(task);
            }

            throw new JobExecutionException(e);
        }
    }

    /**
     * 将任务类型映射为执行类型
     */
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
}

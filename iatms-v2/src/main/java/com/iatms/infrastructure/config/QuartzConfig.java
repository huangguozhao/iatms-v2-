package com.iatms.infrastructure.config;

import com.iatms.infrastructure.job.TestExecutionJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 定时任务配置
 */
@Configuration
public class QuartzConfig {

    /**
     * 测试执行 Job Detail
     */
    @Bean
    public JobDetail testExecutionJobDetail() {
        return JobBuilder.newJob(TestExecutionJob.class)
                .withIdentity("testExecutionJob")
                .storeDurably()
                .build();
    }

    /**
     * 测试执行 Trigger (示例触发器，已禁用)
     * 如需测试可手动调用 scheduler.scheduleJob()
     */
    // @Bean
    public Trigger testExecutionTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(10)
                .withRepeatCount(0);

        return TriggerBuilder.newTrigger()
                .forJob(testExecutionJobDetail())
                .withIdentity("testExecutionTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    /**
     * 创建定时任务的 Job Detail
     * 注意：只能存储基本类型和 String，Spring Bean 需要通过 SpringContextHolder 获取
     */
    public static JobDetail createTaskJobDetail(Long taskId, String cronExpression) {
        return JobBuilder.newJob(TestExecutionJob.class)
                .usingJobData("taskId", String.valueOf(taskId))
                .usingJobData("cronExpression", cronExpression)
                .withIdentity("taskJob-" + taskId)
                .storeDurably()
                .build();
    }

    /**
     * 创建 Cron Trigger
     */
    public static CronTrigger createCronTrigger(Long taskId, String cronExpression) {
        return TriggerBuilder.newTrigger()
                .forJob(createTaskJobDetail(taskId, cronExpression))
                .withIdentity("taskTrigger-" + taskId)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }
}

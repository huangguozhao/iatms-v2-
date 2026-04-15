package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 定时任务
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scheduledtesttasks")
public class ScheduledTask extends BaseEntity {

    /**
     * 任务ID
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型：single_case, module, project, test_suite, api
     */
    private String taskType;

    /**
     * 目标ID
     */
    private Integer targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 用例ID列表（JSON格式）
     */
    private String caseIds;

    /**
     * 触发类型：cron, simple, daily, weekly, monthly
     */
    private String triggerType;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 简单重复间隔（毫秒）
     */
    private Long simpleRepeatInterval;

    /**
     * 简单重复次数（-1表示无限）
     */
    private Integer simpleRepeatCount;

    /**
     * 每日执行时刻-小时
     */
    private Integer dailyHour;

    /**
     * 每日执行时刻-分钟
     */
    private Integer dailyMinute;

    /**
     * 每周执行的天数
     */
    private String weeklyDays;

    /**
     * 每月执行的日期
     */
    private Integer monthlyDay;

    /**
     * 执行环境：dev, test, prod, staging
     */
    private String executionEnvironment;

    /**
     * 基础URL覆盖
     */
    private String baseUrl;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 并发数
     */
    private Integer concurrency;

    /**
     * 执行策略：sequential, parallel, by_module, smart
     */
    private String executionStrategy;

    /**
     * 是否启用重试
     */
    private Boolean retryEnabled;

    /**
     * 最大重试次数
     */
    private Integer maxRetryAttempts;

    /**
     * 重试延迟（毫秒）
     */
    private Integer retryDelayMs;

    /**
     * 成功时通知
     */
    private Boolean notifyOnSuccess;

    /**
     * 失败时通知
     */
    private Boolean notifyOnFailure;

    /**
     * 通知接收者
     */
    private String notificationRecipients;

    /**
     * 前次失败时跳过
     */
    private Boolean skipIfPreviousFailed;

    /**
     * 最大执行时长（秒）
     */
    private Integer maxDurationSeconds;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerTime;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecutionTime;

    /**
     * 最后执行状态：SUCCESS, FAILURE, ERROR
     */
    private String lastExecutionStatus;

    /**
     * 总执行次数
     */
    private Integer totalExecutions;

    /**
     * 成功执行次数
     */
    private Integer successfulExecutions;

    /**
     * 失败执行次数
     */
    private Integer failedExecutions;

    /**
     * 创建人ID
     */
    private Long createdBy;

    // ========== 业务方法 ==========

    /**
     * 计算下次运行时间
     */
    public LocalDateTime calculateNextRun() {
        LocalDateTime now = LocalDateTime.now();

        if (triggerType == null) {
            return null;
        }

        switch (triggerType.toLowerCase()) {
            case "cron":
                return calculateCronNextRun(now);
            case "simple":
                return calculateSimpleNextRun(now);
            case "daily":
                return calculateDailyNextRun(now);
            default:
                return null;
        }
    }

    private LocalDateTime calculateCronNextRun(LocalDateTime now) {
        if (cronExpression == null || cronExpression.isEmpty()) {
            return null;
        }
        // 简化的CRON计算 - 返回1小时后的固定时间
        try {
            return now.plusHours(1).withMinute(0).withSecond(0);
        } catch (Exception e) {
            log.error("Failed to calculate next run for cron: {}", cronExpression, e);
            return null;
        }
    }

    private LocalDateTime calculateSimpleNextRun(LocalDateTime now) {
        if (simpleRepeatInterval == null || simpleRepeatInterval <= 0) {
            return null;
        }
        return now.plusNanos(simpleRepeatInterval * 1_000_000);
    }

    private LocalDateTime calculateDailyNextRun(LocalDateTime now) {
        if (dailyHour == null || dailyMinute == null) {
            return null;
        }
        LocalDateTime next = now.withHour(dailyHour).withMinute(dailyMinute).withSecond(0).withNano(0);
        if (next.isBefore(now) || next.isEqual(now)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * 检查是否应该立即执行
     */
    public boolean shouldRunNow() {
        if (isEnabled == null || !isEnabled) {
            return false;
        }
        if (nextTriggerTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(nextTriggerTime) || LocalDateTime.now().isEqual(nextTriggerTime);
    }

    /**
     * 记录执行结果
     */
    public void recordRun(boolean success) {
        this.lastExecutionTime = LocalDateTime.now();
        this.nextTriggerTime = calculateNextRun();
    }
}

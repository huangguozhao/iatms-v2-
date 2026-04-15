package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试执行记录
 * 对应数据库表: testexecutionrecords
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("testexecutionrecords")
public class TestExecution extends BaseEntity {

    /**
     * 记录ID (record_id)
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long id;

    /**
     * 执行ID字符串，用于外部引用
     */
    private String executionId;

    /**
     * 执行范围类型：api/module/project/test_suite/test_case
     */
    private String executionScope;

    /**
     * 关联的ID（根据execution_scope关联对应表）
     */
    private Integer refId;

    /**
     * 执行范围的名称
     */
    private String scopeName;

    /**
     * 执行人ID
     */
    private Integer executedBy;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 执行类型：manual/scheduled/triggered
     */
    private String executionType;

    /**
     * 测试环境
     */
    private String environment;

    /**
     * 执行状态：running/completed/failed/cancelled
     */
    private String status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时(秒)
     */
    private Integer durationSeconds;

    /**
     * 总用例数
     */
    private Integer totalCases;

    /**
     * 失败用例数
     */
    private Integer failedCases;

    /**
     * 成功率
     */
    private BigDecimal successRate;

    /**
     * 执行配置信息(JSON)
     */
    private String executionConfig;

    /**
     * 报告访问地址
     */
    private String reportUrl;

    /**
     * 日志文件路径
     */
    private String logFilePath;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 触发任务ID
     */
    private Long triggeredTaskId;

    // ========== 内部使用的字段（不对应数据库持久化）==========

    /**
     * 进度百分比（运行时计算，不存储到数据库）
     */
    @TableField(exist = false)
    private Integer progress;

    /**
     * 完成用例数（运行时计算，不存储）
     */
    @TableField(exist = false)
    private Integer completedCases;

    /**
     * 通过用例数（运行时计算，不存储）
     */
    @TableField(exist = false)
    private Integer passedCases;

    /**
     * 跳过用例数（运行时计算，不存储）
     */
    @TableField(exist = false)
    private Integer skippedCases;

    // ========== 兼容性别名（用于旧代码）==========

    public String getTriggerType() {
        return this.executionType;
    }

    public void setTriggerType(String triggerType) {
        this.executionType = triggerType;
    }

    public Long getExecutedBy() {
        return this.executedBy != null ? this.executedBy.longValue() : null;
    }

    public void setExecutedBy(Long executedBy) {
        this.executedBy = executedBy != null ? executedBy.intValue() : null;
    }

    public Integer getDuration() {
        return this.durationSeconds;
    }

    public void setDuration(Integer duration) {
        this.durationSeconds = duration;
    }

    public LocalDateTime getStartedAt() {
        return this.startTime;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startTime = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return this.endTime;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.endTime = completedAt;
    }

    public Long getCreatedBy() {
        return super.getCreatedBy();
    }

    public void setCreatedBy(Long createdBy) {
        super.setCreatedBy(createdBy);
    }
}

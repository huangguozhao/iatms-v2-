package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 测试执行记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_execution")
public class TestExecution extends BaseEntity {

    /**
     * 执行类型：TEST_CASE-单个用例，TEST_SUITE-套件，PROJECT-项目
     */
    private String executionType;

    /**
     * 执行对象ID（用例/套件/项目ID）
     */
    private Long targetId;

    /**
     * 执行批次ID（同一批次执行共享）
     */
    private String executionId;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 进度百分比
     */
    private Integer progress;

    /**
     * 总用例数
     */
    private Integer totalCases;

    /**
     * 已完成用例数
     */
    private Integer completedCases;

    /**
     * 通过用例数
     */
    private Integer passedCases;

    /**
     * 失败用例数
     */
    private Integer failedCases;

    /**
     * 执行环境ID
     */
    private Long environmentId;

    /**
     * 触发类型：MANUAL-手动，SCHEDULED-定时，API-API触发
     */
    private String triggerType;

    /**
     * 触发任务的ID
     */
    private Long scheduledTaskId;

    /**
     * 执行人ID
     */
    private Long executedBy;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime completedAt;

    /**
     * 执行时长（秒）
     */
    private Integer duration;

    /**
     * 错误信息
     */
    private String errorMessage;
}

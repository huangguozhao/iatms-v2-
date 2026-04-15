package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 测试结果明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_result")
public class TestResult extends BaseEntity {

    /**
     * 执行批次ID
     */
    private String executionId;

    /**
     * 用例ID
     */
    private Long caseId;

    /**
     * 用例名称
     */
    private String caseName;

    /**
     * 用例编号
     */
    private String caseCode;

    /**
     * 执行状态：PASSED, FAILED, SKIPPED, ERROR
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTime;

    /**
     * 请求数据快照（JSON）
     */
    private String requestData;

    /**
     * 响应数据快照（JSON）
     */
    private String responseData;

    /**
     * 断言结果（JSON）
     */
    private String assertionResults;

    /**
     * 提取的变量（JSON）
     */
    private String extractedVariables;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行顺序
     */
    private Integer executionOrder;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime completedAt;
}

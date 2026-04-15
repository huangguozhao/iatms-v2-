package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 测试用例结果
 * 对应数据库表: testcaseresults
 * 采用Allure报告风格的表结构设计
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("testcaseresults")
public class TestResult extends BaseEntity {

    /**
     * 结果ID
     */
    @TableId(value = "result_id", type = IdType.AUTO)
    private Long id;

    /**
     * 测试执行记录ID（关联testexecutionrecords表）
     */
    private Long executionRecordId;

    /**
     * 外部引用执行ID字符串
     */
    private String executionIdStr;

    /**
     * 外键，关联TestReportSummaries表
     */
    private Long reportId;

    /**
     * 执行记录ID
     */
    private Long executionId;

    /**
     * 任务类型：test_suite/test_case/project/module/api_monitor
     */
    private String taskType;

    /**
     * 关联的ID（根据task_type关联对应表）
     */
    private Integer refId;

    /**
     * Allure中的完整名称（包含路径）
     */
    private String fullName;

    /**
     * 执行状态：passed/failed/broken/skipped/unknown
     */
    private String status;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 用例开始时间
     */
    private LocalDateTime startTime;

    /**
     * 用例结束时间
     */
    private LocalDateTime endTime;

    /**
     * 失败信息
     */
    private String failureMessage;

    /**
     * 失败堆栈跟踪
     */
    private String failureTrace;

    /**
     * 失败类型
     */
    private String failureType;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 测试步骤执行详情(JSON)
     */
    private String stepsJson;

    /**
     * 测试参数信息(JSON)
     */
    private String parametersJson;

    /**
     * 附件信息(JSON)
     */
    private String attachmentsJson;

    /**
     * 日志链接
     */
    private String logsLink;

    /**
     * 截图链接
     */
    private String screenshotLink;

    /**
     * 视频录制链接
     */
    private String videoLink;

    /**
     * 执行环境
     */
    private String environment;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 设备信息
     */
    private String device;

    /**
     * 标签信息(JSON)
     */
    private String tagsJson;

    /**
     * 严重程度：blocker/critical/high/normal/minor/trivial
     */
    private String severity;

    /**
     * 优先级：P0/P1/P2/P3
     */
    private String priority;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否不稳定用例
     */
    private Boolean flaky;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 测试层级：UNIT/INTEGRATION/API/E2E/PERFORMANCE/SECURITY
     */
    private String testLayer;

    /**
     * 测试类型：POSITIVE/NEGATIVE/BOUNDARY/SECURITY/PERFORMANCE/USABILITY
     */
    private String testType;

    /**
     * 不稳定次数
     */
    private Integer flakyCount;

    /**
     * 最后一次不稳定时间
     */
    private LocalDateTime lastFlakyTime;

    /**
     * 历史趋势数据(JSON)
     */
    private String historyTrend;

    /**
     * 自定义标签(JSON)
     */
    private String customLabels;

    /**
     * 根因分析
     */
    private String rootCauseAnalysis;

    /**
     * 影响评估：HIGH/MEDIUM/LOW
     */
    private String impactAssessment;

    /**
     * 复测结果：PASSED/FAILED/NOT_RETESTED
     */
    private String retestResult;

    /**
     * 复测备注
     */
    private String retestNotes;

    /**
     * 关联TestCases
     */
    private Integer caseId;

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * 用例名称（内部使用，从testcases表关联获取或通过caseId查询）
     */
    @TableField(exist = false)
    private String caseName;

    /**
     * 用例编码（内部使用）
     */
    @TableField(exist = false)
    private String caseCode;

    /**
     * 响应时间（内部使用，对应duration字段）
     */
    @TableField(exist = false)
    private Integer responseTime;

    /**
     * 响应数据（内部使用，不存储到数据库）
     */
    @TableField(exist = false)
    private String responseData;

    /**
     * 断言结果JSON（内部使用，存储到stepsJson）
     */
    @TableField(exist = false)
    private String assertionResults;

    /**
     * 提取的变量JSON（内部使用）
     */
    @TableField(exist = false)
    private String extractedVariables;

    /**
     * 请求数据快照（内部使用，存储到stepsJson）
     */
    @TableField(exist = false)
    private String requestData;

    // ========== 兼容性别名方法 ==========

    public Integer getResponseTime() {
        return this.duration != null ? this.duration.intValue() : null;
    }

    public void setResponseTime(Integer responseTime) {
        this.duration = responseTime != null ? responseTime.longValue() : null;
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

    public String getErrorMessage() {
        return this.failureMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.failureMessage = errorMessage;
    }
}

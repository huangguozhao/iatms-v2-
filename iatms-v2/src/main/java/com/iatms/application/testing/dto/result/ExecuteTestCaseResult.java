package com.iatms.application.testing.dto.result;

import lombok.Data;

/**
 * 执行测试用例结果
 */
@Data
public class ExecuteTestCaseResult {

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 执行状态：passed, failed, running
     */
    private String status;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 执行耗时(毫秒)
     */
    private Long duration;

    /**
     * 通过的断言数
     */
    private Integer assertionsPassed;

    /**
     * 失败的断言数
     */
    private Integer assertionsFailed;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 执行人
     */
    private String executor;

    /**
     * 错误信息
     */
    private String errorMessage;
}

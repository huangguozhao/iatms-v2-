package com.iatms.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近活动 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityVO {

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 执行范围类型：api/module/project/test_suite/test_case
     */
    private String scope;

    /**
     * 关联名称（如项目名、用例名等）
     */
    private String refName;

    /**
     * 执行状态：running/completed/failed/cancelled
     */
    private String status;

    /**
     * 执行类型：manual/scheduled/triggered
     */
    private String executionType;

    /**
     * 执行人ID
     */
    private Long executedBy;

    /**
     * 执行人名称
     */
    private String executedByName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 总用例数
     */
    private Integer totalCases;

    /**
     * 失败用例数
     */
    private Integer failedCases;
}

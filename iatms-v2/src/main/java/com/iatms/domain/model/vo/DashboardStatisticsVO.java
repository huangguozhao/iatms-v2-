package com.iatms.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsVO {

    /**
     * 项目总数
     */
    private Long projectCount;

    /**
     * API 总数
     */
    private Long apiCount;

    /**
     * 测试用例总数
     */
    private Long testCaseCount;

    /**
     * 测试执行总数
     */
    private Long executionCount;

    /**
     * 今日执行次数
     */
    private Long todayExecutionCount;

    /**
     * 本周执行次数
     */
    private Long weekExecutionCount;

    /**
     * 成功率
     */
    private Double successRate;
}

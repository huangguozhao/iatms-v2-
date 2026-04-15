package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务概要 VO
 */
@Data
@Builder
public class ScheduledTaskSummaryVO {

    private Long id;
    private String name;
    private String taskType;
    private String triggerType;
    private String status;
    private LocalDateTime nextRunTime;
    private Integer totalRuns;
    private Integer successRuns;
    private Integer failedRuns;
    private LocalDateTime createdAt;
}

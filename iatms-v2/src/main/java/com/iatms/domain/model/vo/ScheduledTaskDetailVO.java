package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务详情 VO
 */
@Data
@Builder
public class ScheduledTaskDetailVO {

    private Long id;
    private String name;
    private String description;
    private String taskType;
    private String triggerType;
    private String cronExpression;
    private Integer intervalSeconds;
    private LocalDateTime executeAt;
    private Long targetId;
    private String targetName;
    private Long environmentId;
    private String environmentName;
    private String status;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private Integer totalRuns;
    private Integer successRuns;
    private Integer failedRuns;
    private Boolean notifyOnSuccess;
    private Boolean notifyOnFailure;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String creatorName;
}

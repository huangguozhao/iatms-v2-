package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行进度 VO
 */
@Data
@Builder
public class ExecutionProgressVO {

    private Long executionId;
    private String executionIdStr;
    private String status;
    private Integer progress;
    private Integer totalCases;
    private Integer completedCases;
    private Integer passedCases;
    private Integer failedCases;
    private Integer skippedCases;
    private String currentCaseName;
    private Integer duration;
    private LocalDateTime startedAt;
    private LocalDateTime estimatedEndTime;
    private String errorMessage;
}

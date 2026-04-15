package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目概要 VO（列表页用）
 */
@Data
@Builder
public class ProjectSummaryVO {

    private Long id;
    private String name;
    private String code;
    private String projectType;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ownerId;
    private String ownerName;
    private String iconColor;

    private Integer moduleCount;
    private Integer apiCount;
    private Integer testCaseCount;

    private LocalDateTime createdAt;
}

package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试套件详情 VO
 */
@Data
@Builder
public class TestSuiteDetailVO {

    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private String projectName;
    private Long environmentId;
    private String environmentName;
    private String status;
    private List<ApiSummaryVO> requests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String creatorName;
}

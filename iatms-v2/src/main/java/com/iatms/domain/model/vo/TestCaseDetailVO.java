package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试用例详情 VO
 */
@Data
@Builder
public class TestCaseDetailVO {

    private Long id;
    private String caseCode;
    private String name;
    private String description;
    private Long projectId;
    private String projectName;
    private Long moduleId;
    private String moduleName;
    private Long apiId;
    private ApiSummaryVO api;
    private String testType;
    private String priority;
    private String severity;
    private String tags;
    private String status;
    private String preconditions;
    private String testSteps;
    private String testData;
    private String headers;
    private String requestParams;
    private String requestBody;
    private String assertions;
    private String expectedResponse;
    private String extractors;
    private Integer expectedHttpStatus;
    private String expectedResponseSchema;
    private String expectedResponseBody;
    private String requestOverride;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String creatorName;
}

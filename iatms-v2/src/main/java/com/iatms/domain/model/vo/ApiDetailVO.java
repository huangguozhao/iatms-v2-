package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 详情 VO
 */
@Data
@Builder
public class ApiDetailVO {

    private Long id;
    private String name;
    private String description;
    private String requestType;
    private String httpMethod;
    private String url;
    private String baseUrl;
    private String headers;
    private String queryParams;
    private String requestBody;
    private String authConfig;
    private Long collectionId;
    private String collectionName;
    private Long projectId;
    private String projectName;
    private Long moduleId;
    private String moduleName;
    private Integer orderNum;
    private String status;
    private String requestBodyType;
    private String responseBodyType;
    private String tags;
    private Integer timeoutSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String creatorName;
}

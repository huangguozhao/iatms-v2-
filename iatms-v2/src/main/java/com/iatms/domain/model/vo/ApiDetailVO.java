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
    private String headers;
    private String queryParams;
    private String requestBody;
    private String authConfig;
    private String preScript;
    private String postScript;
    private String assertions;
    private Long collectionId;
    private String collectionName;
    private Long projectId;
    private String projectName;
    private Integer orderNum;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String creatorName;
}

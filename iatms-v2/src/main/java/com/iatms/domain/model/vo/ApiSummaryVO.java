package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 概要 VO
 */
@Data
@Builder
public class ApiSummaryVO {

    private Long id;
    private String name;
    private String httpMethod;
    private String url;
    private Long collectionId;
    private String collectionName;
    private String status;
    private LocalDateTime updatedAt;
}

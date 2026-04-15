package com.iatms.application.testing.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * API 查询条件
 */
@Data
@Builder
public class ApiQuery {

    private Long projectId;

    private Long collectionId;

    private String keyword;

    private String httpMethod;

    private String status;

    private Long creatorId;

    @Builder.Default
    private Integer pageNum = 1;

    @Builder.Default
    private Integer pageSize = 20;
}

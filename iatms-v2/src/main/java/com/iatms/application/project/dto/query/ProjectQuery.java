package com.iatms.application.project.dto.query;

import lombok.Builder;
import lombok.Data;

/**
 * 项目查询条件
 */
@Data
@Builder
public class ProjectQuery {

    private String keyword;

    private String status;

    private String projectType;

    private Long ownerId;

    @Builder.Default
    private Integer pageNum = 1;

    @Builder.Default
    private Integer pageSize = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortOrder = "DESC";
}

package com.iatms.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模块详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDetailVO {

    private Long id;
    private String name;
    private String code;
    private Long projectId;
    private String projectName;
    private Long parentId;
    private String parentName;
    private String description;
    private String status;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;

    /**
     * 统计信息
     */
    private Stats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private Integer apiCount;
        private Integer testCaseCount;
        private Integer passedCount;
        private Integer failedCount;
        private Integer notExecutedCount;
    }
}

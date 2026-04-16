package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情 VO
 */
@Data
@Builder
public class ProjectDetailVO {

    private Long id;
    private String name;
    private String code;
    private String projectType;
    private String status;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ownerId;
    private String ownerName;
    private String iconColor;

    private List<ProjectMemberVO> members;

    private Integer totalModules;
    private Integer totalApis;
    private Integer totalTestCases;
    private Integer passedCount;
    private Integer failedCount;
    private Integer notExecutedCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;

    /**
     * 项目成员 VO
     */
    @Data
    @Builder
    public static class ProjectMemberVO {
        private Long userId;
        private String userName;
        private String displayName;
        private String avatar;
        private String role;
        private LocalDateTime joinedAt;
    }
}

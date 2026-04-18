package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户摘要 VO
 */
@Data
@Builder
public class UserSummaryVO {

    private Long userId;
    private String name;
    private String email;
    private String avatarUrl;
    private String phone;
    private String position;
    private Integer departmentId;
    private String departmentName;
    private String employeeId;
    private String status;
    private String role;
    private String createdAt;
    private String lastLoginTime;
}

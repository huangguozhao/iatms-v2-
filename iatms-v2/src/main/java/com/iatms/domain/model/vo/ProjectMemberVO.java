package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 项目成员 VO
 */
@Data
@Builder
public class ProjectMemberVO {

    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String role;
}

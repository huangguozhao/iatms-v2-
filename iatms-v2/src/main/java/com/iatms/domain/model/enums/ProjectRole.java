package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目角色枚举
 * 对应论文 3.2.1 用户权限管理功能 表3.1 角色权限表
 */
@Getter
@AllArgsConstructor
public enum ProjectRole {

    owner("owner", "项目负责人"),
    manager("manager", "项目管理员"),
    developer("developer", "开发人员"),
    tester("tester", "测试人员"),
    viewer("viewer", "查看者");

    private final String code;
    private final String description;

    /**
     * 根据角色码获取枚举
     */
    public static ProjectRole fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ProjectRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}

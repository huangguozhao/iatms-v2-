package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目级权限枚举
 * 对应论文 3.2.1 用户权限管理功能 的权限矩阵
 */
@Getter
@AllArgsConstructor
public enum ProjectPermission {

    // ==================== 项目级别 ====================
    PROJECT_VIEW("project:view", "查看项目"),
    PROJECT_EDIT("project:edit", "编辑项目信息"),
    PROJECT_DELETE("project:delete", "删除项目"),
    PROJECT_MANAGE_MEMBERS("project:manage_members", "管理项目成员"),

    // ==================== 模块级别 ====================
    MODULE_CREATE("module:create", "创建模块"),
    MODULE_EDIT("module:edit", "编辑模块"),
    MODULE_DELETE("module:delete", "删除模块"),

    // ==================== 接口级别 ====================
    API_CREATE("api:create", "创建接口"),
    API_EDIT("api:edit", "编辑接口"),
    API_DELETE("api:delete", "删除接口"),
    API_VIEW("api:view", "查看接口"),

    // ==================== 用例级别 ====================
    CASE_CREATE("case:create", "创建用例"),
    CASE_EDIT("case:edit", "编辑用例"),
    CASE_DELETE("case:delete", "删除用例"),
    CASE_EXECUTE("case:execute", "执行用例"),
    CASE_VIEW("case:view", "查看用例"),

    // ==================== 套件级别 ====================
    SUITE_CREATE("suite:create", "创建测试套件"),
    SUITE_EDIT("suite:edit", "编辑测试套件"),
    SUITE_DELETE("suite:delete", "删除测试套件"),
    SUITE_EXECUTE("suite:execute", "执行测试套件"),
    SUITE_VIEW("suite:view", "查看测试套件"),

    // ==================== 定时任务级别 ====================
    TASK_CREATE("task:create", "创建定时任务"),
    TASK_EDIT("task:edit", "编辑定时任务"),
    TASK_DELETE("task:delete", "删除定时任务"),
    TASK_EXECUTE("task:execute", "立即执行任务"),
    TASK_VIEW("task:view", "查看定时任务"),

    // ==================== 报告级别 ====================
    REPORT_VIEW("report:view", "查看报告"),
    REPORT_EXPORT("report:export", "导出报告"),

    // ==================== AI诊断 ====================
    AI_DIAGNOSE("ai:diagnose", "AI智能诊断");

    private final String code;
    private final String description;

    /**
     * 角色权限映射
     * 按照论文表3.1 角色权限表
     */
    public static final Set<ProjectPermission> OWNER_PERMISSIONS = Stream.of(values())
            .collect(Collectors.toSet());

    public static final Set<ProjectPermission> MANAGER_PERMISSIONS = Stream.of(
            PROJECT_VIEW, PROJECT_EDIT, PROJECT_MANAGE_MEMBERS,
            MODULE_CREATE, MODULE_EDIT, MODULE_DELETE,
            API_CREATE, API_EDIT, API_DELETE, API_VIEW,
            CASE_CREATE, CASE_EDIT, CASE_DELETE, CASE_VIEW,
            SUITE_CREATE, SUITE_EDIT, SUITE_DELETE, SUITE_VIEW, SUITE_EXECUTE,
            TASK_CREATE, TASK_EDIT, TASK_DELETE, TASK_EXECUTE, TASK_VIEW,
            REPORT_VIEW, REPORT_EXPORT,
            AI_DIAGNOSE
    ).collect(Collectors.toSet());

    public static final Set<ProjectPermission> DEVELOPER_PERMISSIONS = Stream.of(
            PROJECT_VIEW,
            MODULE_CREATE, MODULE_EDIT, MODULE_DELETE,
            API_CREATE, API_EDIT, API_DELETE, API_VIEW,
            CASE_CREATE, CASE_EDIT, CASE_VIEW,
            SUITE_CREATE, SUITE_EDIT, SUITE_VIEW,
            TASK_CREATE, TASK_EDIT, TASK_EXECUTE, TASK_VIEW,
            REPORT_VIEW,
            AI_DIAGNOSE
    ).collect(Collectors.toSet());

    public static final Set<ProjectPermission> TESTER_PERMISSIONS = Stream.of(
            PROJECT_VIEW,
            CASE_CREATE, CASE_EDIT, CASE_EXECUTE, CASE_VIEW,
            SUITE_CREATE, SUITE_EDIT, SUITE_VIEW, SUITE_EXECUTE,
            TASK_CREATE, TASK_EDIT, TASK_EXECUTE, TASK_VIEW,
            REPORT_VIEW,
            AI_DIAGNOSE
    ).collect(Collectors.toSet());

    public static final Set<ProjectPermission> VIEWER_PERMISSIONS = Stream.of(
            PROJECT_VIEW,
            REPORT_VIEW
    ).collect(Collectors.toSet());

    /**
     * 根据角色获取权限集合
     */
    public static Set<ProjectPermission> getPermissionsByRole(ProjectRole role) {
        if (role == null) {
            return Set.of();
        }
        return switch (role) {
            case owner -> OWNER_PERMISSIONS;
            case manager -> MANAGER_PERMISSIONS;
            case developer -> DEVELOPER_PERMISSIONS;
            case tester -> TESTER_PERMISSIONS;
            case viewer -> VIEWER_PERMISSIONS;
        };
    }

    /**
     * 根据权限码获取权限枚举
     */
    public static ProjectPermission fromCode(String code) {
        for (ProjectPermission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        return null;
    }
}

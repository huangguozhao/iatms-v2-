package com.iatms.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    UNKNOWN_ERROR("E0000", "未知错误"),
    INVALID_PARAMETER("E0001", "参数无效"),
    RESOURCE_NOT_FOUND("E0002", "资源不存在"),
    UNAUTHORIZED("E0003", "未认证"),
    FORBIDDEN("E0004", "权限不足"),

    // 项目模块
    PROJECT_NOT_FOUND("P0001", "项目不存在"),
    PROJECT_CODE_EXISTS("P0002", "项目编号已存在"),
    PROJECT_NAME_EXISTS("P0003", "项目名称已存在"),

    // 模块
    MODULE_NOT_FOUND("M0001", "模块不存在"),
    MODULE_HAS_CHILDREN("M0002", "模块包含子模块，无法删除"),

    // 接口
    API_NOT_FOUND("A0001", "接口不存在"),
    API_NAME_EXISTS("A0002", "接口名称已存在"),
    API_PATH_EXISTS("A0003", "接口路径已存在"),

    // 测试用例
    TEST_CASE_NOT_FOUND("T0001", "测试用例不存在"),
    TEST_CASE_CODE_EXISTS("T0002", "用例编号已存在"),

    // 测试套件
    TEST_SUITE_NOT_FOUND("S0001", "测试套件不存在"),
    TEST_SUITE_NAME_EXISTS("S0002", "套件名称已存在"),

    // 执行
    EXECUTION_NOT_FOUND("E0001", "执行记录不存在"),
    EXECUTION_IN_PROGRESS("E0002", "执行正在进行中"),
    EXECUTION_CANCELLED("E0003", "执行已取消"),

    // 定时任务
    TASK_NOT_FOUND("K0001", "定时任务不存在"),
    TASK_STATUS_ERROR("K0002", "任务状态错误"),

    // 用户
    USER_NOT_FOUND("U0001", "用户不存在"),
    USER_NAME_EXISTS("U0002", "用户名已存在"),
    USER_EMAIL_EXISTS("U0003", "邮箱已存在"),
    INVALID_CREDENTIALS("U0004", "用户名或密码错误"),

    // 角色
    ROLE_NOT_FOUND("R0001", "角色不存在"),
    ROLE_HAS_USERS("R0002", "角色已分配给用户，无法删除"),

    // AI 服务
    AI_SERVICE_NOT_FOUND("AI001", "AI 服务配置不存在"),
    AI_SERVICE_UNAVAILABLE("AI002", "AI 服务不可用");

    private final String code;
    private final String message;
}

package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试用例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_cases")
public class TestCase extends BaseEntity {

    /**
     * 用例编号
     */
    private String caseCode;

    /**
     * 用例名称
     */
    private String name;

    /**
     * 用例描述
     */
    private String description;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 所属模块ID
     */
    private Long moduleId;

    /**
     * 关联的接口ID
     */
    private Long apiId;

    /**
     * 请求方法（GET, POST等）
     */
    private String method;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 前置条件（JSON格式）
     */
    private String preconditions;

    /**
     * 测试步骤（JSON格式）
     */
    private String testSteps;

    /**
     * 测试数据（JSON格式）
     */
    private String testData;

    /**
     * 请求头（JSON格式）
     */
    private String headers;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 请求体（JSON格式）
     */
    private String requestBody;

    /**
     * 断言规则（JSON格式）
     */
    private String assertions;

    /**
     * 预期响应（JSON格式）
     */
    private String expectedResponse;

    /**
     * 变量提取规则（JSON格式）
     */
    private String extractors;

    /**
     * 优先级：P0, P1, P2, P3
     */
    private String priority;

    /**
     * 状态：DRAFT-草稿，ACTIVE-激活，DEPRECATED-废弃
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 测试类型：FUNCTIONAL-功能，PERFORMANCE-性能，SECURITY-安全
     */
    private String testType;
}

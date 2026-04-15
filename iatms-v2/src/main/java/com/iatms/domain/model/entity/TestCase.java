package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试用例
 * 对应数据库表: testcases
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("testcases")
public class TestCase extends BaseEntity {

    /**
     * 用例ID
     */
    @TableId(value = "case_id", type = IdType.AUTO)
    private Long id;

    /**
     * 用例编号
     */
    private String caseCode;

    /**
     * 关联的接口ID
     */
    private Integer apiId;

    /**
     * 用例名称
     */
    private String name;

    /**
     * 用例描述
     */
    private String description;

    /**
     * 测试类型：functional, performance, security, compatibility, smoke, regression
     */
    private String testType;

    /**
     * 优先级：P0, P1, P2, P3
     */
    private String priority;

    /**
     * 严重程度：critical, high, medium, low
     */
    private String severity;

    /**
     * 标签（JSON格式）
     */
    private String tags;

    /**
     * 前置条件（JSON格式）
     */
    private String preconditions;

    /**
     * 测试步骤（JSON格式）
     */
    private String testSteps;

    /**
     * 请求参数覆盖（JSON格式）
     */
    private String requestOverride;

    /**
     * 预期HTTP状态码
     */
    private Integer expectedHttpStatus;

    /**
     * 预期响应Schema（JSON格式）
     */
    private String expectedResponseSchema;

    /**
     * 预期响应体
     */
    private String expectedResponseBody;

    /**
     * 断言规则（JSON格式）
     */
    private String assertions;

    /**
     * 响应提取规则（JSON格式）
     */
    private String extractors;

    /**
     * 验证器配置（JSON格式）
     */
    private String validators;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 是否为模板用例
     */
    private Boolean isTemplate;

    /**
     * 模板用例ID
     */
    private Integer templateId;

    /**
     * 版本号（来自数据库的version字段）
     */
    @TableField("version")
    private String caseVersion;

    // ========== 兼容字段（数据库中不存在，但代码需要使用）==========

    /**
     * 项目ID（数据库中不存在，通过api间接关联）
     */
    @TableField(exist = false)
    private Long projectId;

    /**
     * 模块ID（数据库中不存在，通过api间接关联）
     */
    @TableField(exist = false)
    private Long moduleId;

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * 用例ID别名
     */
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 请求方法（兼容旧代码，数据库中没有此字段）
     */
    @TableField(exist = false)
    private String method;

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 请求路径（兼容旧代码，数据库中没有此字段）
     */
    @TableField(exist = false)
    private String path;

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 测试数据（兼容旧代码）
     */
    @TableField(exist = false)
    private String testData;

    public String getTestData() {
        return this.testData;
    }

    public void setTestData(String testData) {
        this.testData = testData;
    }

    /**
     * 请求头（兼容旧代码）
     */
    @TableField(exist = false)
    private String headers;

    public String getHeaders() {
        return this.headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    /**
     * 请求参数（兼容旧代码）
     */
    @TableField(exist = false)
    private String requestParams;

    public String getRequestParams() {
        return this.requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    /**
     * 请求体（兼容旧代码）
     */
    @TableField(exist = false)
    private String requestBody;

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * 预期响应（兼容旧代码）
     */
    @TableField(exist = false)
    private String expectedResponse;

    public String getExpectedResponse() {
        return this.expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    /**
     * 状态（兼容旧代码）
     */
    @TableField(exist = false)
    private String status = "DRAFT";

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

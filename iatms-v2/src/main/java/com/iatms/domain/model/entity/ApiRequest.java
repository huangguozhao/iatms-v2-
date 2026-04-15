package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * API 接口定义
 * 对应数据库表: apis
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("apis")
public class ApiRequest extends BaseEntity {

    /**
     * 接口ID
     */
    @TableId(value = "api_id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 模块ID
     */
    private Integer moduleId;

    /**
     * 集合ID（兼容旧代码，实际指向module_id）
     * 用于ApiCollection概念的兼容
     */
    @TableField(exist = false)
    private Integer collectionId;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 接口路径
     */
    private String path;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 查询参数（JSON格式）
     */
    private String requestParameters;

    /**
     * 路径参数（JSON格式）
     */
    private String pathParameters;

    /**
     * 请求头信息（JSON格式）
     */
    private String requestHeaders;

    /**
     * 请求体内容
     */
    private String requestBody;

    /**
     * 请求体类型
     */
    private String requestBodyType;

    /**
     * 响应体类型
     */
    private String responseBodyType;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口状态
     */
    private String status;

    /**
     * 版本号
     */
    @TableField("version")
    private String apiVersion;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 认证配置（JSON）
     */
    private String authConfig;

    /**
     * 标签（JSON数组）
     */
    private String tags;

    /**
     * 请求示例（JSON）
     */
    private String examples;

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * HTTP 方法别名
     */
    @TableField(exist = false)
    private String httpMethod;

    public String getHttpMethod() {
        return this.method;
    }

    public void setHttpMethod(String httpMethod) {
        this.method = httpMethod;
    }

    /**
     * URL 别名（使用 path + base_url 组合）
     */
    @TableField(exist = false)
    private String url;

    /**
     * 请求头别名
     */
    @TableField(exist = false)
    private String headers;

    public String getHeaders() {
        return this.requestHeaders;
    }

    public void setHeaders(String headers) {
        this.requestHeaders = headers;
    }

    /**
     * 查询参数别名
     */
    @TableField(exist = false)
    private String queryParams;

    public String getQueryParams() {
        return this.requestParameters;
    }

    public void setQueryParams(String queryParams) {
        this.requestParameters = queryParams;
    }

    /**
     * 集合ID兼容方法 - 实际使用moduleId
     */
    public Integer getCollectionId() {
        return this.moduleId;
    }

    public void setCollectionId(Integer collectionId) {
        this.moduleId = collectionId;
    }

    // ========== 兼容性别名（用于旧代码）==========

    /**
     * 排序序号（兼容旧代码，数据库中不存在）
     */
    @TableField(exist = false)
    private Integer orderNum;

    public Integer getOrderNum() {
        return this.orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    /**
     * 请求类型别名（兼容旧代码）
     */
    @TableField(exist = false)
    private String requestType;

    public String getRequestType() {
        return this.requestBodyType;
    }

    public void setRequestType(String requestType) {
        this.requestBodyType = requestType;
    }

    /**
     * 前置脚本（兼容旧代码，数据库中不存在）
     */
    @TableField(exist = false)
    private String preScript;

    public String getPreScript() {
        return this.preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
    }

    /**
     * 后置脚本（兼容旧代码，数据库中不存在）
     */
    @TableField(exist = false)
    private String postScript;

    public String getPostScript() {
        return this.postScript;
    }

    public void setPostScript(String postScript) {
        this.postScript = postScript;
    }

    /**
     * 断言配置（兼容旧代码，数据库中不存在）
     */
    @TableField(exist = false)
    private String assertions;

    public String getAssertions() {
        return this.assertions;
    }

    public void setAssertions(String assertions) {
        this.assertions = assertions;
    }
}

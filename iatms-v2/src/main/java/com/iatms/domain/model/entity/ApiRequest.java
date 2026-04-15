package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API 请求定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_request")
public class ApiRequest extends BaseEntity {

    /**
     * 请求名称
     */
    private String name;

    /**
     * 请求描述
     */
    private String description;

    /**
     * 请求类型：HTTP, WEBSOCKET
     */
    private String requestType;

    /**
     * HTTP 方法
     */
    private String httpMethod;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 请求头（JSON格式）
     */
    private String headers;

    /**
     * URL参数（JSON格式）
     */
    private String queryParams;

    /**
     * 请求体（JSON格式）
     */
    private String requestBody;

    /**
     * 认证配置（JSON格式）
     */
    private String authConfig;

    /**
     * 前置脚本
     */
    private String preScript;

    /**
     * 后置脚本
     */
    private String postScript;

    /**
     * 断言规则（JSON格式）
     */
    private String assertions;

    /**
     * 所属集合ID
     */
    private Long collectionId;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 状态：DRAFT-草稿，ACTIVE-激活，DEPRECATED-废弃
     */
    private String status;
}

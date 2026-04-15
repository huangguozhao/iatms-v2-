package com.iatms.domain.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 服务配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_service_config")
public class AIServiceConfig extends BaseEntity {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 服务类型：openai, deepseek, qwen 等
     */
    private String serviceType;

    /**
     * 角色：doc_extractor, naming, mock_data, description
     */
    private String role;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API Base URL
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 最大 Token 数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建人ID
     */
    private Long createdBy;
}

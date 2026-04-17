package com.iatms.application.environment;

import com.iatms.domain.model.entity.EnvironmentConfig;

/**
 * 环境配置查询服务
 */
public interface EnvironmentQueryService {

    /**
     * 获取所有激活的环境列表
     */
    java.util.List<EnvironmentConfig> listActiveEnvironments();

    /**
     * 根据ID获取环境详情
     */
    EnvironmentConfig getEnvironmentById(Integer envId);

    /**
     * 获取默认环境
     */
    EnvironmentConfig getDefaultEnvironment();
}

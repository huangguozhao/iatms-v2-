package com.iatms.application.environment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.environment.EnvironmentQueryService;
import com.iatms.domain.model.entity.EnvironmentConfig;
import com.iatms.infrastructure.persistence.mapper.EnvironmentConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 环境配置查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentQueryServiceImpl implements EnvironmentQueryService {

    private final EnvironmentConfigMapper environmentConfigMapper;

    @Override
    public List<EnvironmentConfig> listActiveEnvironments() {
        LambdaQueryWrapper<EnvironmentConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvironmentConfig::getStatus, "active")
                    .orderByAsc(EnvironmentConfig::getEnvName);
        return environmentConfigMapper.selectList(queryWrapper);
    }

    @Override
    public EnvironmentConfig getEnvironmentById(Integer envId) {
        return environmentConfigMapper.selectById(envId);
    }

    @Override
    public EnvironmentConfig getDefaultEnvironment() {
        LambdaQueryWrapper<EnvironmentConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EnvironmentConfig::getIsDefault, true)
                    .eq(EnvironmentConfig::getStatus, "active");
        return environmentConfigMapper.selectOne(queryWrapper);
    }
}

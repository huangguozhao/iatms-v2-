package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.EnvironmentConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 环境配置 Mapper
 */
@Mapper
public interface EnvironmentConfigMapper extends BaseMapper<EnvironmentConfig> {
}

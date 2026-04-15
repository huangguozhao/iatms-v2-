package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.AIServiceConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 服务配置 Mapper
 */
@Mapper
public interface AIServiceConfigMapper extends BaseMapper<AIServiceConfig> {
}

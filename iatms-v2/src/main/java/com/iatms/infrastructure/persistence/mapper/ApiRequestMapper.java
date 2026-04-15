package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.ApiRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 请求 Mapper
 */
@Mapper
public interface ApiRequestMapper extends BaseMapper<ApiRequest> {
}

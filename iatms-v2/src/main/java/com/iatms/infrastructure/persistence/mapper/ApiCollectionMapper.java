package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.ApiCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 集合 Mapper
 */
@Mapper
public interface ApiCollectionMapper extends BaseMapper<ApiCollection> {
}

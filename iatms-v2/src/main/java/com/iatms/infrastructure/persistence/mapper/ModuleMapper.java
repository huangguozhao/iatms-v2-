package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.Module;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模块 Mapper
 */
@Mapper
public interface ModuleMapper extends BaseMapper<Module> {
}

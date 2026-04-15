package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestSuite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 测试套件 Mapper
 */
@Mapper
public interface TestSuiteMapper extends BaseMapper<TestSuite> {

    @Select("SELECT * FROM testsuites WHERE project_id = #{projectId} AND deleted = false ORDER BY created_at DESC")
    List<TestSuite> selectByProjectId(@Param("projectId") Long projectId);
}

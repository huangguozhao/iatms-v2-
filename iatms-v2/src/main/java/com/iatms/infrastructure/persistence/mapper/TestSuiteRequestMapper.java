package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestSuiteRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestSuiteRequestMapper extends BaseMapper<TestSuiteRequest> {

    @Select("SELECT * FROM suitecasemappings WHERE suite_id = #{suiteId}")
    List<TestSuiteRequest> selectByTestSuiteId(@Param("suiteId") Integer suiteId);

    @Select("SELECT * FROM suitecasemappings WHERE case_id = #{caseId}")
    List<TestSuiteRequest> selectByCaseId(@Param("caseId") Integer caseId);

    default void deleteByTestSuiteId(Integer suiteId) {
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TestSuiteRequest>()
                .eq(TestSuiteRequest::getSuiteId, suiteId));
    }
}

package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestSuiteRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestSuiteRequestMapper extends BaseMapper<TestSuiteRequest> {

    @Select("SELECT * FROM test_suite_request WHERE test_suite_id = #{testSuiteId} ORDER BY sort_order")
    List<TestSuiteRequest> selectByTestSuiteId(@Param("testSuiteId") Long testSuiteId);

    default void deleteByTestSuiteId(Long testSuiteId) {
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TestSuiteRequest>()
                .eq(TestSuiteRequest::getTestSuiteId, testSuiteId));
    }
}

package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestExecution;
import org.apache.ibatis.annotations.*;

/**
 * 测试执行 Mapper
 */
@Mapper
public interface TestExecutionMapper extends BaseMapper<TestExecution> {

    @Select("SELECT * FROM test_execution WHERE execution_id = #{executionId} LIMIT 1")
    TestExecution selectByExecutionId(@Param("executionId") String executionId);

    @Update("UPDATE test_execution SET status = #{status}, progress = #{progress}, " +
            "completed_cases = #{completedCases}, passed_cases = #{passedCases}, " +
            "failed_cases = #{failedCases}, error_message = #{errorMessage}, " +
            "updated_at = NOW() WHERE execution_id = #{executionId}")
    void updateByExecutionId(@Param("executionId") String executionId,
                              @Param("status") String status,
                              @Param("progress") Integer progress,
                              @Param("completedCases") Integer completedCases,
                              @Param("passedCases") Integer passedCases,
                              @Param("failedCases") Integer failedCases,
                              @Param("errorMessage") String errorMessage);

    @Update("UPDATE test_execution SET status = #{status}, error_message = #{errorMessage}, " +
            "completed_at = NOW(), updated_at = NOW() WHERE execution_id = #{executionId}")
    void updateStatusByExecutionId(@Param("executionId") String executionId,
                                    @Param("status") String status,
                                    @Param("errorMessage") String errorMessage);
}

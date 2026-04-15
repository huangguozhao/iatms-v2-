package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 测试结果 Mapper
 */
@Mapper
public interface TestResultMapper extends BaseMapper<TestResult> {

    @Select("SELECT * FROM test_result WHERE execution_id = #{executionId} AND case_id = #{caseId} LIMIT 1")
    TestResult selectByCaseAndExecution(@Param("executionId") String executionId, @Param("caseId") Long caseId);

    @Select("SELECT * FROM test_result WHERE execution_id = #{executionId} ORDER BY started_at ASC")
    List<TestResult> selectByExecutionId(@Param("executionId") String executionId);

    @Insert("INSERT INTO test_result (execution_id, case_id, case_name, case_code, status, " +
            "response_time, request_data, response_data, assertion_results, " +
            "extracted_variables, error_message, execution_order, started_at, completed_at, " +
            "created_at, updated_at, deleted) " +
            "VALUES (#{executionId}, #{caseId}, #{caseName}, #{caseCode}, #{status}, " +
            "#{responseTime}, #{requestData}, #{responseData}, #{assertionResults}, " +
            "#{extractedVariables}, #{errorMessage}, #{executionOrder}, #{startedAt}, #{completedAt}, " +
            "NOW(), NOW(), false)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertResult(TestResult result);

    @Update("UPDATE test_result SET status = #{status}, response_time = #{responseTime}, " +
            "response_data = #{responseData}, assertion_results = #{assertionResults}, " +
            "extracted_variables = #{extractedVariables}, error_message = #{errorMessage}, " +
            "completed_at = #{completedAt}, updated_at = NOW() " +
            "WHERE execution_id = #{executionId} AND case_id = #{caseId}")
    int updateByCaseAndExecution(TestResult result);
}

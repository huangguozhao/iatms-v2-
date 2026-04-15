package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 测试结果 Mapper
 * 对应数据库表: testcaseresults
 */
@Mapper
public interface TestResultMapper extends BaseMapper<TestResult> {

    /**
     * 通过执行记录ID查询结果列表
     */
    @Select("SELECT * FROM testcaseresults WHERE execution_record_id = #{executionRecordId} ORDER BY start_time ASC")
    List<TestResult> selectByExecutionRecordId(@Param("executionRecordId") Long executionRecordId);

    /**
     * 通过执行ID字符串查询结果列表
     */
    @Select("SELECT * FROM testcaseresults WHERE execution_id_str = #{executionId} ORDER BY start_time ASC")
    List<TestResult> selectByExecutionId(@Param("executionId") String executionId);

    /**
     * 通过caseId查询最新结果
     */
    @Select("SELECT * FROM testcaseresults WHERE case_id = #{caseId} ORDER BY created_at DESC LIMIT 1")
    TestResult selectLatestByCaseId(@Param("caseId") Integer caseId);

    /**
     * 插入测试结果
     */
    @Insert("INSERT INTO testcaseresults (" +
            "execution_record_id, execution_id_str, case_id, status, duration, start_time, end_time, " +
            "failure_message, failure_trace, failure_type, error_code, " +
            "steps_json, parameters_json, attachments_json, " +
            "environment, severity, priority, retry_count, flaky, " +
            "created_at, is_deleted" +
            ") VALUES (" +
            "#{executionRecordId}, #{executionIdStr}, #{caseId}, #{status}, #{duration}, #{startTime}, #{endTime}, " +
            "#{failureMessage}, #{failureTrace}, #{failureType}, #{errorCode}, " +
            "#{stepsJson}, #{parametersJson}, #{attachmentsJson}, " +
            "#{environment}, #{severity}, #{priority}, #{retryCount}, #{flaky}, " +
            "NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertResult(TestResult result);

    /**
     * 更新测试结果
     */
    @Update("UPDATE testcaseresults SET " +
            "status = #{status}, " +
            "duration = #{duration}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime}, " +
            "failure_message = #{failureMessage}, " +
            "failure_trace = #{failureTrace}, " +
            "failure_type = #{failureType}, " +
            "error_code = #{errorCode}, " +
            "steps_json = #{stepsJson}, " +
            "parameters_json = #{parametersJson}, " +
            "attachments_json = #{attachmentsJson}, " +
            "severity = #{severity}, " +
            "priority = #{priority}, " +
            "retry_count = #{retryCount}, " +
            "flaky = #{flaky}, " +
            "updated_at = NOW() " +
            "WHERE result_id = #{id}")
    int updateResult(TestResult result);

    /**
     * 统计特定执行ID和状态的结果数量
     */
    @Select("SELECT COUNT(*) FROM testcaseresults WHERE execution_id_str = #{executionId} AND status = #{status}")
    int countByExecutionIdAndStatus(@Param("executionId") String executionId, @Param("status") String status);

    /**
     * 统计特定执行记录ID和状态的结果数量
     */
    @Select("SELECT COUNT(*) FROM testcaseresults WHERE execution_record_id = #{executionRecordId} AND status = #{status}")
    int countByExecutionRecordIdAndStatus(@Param("executionRecordId") Long executionRecordId, @Param("status") String status);
}

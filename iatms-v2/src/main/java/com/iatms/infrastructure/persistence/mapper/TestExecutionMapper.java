package com.iatms.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iatms.domain.model.entity.TestExecution;
import org.apache.ibatis.annotations.*;

/**
 * 测试执行 Mapper
 * 对应数据库表: testexecutionrecords
 */
@Mapper
public interface TestExecutionMapper extends BaseMapper<TestExecution> {

    /**
     * 通过记录ID查询
     */
    @Select("SELECT * FROM testexecutionrecords WHERE record_id = #{recordId} LIMIT 1")
    TestExecution selectByRecordId(@Param("recordId") Long recordId);

    /**
     * 通过执行ID字符串查询
     */
    @Select("SELECT * FROM testexecutionrecords WHERE execution_id = #{executionId} LIMIT 1")
    TestExecution selectByExecutionId(@Param("executionId") String executionId);

    /**
     * 通过执行范围和refId查询
     */
    @Select("SELECT * FROM testexecutionrecords WHERE execution_scope = #{scope} AND ref_id = #{refId} ORDER BY created_at DESC")
    java.util.List<TestExecution> selectByScopeAndRefId(@Param("scope") String scope, @Param("refId") Integer refId);

    /**
     * 更新执行统计信息
     */
    @Update("UPDATE testexecutionrecords SET " +
            "status = #{status}, " +
            "total_cases = #{totalCases}, " +
            "failed_cases = #{failedCases}, " +
            "success_rate = #{successRate}, " +
            "error_message = #{errorMessage}, " +
            "end_time = #{endTime}, " +
            "duration_seconds = #{durationSeconds}, " +
            "updated_at = NOW() " +
            "WHERE record_id = #{recordId}")
    void updateExecutionStats(@Param("recordId") Long recordId,
                               @Param("status") String status,
                               @Param("totalCases") Integer totalCases,
                               @Param("failedCases") Integer failedCases,
                               @Param("successRate") java.math.BigDecimal successRate,
                               @Param("errorMessage") String errorMessage,
                               @Param("endTime") java.time.LocalDateTime endTime,
                               @Param("durationSeconds") Integer durationSeconds);

    /**
     * 通过执行ID字符串更新状态
     */
    @Update("UPDATE testexecutionrecords SET " +
            "status = #{status}, " +
            "error_message = #{errorMessage}, " +
            "end_time = NOW(), " +
            "updated_at = NOW() " +
            "WHERE execution_id = #{executionId}")
    void updateStatusByExecutionId(@Param("executionId") String executionId,
                                    @Param("status") String status,
                                    @Param("errorMessage") String errorMessage);

    /**
     * 通过记录ID更新状态
     */
    @Update("UPDATE testexecutionrecords SET " +
            "status = #{status}, " +
            "error_message = #{errorMessage}, " +
            "end_time = NOW(), " +
            "updated_at = NOW() " +
            "WHERE record_id = #{recordId}")
    void updateStatusByRecordId(@Param("recordId") Long recordId,
                                  @Param("status") String status,
                                  @Param("errorMessage") String errorMessage);
}

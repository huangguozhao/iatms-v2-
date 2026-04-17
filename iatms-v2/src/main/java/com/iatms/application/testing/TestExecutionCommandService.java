package com.iatms.application.testing;

import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.domain.model.vo.ExecutionProgressVO;

import java.util.concurrent.CompletableFuture;

/**
 * 测试执行命令服务
 */
public interface TestExecutionCommandService {

    /**
     * 开始执行测试
     * @param cmd 执行参数
     * @param userId 执行人ID
     * @return 执行ID
     */
    String startExecution(StartExecutionCmd cmd, Long userId);

    /**
     * 异步开始执行测试
     * @param cmd 执行参数
     * @param userId 执行人ID
     * @return 执行ID的CompletableFuture
     */
    CompletableFuture<String> startExecutionAsync(StartExecutionCmd cmd, Long userId);

    /**
     * 取消执行
     * @param executionId 执行ID
     * @param userId 用户ID
     */
    void cancelExecution(String executionId, Long userId);

    /**
     * 暂停执行
     * @param executionId 执行ID
     * @param userId 用户ID
     */
    void pauseExecution(String executionId, Long userId);

    /**
     * 恢复执行
     * @param executionId 执行ID
     * @param userId 用户ID
     */
    void resumeExecution(String executionId, Long userId);

    /**
     * 获取执行进度
     * @param executionId 执行ID
     * @return 执行进度
     */
    ExecutionProgressVO getExecutionProgress(String executionId);
}

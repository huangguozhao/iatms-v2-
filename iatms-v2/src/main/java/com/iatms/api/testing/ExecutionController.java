package com.iatms.api.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ExecutionProgressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 测试执行控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/executions")
@RequiredArgsConstructor
public class ExecutionController {

    /**
     * 获取执行进度
     */
    @GetMapping("/{executionId}/status")
    @RequirePermission(value = ProjectPermission.CASE_EXECUTE, requireProjectId = false)
    public ApiResponse<ExecutionProgressVO> getExecutionStatus(@PathVariable String executionId) {
        // TODO: 从 Redis 获取执行状态
        ExecutionProgressVO progress = ExecutionProgressVO.builder()
                .executionIdStr(executionId)
                .status("PENDING")
                .progress(0)
                .totalCases(1)
                .completedCases(0)
                .passedCases(0)
                .failedCases(0)
                .build();

        return ApiResponse.success(progress);
    }

    /**
     * 取消执行
     */
    @PostMapping("/{executionId}/cancel")
    @RequirePermission(value = ProjectPermission.CASE_EXECUTE, requireProjectId = false)
    public ApiResponse<Void> cancelExecution(
            @PathVariable String executionId,
            @RequestAttribute("userId") Long userId) {

        log.info("取消执行: executionId={}, userId={}", executionId, userId);
        // TODO: 实现取消逻辑
        return ApiResponse.success();
    }
}

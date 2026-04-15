package com.iatms.application.scheduling.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.scheduling.ScheduledTaskQueryService;
import com.iatms.domain.model.entity.ScheduledTask;
import com.iatms.domain.model.vo.ScheduledTaskDetailVO;
import com.iatms.domain.model.vo.ScheduledTaskSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ScheduledTaskMapper;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 定时任务查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskQueryServiceImpl implements ScheduledTaskQueryService {

    private final ScheduledTaskMapper taskMapper;

    @Override
    public ApiResponse.PageResult<ScheduledTaskSummaryVO> queryTasks(
            Long projectId, String status, Integer pageNum, Integer pageSize) {

        LambdaQueryWrapper<ScheduledTask> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            // Map status string to isEnabled boolean
            wrapper.eq(ScheduledTask::getIsEnabled, "ACTIVE".equals(status) || "RUNNING".equals(status));
        }

        wrapper.eq(ScheduledTask::getDeleted, false);
        wrapper.orderByDesc(ScheduledTask::getCreatedAt);

        IPage<ScheduledTask> page = new Page<>(pageNum, pageSize);
        IPage<ScheduledTask> result = taskMapper.selectPage(page, wrapper);

        return ApiResponse.PageResult.of(
                result.getRecords().stream().map(this::convertToSummaryVO).toList(),
                result.getTotal(),
                pageNum,
                pageSize
        );
    }

    @Override
    public ScheduledTaskDetailVO getTaskDetail(Long taskId) {
        ScheduledTask task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TASK_NOT_FOUND.getCode(),
                    ErrorCode.TASK_NOT_FOUND.getMessage());
        }

        String taskStatus = "PAUSED";
        if (task.getIsEnabled() != null && task.getIsEnabled()) {
            taskStatus = "ACTIVE";
        }

        return ScheduledTaskDetailVO.builder()
                .id(task.getId())
                .name(task.getTaskName())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .triggerType(task.getTriggerType())
                .cronExpression(task.getCronExpression())
                .targetId(task.getTargetId() != null ? task.getTargetId().longValue() : null)
                .environmentId(null) // executionEnvironment is String, not Long
                .status(taskStatus)
                .lastRunTime(task.getLastExecutionTime())
                .nextRunTime(task.getNextTriggerTime())
                .totalRuns(task.getTotalExecutions())
                .successRuns(task.getSuccessfulExecutions())
                .failedRuns(task.getFailedExecutions())
                .notifyOnSuccess(task.getNotifyOnSuccess())
                .notifyOnFailure(task.getNotifyOnFailure())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .createdBy(task.getCreatedBy())
                .build();
    }

    private ScheduledTaskSummaryVO convertToSummaryVO(ScheduledTask task) {
        String taskStatus = "PAUSED";
        if (task.getIsEnabled() != null && task.getIsEnabled()) {
            taskStatus = "ACTIVE";
        }

        return ScheduledTaskSummaryVO.builder()
                .id(task.getId())
                .name(task.getTaskName())
                .taskType(task.getTaskType())
                .triggerType(task.getTriggerType())
                .status(taskStatus)
                .nextRunTime(task.getNextTriggerTime())
                .totalRuns(task.getTotalExecutions())
                .successRuns(task.getSuccessfulExecutions())
                .failedRuns(task.getFailedExecutions())
                .createdAt(task.getCreatedAt())
                .build();
    }
}

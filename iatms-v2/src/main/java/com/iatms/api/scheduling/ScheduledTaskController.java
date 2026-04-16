package com.iatms.api.scheduling;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.scheduling.ScheduledTaskCommandService;
import com.iatms.application.scheduling.ScheduledTaskQueryService;
import com.iatms.application.scheduling.dto.command.CreateScheduledTaskCmd;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ScheduledTaskDetailVO;
import com.iatms.domain.model.vo.ScheduledTaskSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/scheduled-tasks")
@RequiredArgsConstructor
public class ScheduledTaskController {

    private final ScheduledTaskCommandService taskCommandService;
    private final ScheduledTaskQueryService taskQueryService;

    @PostMapping
    @RequirePermission(ProjectPermission.TASK_CREATE)
    public ApiResponse<ScheduledTaskDetailVO> createTask(
            @RequestBody @Valid CreateScheduledTaskCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建定时任务: name={}, userId={}", cmd.getName(), userId);
        ScheduledTaskDetailVO result = taskCommandService.createTask(cmd, userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{taskId}")
    @RequirePermission(ProjectPermission.TASK_EDIT)
    public ApiResponse<ScheduledTaskDetailVO> updateTask(
            @PathVariable Long taskId,
            @RequestBody @Valid CreateScheduledTaskCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新定时任务: taskId={}, userId={}", taskId, userId);
        ScheduledTaskDetailVO result = taskCommandService.updateTask(taskId, cmd, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{taskId}")
    @RequirePermission(ProjectPermission.TASK_DELETE)
    public ApiResponse<Void> deleteTask(
            @PathVariable Long taskId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除定时任务: taskId={}, userId={}", taskId, userId);
        taskCommandService.deleteTask(taskId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{taskId}")
    @RequirePermission(ProjectPermission.TASK_VIEW)
    public ApiResponse<ScheduledTaskDetailVO> getTaskDetail(@PathVariable Long taskId) {
        ScheduledTaskDetailVO result = taskQueryService.getTaskDetail(taskId);
        return ApiResponse.success(result);
    }

    @GetMapping
    @RequirePermission(ProjectPermission.TASK_VIEW)
    public ApiResponse<ApiResponse.PageResult<ScheduledTaskSummaryVO>> queryTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        var result = taskQueryService.queryTasks(projectId, status, pageNum, pageSize);
        return ApiResponse.pageSuccess(result);
    }

    @PostMapping("/{taskId}/run")
    @RequirePermission(ProjectPermission.TASK_EXECUTE)
    public ApiResponse<Void> runTaskNow(
            @PathVariable Long taskId,
            @RequestAttribute("userId") Long userId) {

        log.info("立即执行定时任务: taskId={}, userId={}", taskId, userId);
        taskCommandService.runTaskNow(taskId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{taskId}/pause")
    @RequirePermission(ProjectPermission.TASK_EDIT)
    public ApiResponse<Void> pauseTask(
            @PathVariable Long taskId,
            @RequestAttribute("userId") Long userId) {

        log.info("暂停定时任务: taskId={}, userId={}", taskId, userId);
        taskCommandService.pauseTask(taskId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{taskId}/resume")
    @RequirePermission(ProjectPermission.TASK_EDIT)
    public ApiResponse<Void> resumeTask(
            @PathVariable Long taskId,
            @RequestAttribute("userId") Long userId) {

        log.info("恢复定时任务: taskId={}, userId={}", taskId, userId);
        taskCommandService.resumeTask(taskId, userId);
        return ApiResponse.success();
    }
}

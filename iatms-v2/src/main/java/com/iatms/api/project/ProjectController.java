package com.iatms.api.project;

import com.iatms.api.common.ApiResponse;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.application.project.ProjectCommandService;
import com.iatms.application.project.ProjectQueryService;
import com.iatms.application.project.dto.command.CreateProjectCmd;
import com.iatms.application.project.dto.command.UpdateProjectCmd;
import com.iatms.application.project.dto.query.ProjectQuery;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ProjectDetailVO;
import com.iatms.domain.model.vo.ProjectSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectCommandService projectCommandService;
    private final ProjectQueryService projectQueryService;

    /**
     * 创建项目
     * 任何登录用户都可以创建项目，创建后成为项目负责人
     */
    @PostMapping
    public ApiResponse<ProjectDetailVO> createProject(
            @RequestBody @Valid CreateProjectCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建项目: cmd={}, userId={}", cmd, userId);
        ProjectDetailVO result = projectCommandService.createProject(cmd, userId);
        return ApiResponse.success(result);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{projectId}")
    @RequirePermission(ProjectPermission.PROJECT_EDIT)
    public ApiResponse<ProjectDetailVO> updateProject(
            @PathVariable Long projectId,
            @RequestBody @Valid UpdateProjectCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新项目: projectId={}, cmd={}, userId={}", projectId, cmd, userId);
        ProjectDetailVO result = projectCommandService.updateProject(projectId, cmd, userId);
        return ApiResponse.success(result);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{projectId}")
    @RequirePermission(ProjectPermission.PROJECT_DELETE)
    public ApiResponse<Void> deleteProject(
            @PathVariable Long projectId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除项目: projectId={}, userId={}", projectId, userId);
        projectCommandService.deleteProject(projectId, userId);
        return ApiResponse.success();
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{projectId}")
    @RequirePermission(ProjectPermission.PROJECT_VIEW)
    public ApiResponse<ProjectDetailVO> getProjectDetail(
            @PathVariable Long projectId,
            @RequestAttribute("userId") Long userId) {

        ProjectDetailVO result = projectQueryService.getProjectDetail(projectId, userId);
        return ApiResponse.success(result);
    }

    /**
     * 分页查询项目列表
     * 根据用户权限自动过滤可访问的项目
     */
    @GetMapping
    public ApiResponse<ApiResponse.PageResult<ProjectSummaryVO>> queryProjects(
            ProjectQuery query,
            @RequestAttribute("userId") Long userId) {

        ApiResponse.PageResult<ProjectSummaryVO> result = projectQueryService.queryProjects(query, userId);
        return ApiResponse.pageSuccess(result);
    }

    /**
     * 获取最近访问的项目
     */
    @GetMapping("/recent")
    public ApiResponse<List<ProjectSummaryVO>> getRecentProjects(
            @RequestParam(defaultValue = "10") int limit,
            @RequestAttribute("userId") Long userId) {

        List<ProjectSummaryVO> result = projectQueryService.getRecentProjects(userId, limit);
        return ApiResponse.success(result);
    }

    /**
     * 添加项目成员
     */
    @PostMapping("/{projectId}/members")
    @RequirePermission(ProjectPermission.PROJECT_MANAGE_MEMBERS)
    public ApiResponse<Void> addMember(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "MEMBER") String role,
            @RequestAttribute("userId") Long operatorId) {

        projectCommandService.addMember(projectId, userId, role, operatorId);
        return ApiResponse.success();
    }

    /**
     * 移除项目成员
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    @RequirePermission(ProjectPermission.PROJECT_MANAGE_MEMBERS)
    public ApiResponse<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestAttribute("userId") Long operatorId) {

        projectCommandService.removeMember(projectId, userId, operatorId);
        return ApiResponse.success();
    }
}

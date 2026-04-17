package com.iatms.api.environment;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.environment.EnvironmentQueryService;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.entity.EnvironmentConfig;
import com.iatms.domain.model.enums.ProjectPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 环境配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentQueryService environmentQueryService;

    /**
     * 获取所有激活的环境列表
     */
    @GetMapping
    @RequirePermission(value = ProjectPermission.CASE_EXECUTE, requireProjectId = false)
    public ApiResponse<List<EnvironmentConfig>> listEnvironments() {
        List<EnvironmentConfig> environments = environmentQueryService.listActiveEnvironments();
        return ApiResponse.success(environments);
    }

    /**
     * 根据ID获取环境详情
     */
    @GetMapping("/{envId}")
    @RequirePermission(value = ProjectPermission.CASE_EXECUTE, requireProjectId = false)
    public ApiResponse<EnvironmentConfig> getEnvironmentById(@PathVariable Integer envId) {
        EnvironmentConfig environment = environmentQueryService.getEnvironmentById(envId);
        if (environment == null) {
            return ApiResponse.error(404, "环境不存在");
        }
        return ApiResponse.success(environment);
    }

    /**
     * 获取默认环境
     */
    @GetMapping("/default")
    @RequirePermission(value = ProjectPermission.CASE_EXECUTE, requireProjectId = false)
    public ApiResponse<EnvironmentConfig> getDefaultEnvironment() {
        EnvironmentConfig environment = environmentQueryService.getDefaultEnvironment();
        if (environment == null) {
            return ApiResponse.error(404, "未设置默认环境");
        }
        return ApiResponse.success(environment);
    }
}

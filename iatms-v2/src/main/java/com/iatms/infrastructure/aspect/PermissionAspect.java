package com.iatms.infrastructure.aspect;

import com.iatms.common.annotation.RequirePermission;
import com.iatms.common.exception.BusinessException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.application.system.PermissionService;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestSuiteMapper;
import com.iatms.infrastructure.persistence.mapper.ScheduledTaskMapper;
import com.iatms.infrastructure.persistence.mapper.ModuleMapper;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestSuite;
import com.iatms.domain.model.entity.ScheduledTask;
import com.iatms.domain.model.entity.Module;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 权限校验切面
 * 拦截带有 @RequirePermission 注解的方法进行权限校验
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;
    private final ApiRequestMapper apiRequestMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestSuiteMapper testSuiteMapper;
    private final ScheduledTaskMapper scheduledTaskMapper;
    private final ModuleMapper moduleMapper;

    /**
     * URL中提取projectId的正则表达式
     * 匹配 /v1/projects/123/modules 或 /projects/123 等格式
     */
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("/(?:v\\d+/)?projects?/(\\d+)");

    /**
     * API路径模式: /v1/apis/{apiId} 或 /apis/{apiId}
     */
    private static final Pattern API_PATH_PATTERN = Pattern.compile("/(?:v\\d+/)?apis?/(\\d+)");

    /**
     * 测试用例路径模式: /v1/test-cases/{caseId}
     */
    private static final Pattern TEST_CASE_PATH_PATTERN = Pattern.compile("/(?:v\\d+/)?test-cases?/(\\d+)");

    /**
     * 测试套件路径模式: /test-suites/{suiteId}
     */
    private static final Pattern TEST_SUITE_PATH_PATTERN = Pattern.compile("/(?:v\\d+/)?test-suites?/(\\d+)");

    /**
     * 定时任务路径模式: /v1/scheduled-tasks/{taskId}
     */
    private static final Pattern SCHEDULED_TASK_PATH_PATTERN = Pattern.compile("/(?:v\\d+/)?scheduled-tasks?/(\\d+)");

    @Around("@annotation(com.iatms.common.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(),
                    ErrorCode.UNAUTHORIZED.getMessage());
        }

        // 2. 获取登录时存入的 userId
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(),
                    ErrorCode.UNAUTHORIZED.getMessage());
        }

        // 3. 获取方法上注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);

        // 4. 如果不需要项目ID（如项目树接口），直接校验登录状态即可
        if (!requirePermission.requireProjectId()) {
            log.debug("项目ID非必需，直接放行: userId={}, uri={}", userId, request.getRequestURI());
            return joinPoint.proceed();
        }

        // 5. 获取 projectId
        Long projectId = getProjectId(request);

        if (projectId == null) {
            log.warn("无法确定项目ID: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.INVALID_PARAMETER.getCode(), "无法确定项目ID");
        }

        // 6. 获取方法上注解要求的权限
        ProjectPermission[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();

        // 7. 校验权限
        boolean hasPermission;
        if (logical == RequirePermission.Logical.AND) {
            // 所有权限都需要满足
            hasPermission = true;
            for (ProjectPermission permission : requiredPermissions) {
                if (!permissionService.hasProjectPermission(userId, projectId, permission)) {
                    log.warn("权限不足: userId={}, projectId={}, required={}, logical=AND",
                            userId, projectId, permission);
                    hasPermission = false;
                    break;
                }
            }
        } else {
            // 任意一个权限满足即可
            hasPermission = false;
            for (ProjectPermission permission : requiredPermissions) {
                if (permissionService.hasProjectPermission(userId, projectId, permission)) {
                    hasPermission = true;
                    break;
                }
            }
        }

        if (!hasPermission) {
            log.warn("权限校验失败: userId={}, projectId={}, requiredPermissions={}, logical={}",
                    userId, projectId, java.util.Arrays.toString(requiredPermissions), logical);
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "权限不足");
        }

        log.debug("权限校验通过: userId={}, projectId={}, requiredPermissions={}",
                userId, projectId, java.util.Arrays.toString(requiredPermissions));

        // 6. 校验通过，执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 获取projectId
     * 1. 优先从request属性获取（Controller显式设置的）
     * 2. 从URL路径提取（针对 /projects/{projectId} 路径）
     * 3. 从资源路径提取并查询关联项目（针对 /apis/{apiId} 等路径）
     * 4. 从查询参数获取（?projectId=xxx）
     */
    private Long getProjectId(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // 1. 尝试从request属性获取（Controller已设置的）
        Object projectIdAttr = request.getAttribute("projectId");
        if (projectIdAttr instanceof Long) {
            return (Long) projectIdAttr;
        }
        if (projectIdAttr instanceof Integer) {
            return ((Integer) projectIdAttr).longValue();
        }

        // 2. 从URL路径提取（/v1/projects/{projectId} 或 /projects/{projectId}）
        Long projectId = extractProjectIdFromUrl(uri);
        if (projectId != null) {
            return projectId;
        }

        // 3. 从资源路径提取并查询关联项目（/v1/apis/{apiId}, /v1/test-cases/{caseId} 等）
        projectId = extractProjectIdFromResourcePath(uri);
        if (projectId != null) {
            return projectId;
        }

        // 4. 从查询参数获取（?projectId=xxx）
        String projectIdParam = request.getParameter("projectId");
        if (projectIdParam != null) {
            try {
                return Long.parseLong(projectIdParam);
            } catch (NumberFormatException e) {
                log.warn("无效的projectId参数: {}", projectIdParam);
            }
        }

        return null;
    }

    /**
     * 从URL中提取projectId（针对 /projects/{projectId} 路径）
     */
    private Long extractProjectIdFromUrl(String uri) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }

        Matcher matcher = PROJECT_ID_PATTERN.matcher(uri);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("无效的projectId格式: {}", matcher.group(1));
            }
        }
        return null;
    }

    /**
     * 从资源路径提取projectId
     * 通过资源ID查询关联的项目ID
     */
    private Long extractProjectIdFromResourcePath(String uri) {
        if (uri == null || uri.isEmpty()) {
            return null;
        }

        // /v1/apis/{apiId} -> ApiRequest (moduleId -> Module.projectId)
        Matcher apiMatcher = API_PATH_PATTERN.matcher(uri);
        if (apiMatcher.find()) {
            try {
                Long apiId = Long.parseLong(apiMatcher.group(1));
                ApiRequest api = apiRequestMapper.selectById(apiId);
                if (api != null && api.getModuleId() != null) {
                    Module module = moduleMapper.selectById(api.getModuleId());
                    if (module != null && module.getProjectId() != null) {
                        return module.getProjectId().longValue();
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("无效的apiId: {}", apiMatcher.group(1));
            }
        }

        // /v1/test-cases/{caseId} -> TestCase (apiId -> ApiRequest.moduleId -> Module.projectId)
        Matcher caseMatcher = TEST_CASE_PATH_PATTERN.matcher(uri);
        if (caseMatcher.find()) {
            try {
                Long caseId = Long.parseLong(caseMatcher.group(1));
                TestCase testCase = testCaseMapper.selectById(caseId);
                if (testCase != null && testCase.getApiId() != null) {
                    ApiRequest api = apiRequestMapper.selectById(testCase.getApiId().longValue());
                    if (api != null && api.getModuleId() != null) {
                        Module module = moduleMapper.selectById(api.getModuleId());
                        if (module != null && module.getProjectId() != null) {
                            return module.getProjectId().longValue();
                        }
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("无效的caseId: {}", caseMatcher.group(1));
            }
        }

        // /test-suites/{suiteId} -> TestSuite
        Matcher suiteMatcher = TEST_SUITE_PATH_PATTERN.matcher(uri);
        if (suiteMatcher.find()) {
            try {
                Long suiteId = Long.parseLong(suiteMatcher.group(1));
                TestSuite suite = testSuiteMapper.selectById(suiteId);
                if (suite != null && suite.getProjectId() != null) {
                    return suite.getProjectId().longValue();
                }
            } catch (NumberFormatException e) {
                log.warn("无效的suiteId: {}", suiteMatcher.group(1));
            }
        }

        // /v1/scheduled-tasks/{taskId} -> ScheduledTask
        Matcher taskMatcher = SCHEDULED_TASK_PATH_PATTERN.matcher(uri);
        if (taskMatcher.find()) {
            try {
                Long taskId = Long.parseLong(taskMatcher.group(1));
                ScheduledTask task = scheduledTaskMapper.selectById(taskId);
                if (task != null) {
                    return getProjectIdFromScheduledTask(task);
                }
            } catch (NumberFormatException e) {
                log.warn("无效的taskId: {}", taskMatcher.group(1));
            }
        }

        return null;
    }

    /**
     * 从ScheduledTask获取projectId
     * 根据taskType不同，targetId可能指向project/module/case/suite/api
     */
    private Long getProjectIdFromScheduledTask(ScheduledTask task) {
        if (task.getTargetId() == null || task.getTaskType() == null) {
            return null;
        }

        String taskType = task.getTaskType().toLowerCase();
        Integer targetId = task.getTargetId();

        switch (taskType) {
            case "project":
                return targetId.longValue();

            case "module":
                Module module = moduleMapper.selectById(targetId);
                return module != null && module.getProjectId() != null
                        ? module.getProjectId().longValue() : null;

            case "test_suite":
                TestSuite suite = testSuiteMapper.selectById(targetId.longValue());
                return suite != null && suite.getProjectId() != null
                        ? suite.getProjectId().longValue() : null;

            case "single_case":
                TestCase testCase = testCaseMapper.selectById(targetId.longValue());
                if (testCase != null && testCase.getApiId() != null) {
                    ApiRequest api = apiRequestMapper.selectById(testCase.getApiId().longValue());
                    if (api != null && api.getModuleId() != null) {
                        Module m = moduleMapper.selectById(api.getModuleId());
                        return m != null && m.getProjectId() != null
                                ? m.getProjectId().longValue() : null;
                    }
                }
                return null;

            case "api":
                ApiRequest api = apiRequestMapper.selectById(targetId.longValue());
                if (api != null && api.getModuleId() != null) {
                    Module m = moduleMapper.selectById(api.getModuleId());
                    return m != null && m.getProjectId() != null
                            ? m.getProjectId().longValue() : null;
                }
                return null;

            default:
                log.warn("未知的scheduled task类型: {}", taskType);
                return null;
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}

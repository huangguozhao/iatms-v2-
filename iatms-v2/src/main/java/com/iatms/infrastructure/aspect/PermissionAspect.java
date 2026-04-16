package com.iatms.infrastructure.aspect;

import com.iatms.common.annotation.RequirePermission;
import com.iatms.common.exception.BusinessException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.application.system.PermissionService;
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

    /**
     * URL中提取projectId的正则表达式
     * 匹配 /v1/projects/123/modules 或 /projects/123 等格式
     */
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("/(?:v\\d+/)?projects?/(\\d+)");

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

        // 3. 从 URL 提取 projectId
        Long projectId = extractProjectIdFromUrl(request.getRequestURI());
        if (projectId == null) {
            // 如果URL中没有projectId，尝试从请求参数中获取
            String projectIdParam = request.getParameter("projectId");
            if (projectIdParam != null) {
                projectId = Long.parseLong(projectIdParam);
            }
        }

        if (projectId == null) {
            log.warn("无法从URL中提取projectId: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.INVALID_PARAMETER.getCode(), "无法确定项目ID");
        }

        // 4. 获取方法上注解要求的权限
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        ProjectPermission[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();

        // 5. 校验权限
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
     * 从URL中提取projectId
     * 支持格式:
     * - /v1/projects/123/modules
     * - /projects/123/modules
     * - /v1/project/123
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
     * 获取当前请求
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}

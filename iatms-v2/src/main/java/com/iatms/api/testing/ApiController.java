package com.iatms.api.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.testing.ApiCommandService;
import com.iatms.application.testing.ApiQueryService;
import com.iatms.application.testing.dto.command.CreateApiRequestCmd;
import com.iatms.application.testing.dto.query.ApiQuery;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ApiDetailVO;
import com.iatms.domain.model.vo.ApiSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API 管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/apis")
@RequiredArgsConstructor
public class ApiController {

    private final ApiCommandService apiCommandService;
    private final ApiQueryService apiQueryService;

    @PostMapping
    @RequirePermission(ProjectPermission.API_CREATE)
    public ApiResponse<ApiDetailVO> createApi(
            @RequestBody @Valid CreateApiRequestCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建API: name={}, userId={}", cmd.getName(), userId);
        ApiDetailVO result = apiCommandService.createApi(cmd, userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{apiId}")
    @RequirePermission(ProjectPermission.API_EDIT)
    public ApiResponse<ApiDetailVO> updateApi(
            @PathVariable Long apiId,
            @RequestBody @Valid CreateApiRequestCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新API: apiId={}, userId={}", apiId, userId);
        ApiDetailVO result = apiCommandService.updateApi(apiId, cmd, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{apiId}")
    @RequirePermission(ProjectPermission.API_DELETE)
    public ApiResponse<Void> deleteApi(
            @PathVariable Long apiId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除API: apiId={}, userId={}", apiId, userId);
        apiCommandService.deleteApi(apiId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{apiId}")
    @RequirePermission(ProjectPermission.API_VIEW)
    public ApiResponse<ApiDetailVO> getApiDetail(@PathVariable Long apiId) {
        ApiDetailVO result = apiQueryService.getApiDetail(apiId);
        return ApiResponse.success(result);
    }

    @GetMapping
    @RequirePermission(ProjectPermission.API_VIEW)
    public ApiResponse<ApiResponse.PageResult<ApiSummaryVO>> queryApis(ApiQuery query) {
        ApiResponse.PageResult<ApiSummaryVO> result = apiQueryService.queryApis(query);
        return ApiResponse.pageSuccess(result);
    }

    @GetMapping("/tree/{projectId}")
    public ApiResponse<List<?>> getApiTree(@PathVariable Long projectId) {
        List<?> tree = apiQueryService.getApiTree(projectId);
        return ApiResponse.success(tree);
    }
}

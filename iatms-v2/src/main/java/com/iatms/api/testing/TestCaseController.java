package com.iatms.api.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.testing.TestCaseCommandService;
import com.iatms.application.testing.TestCaseQueryService;
import com.iatms.application.testing.dto.command.CreateTestCaseCmd;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.model.vo.ProjectTreeVO;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.domain.model.vo.TestCaseSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用例控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseCommandService testCaseCommandService;
    private final TestCaseQueryService testCaseQueryService;

    @PostMapping
    @RequirePermission(ProjectPermission.CASE_CREATE)
    public ApiResponse<TestCaseDetailVO> createTestCase(
            @RequestBody @Valid CreateTestCaseCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建测试用例: name={}, userId={}", cmd.getName(), userId);
        TestCaseDetailVO result = testCaseCommandService.createTestCase(cmd, userId);
        return ApiResponse.success(result);
    }

    /**
     * 获取项目树形结构（项目→模块→接口→用例）
     * 注意：这个路由必须放在 /{caseId} 前面，否则 /tree 会被 {caseId} 错误匹配
     */
    @GetMapping("/tree")
    @RequirePermission(value = ProjectPermission.CASE_VIEW, requireProjectId = false)
    public ApiResponse<List<ProjectTreeVO>> getProjectTree(
            @RequestParam(required = false) Long projectId,
            @RequestAttribute("userId") Long userId) {
        List<ProjectTreeVO> tree = testCaseQueryService.getProjectTree(projectId, userId);
        return ApiResponse.success(tree);
    }

    @GetMapping
    @RequirePermission(ProjectPermission.CASE_VIEW)
    public ApiResponse<ApiResponse.PageResult<TestCaseSummaryVO>> queryTestCases(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        var result = testCaseQueryService.queryTestCases(projectId, moduleId, keyword, pageNum, pageSize);
        return ApiResponse.pageSuccess(result);
    }

    @GetMapping("/{caseId}")
    @RequirePermission(ProjectPermission.CASE_VIEW)
    public ApiResponse<TestCaseDetailVO> getTestCaseDetail(@PathVariable Long caseId) {
        TestCaseDetailVO result = testCaseQueryService.getTestCaseDetail(caseId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{caseId}")
    @RequirePermission(ProjectPermission.CASE_EDIT)
    public ApiResponse<TestCaseDetailVO> updateTestCase(
            @PathVariable Long caseId,
            @RequestBody @Valid CreateTestCaseCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新测试用例: caseId={}, userId={}", caseId, userId);
        TestCaseDetailVO result = testCaseCommandService.updateTestCase(caseId, cmd, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{caseId}")
    @RequirePermission(ProjectPermission.CASE_DELETE)
    public ApiResponse<Void> deleteTestCase(
            @PathVariable Long caseId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除测试用例: caseId={}, userId={}", caseId, userId);
        testCaseCommandService.deleteTestCase(caseId, userId);
        return ApiResponse.success();
    }

    @PostMapping("/{caseId}/execute")
    @RequirePermission(ProjectPermission.CASE_EXECUTE)
    public ApiResponse<String> executeTestCase(
            @PathVariable Long caseId,
            @RequestAttribute("userId") Long userId) {

        log.info("执行测试用例: caseId={}, userId={}", caseId, userId);
        String executionId = testCaseQueryService.executeTestCase(caseId, userId);
        return ApiResponse.success(executionId);
    }
}

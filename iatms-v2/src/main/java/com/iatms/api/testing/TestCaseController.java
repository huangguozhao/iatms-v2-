package com.iatms.api.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.testing.TestCaseCommandService;
import com.iatms.application.testing.TestCaseQueryService;
import com.iatms.application.testing.dto.command.CreateTestCaseCmd;
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
    public ApiResponse<TestCaseDetailVO> createTestCase(
            @RequestBody @Valid CreateTestCaseCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("创建测试用例: name={}, userId={}", cmd.getName(), userId);
        TestCaseDetailVO result = testCaseCommandService.createTestCase(cmd, userId);
        return ApiResponse.success(result);
    }

    @PutMapping("/{caseId}")
    public ApiResponse<TestCaseDetailVO> updateTestCase(
            @PathVariable Long caseId,
            @RequestBody @Valid CreateTestCaseCmd cmd,
            @RequestAttribute("userId") Long userId) {

        log.info("更新测试用例: caseId={}, userId={}", caseId, userId);
        TestCaseDetailVO result = testCaseCommandService.updateTestCase(caseId, cmd, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{caseId}")
    public ApiResponse<Void> deleteTestCase(
            @PathVariable Long caseId,
            @RequestAttribute("userId") Long userId) {

        log.info("删除测试用例: caseId={}, userId={}", caseId, userId);
        testCaseCommandService.deleteTestCase(caseId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/{caseId}")
    public ApiResponse<TestCaseDetailVO> getTestCaseDetail(@PathVariable Long caseId) {
        TestCaseDetailVO result = testCaseQueryService.getTestCaseDetail(caseId);
        return ApiResponse.success(result);
    }

    @GetMapping
    public ApiResponse<ApiResponse.PageResult<TestCaseSummaryVO>> queryTestCases(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        var result = testCaseQueryService.queryTestCases(projectId, moduleId, keyword, pageNum, pageSize);
        return ApiResponse.pageSuccess(result);
    }

    @PostMapping("/{caseId}/execute")
    public ApiResponse<String> executeTestCase(
            @PathVariable Long caseId,
            @RequestAttribute("userId") Long userId) {

        log.info("执行测试用例: caseId={}, userId={}", caseId, userId);
        String executionId = testCaseQueryService.executeTestCase(caseId, userId);
        return ApiResponse.success(executionId);
    }
}

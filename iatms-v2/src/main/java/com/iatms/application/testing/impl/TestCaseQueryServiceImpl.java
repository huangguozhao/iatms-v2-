package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.testing.TestCaseQueryService;
import com.iatms.application.testing.dto.command.StartExecutionCmd;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.domain.model.vo.TestCaseSummaryVO;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 测试用例查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseQueryServiceImpl implements TestCaseQueryService {

    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper executionMapper;

    @Override
    public ApiResponse.PageResult<TestCaseSummaryVO> queryTestCases(
            Long projectId, Long moduleId, String keyword, Integer pageNum, Integer pageSize) {

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(TestCase::getProjectId, projectId);
        }

        if (moduleId != null) {
            wrapper.eq(TestCase::getModuleId, moduleId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(TestCase::getName, keyword)
                    .or().like(TestCase::getCaseCode, keyword));
        }

        wrapper.eq(TestCase::getDeleted, false);
        wrapper.orderByDesc(TestCase::getCreatedAt);

        IPage<TestCase> page = new Page<>(pageNum, pageSize);
        IPage<TestCase> result = testCaseMapper.selectPage(page, wrapper);

        return ApiResponse.PageResult.of(
                result.getRecords().stream().map(this::convertToSummaryVO).toList(),
                result.getTotal(),
                pageNum,
                pageSize
        );
    }

    @Override
    public TestCaseDetailVO getTestCaseDetail(Long caseId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        return TestCaseDetailVO.builder()
                .id(testCase.getId())
                .caseCode(testCase.getCaseCode())
                .name(testCase.getName())
                .description(testCase.getDescription())
                .projectId(testCase.getProjectId())
                .moduleId(testCase.getModuleId())
                .apiId(testCase.getApiId() != null ? testCase.getApiId().longValue() : null)
                .testType(testCase.getTestType())
                .priority(testCase.getPriority())
                .status(testCase.getStatus())
                .preconditions(testCase.getPreconditions())
                .testSteps(testCase.getTestSteps())
                .testData(testCase.getTestData())
                .headers(testCase.getHeaders())
                .requestParams(testCase.getRequestParams())
                .requestBody(testCase.getRequestBody())
                .assertions(testCase.getAssertions())
                .expectedResponse(testCase.getExpectedResponse())
                .extractors(testCase.getExtractors())
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .createdBy(testCase.getCreatedBy())
                .build();
    }

    @Override
    public String executeTestCase(Long caseId, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null || testCase.getDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // 创建执行记录
        String executionId = "EXEC-" + UUID.randomUUID().toString();

        TestExecution execution = new TestExecution();
        execution.setExecutionId(executionId);
        execution.setExecutionScope("test_case");
        execution.setRefId(caseId.intValue());
        execution.setStatus("pending");
        execution.setTotalCases(1);
        execution.setFailedCases(0);
        execution.setExecutionType("manual");
        execution.setExecutedBy(userId);
        execution.setCreatedBy(userId);

        executionMapper.insert(execution);

        log.info("创建测试执行记录: executionId={}, caseId={}, userId={}", executionId, caseId, userId);

        // TODO: 异步执行测试用例

        return executionId;
    }

    private TestCaseSummaryVO convertToSummaryVO(TestCase testCase) {
        return TestCaseSummaryVO.builder()
                .id(testCase.getId())
                .caseCode(testCase.getCaseCode())
                .name(testCase.getName())
                .priority(testCase.getPriority())
                .status(testCase.getStatus())
                .moduleId(testCase.getModuleId())
                .apiId(testCase.getApiId() != null ? testCase.getApiId().longValue() : null)
                .createdAt(testCase.getCreatedAt())
                .createdBy(testCase.getCreatedBy())
                .build();
    }
}

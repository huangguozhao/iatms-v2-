package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.testing.TestCaseCommandService;
import com.iatms.application.testing.dto.command.CreateTestCaseCmd;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.common.exception.BusinessException;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 测试用例命令服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseCommandServiceImpl implements TestCaseCommandService {

    private final TestCaseMapper testCaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseDetailVO createTestCase(CreateTestCaseCmd cmd, Long userId) {
        // 生成用例编号
        String caseCode = generateCaseCode();

        TestCase testCase = new TestCase();
        testCase.setCaseCode(caseCode);
        testCase.setName(cmd.getName());
        testCase.setDescription(cmd.getDescription());
        testCase.setProjectId(cmd.getProjectId());
        testCase.setModuleId(cmd.getModuleId());
        testCase.setApiId(cmd.getApiId() != null ? cmd.getApiId().intValue() : null);
        testCase.setTestType(cmd.getTestType());
        testCase.setPriority(cmd.getPriority());
        testCase.setPreconditions(cmd.getPreconditions());
        testCase.setTestSteps(cmd.getTestSteps());
        testCase.setTestData(cmd.getTestData());
        testCase.setHeaders(cmd.getHeaders());
        testCase.setRequestParams(cmd.getRequestParams());
        testCase.setRequestBody(cmd.getRequestBody());
        testCase.setAssertions(cmd.getAssertions());
        testCase.setExpectedResponse(cmd.getExpectedResponse());
        testCase.setExtractors(cmd.getExtractors());
        testCase.setStatus(cmd.getStatus());
        testCase.setCreatedBy(userId);
        testCase.setUpdatedBy(userId);

        testCaseMapper.insert(testCase);

        log.info("创建测试用例成功: id={}, code={}, userId={}", testCase.getId(), caseCode, userId);

        return getTestCaseDetail(testCase.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseDetailVO updateTestCase(Long caseId, CreateTestCaseCmd cmd, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        if (cmd.getName() != null) testCase.setName(cmd.getName());
        if (cmd.getDescription() != null) testCase.setDescription(cmd.getDescription());
        if (cmd.getPriority() != null) testCase.setPriority(cmd.getPriority());
        if (cmd.getPreconditions() != null) testCase.setPreconditions(cmd.getPreconditions());
        if (cmd.getTestSteps() != null) testCase.setTestSteps(cmd.getTestSteps());
        if (cmd.getTestData() != null) testCase.setTestData(cmd.getTestData());
        if (cmd.getHeaders() != null) testCase.setHeaders(cmd.getHeaders());
        if (cmd.getRequestParams() != null) testCase.setRequestParams(cmd.getRequestParams());
        if (cmd.getRequestBody() != null) testCase.setRequestBody(cmd.getRequestBody());
        if (cmd.getAssertions() != null) testCase.setAssertions(cmd.getAssertions());
        if (cmd.getExpectedResponse() != null) testCase.setExpectedResponse(cmd.getExpectedResponse());
        if (cmd.getExtractors() != null) testCase.setExtractors(cmd.getExtractors());
        if (cmd.getStatus() != null) testCase.setStatus(cmd.getStatus());
        if (cmd.getIsEnabled() != null) testCase.setIsEnabled(cmd.getIsEnabled());

        testCase.setUpdatedBy(userId);
        testCaseMapper.updateById(testCase);

        log.info("更新测试用例成功: id={}, userId={}", caseId, userId);

        return getTestCaseDetail(caseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTestCase(Long caseId, Long userId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null) {
            throw new ResourceNotFoundException(ErrorCode.TEST_CASE_NOT_FOUND.getCode(),
                    ErrorCode.TEST_CASE_NOT_FOUND.getMessage());
        }

        // 逻辑删除 - 使用 deleteById 触发 @TableLogic 注解
        testCaseMapper.deleteById(caseId);

        log.info("删除测试用例成功: id={}, userId={}", caseId, userId);
    }

    private String generateCaseCode() {
        return "TC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private TestCaseDetailVO getTestCaseDetail(Long caseId) {
        TestCase testCase = testCaseMapper.selectById(caseId);
        if (testCase == null) {
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
}

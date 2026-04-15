package com.iatms.application.testing;

import com.iatms.common.exception.BusinessException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.domain.model.entity.TestSuite;
import com.iatms.domain.model.entity.TestSuiteRequest;
import com.iatms.infrastructure.persistence.mapper.TestSuiteMapper;
import com.iatms.infrastructure.persistence.mapper.TestSuiteRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestSuiteCommandService {

    private final TestSuiteMapper testSuiteMapper;
    private final TestSuiteRequestMapper testSuiteRequestMapper;

    @Transactional
    public Long create(TestSuite testSuite, List<Long> caseIds) {
        validateTestSuite(testSuite);
        testSuiteMapper.insert(testSuite);

        if (caseIds != null && !caseIds.isEmpty()) {
            for (Long caseId : caseIds) {
                TestSuiteRequest link = new TestSuiteRequest();
                link.setTestSuiteId(testSuite.getId());
                link.setApiRequestId(caseId);
                link.setSortOrder(0);
                testSuiteRequestMapper.insert(link);
            }
        }

        log.info("创建测试套件: {}", testSuite.getSuiteName());
        return testSuite.getId();
    }

    @Transactional
    public void update(Long id, TestSuite testSuite, List<Long> caseIds) {
        TestSuite existing = testSuiteMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.TEST_SUITE_NOT_FOUND.getCode(), "测试套件不存在");
        }

        validateTestSuite(testSuite);
        testSuite.setId(id);
        testSuiteMapper.updateById(testSuite);

        // 更新关联的用例
        testSuiteRequestMapper.deleteByTestSuiteId(id);
        if (caseIds != null && !caseIds.isEmpty()) {
            for (Long caseId : caseIds) {
                TestSuiteRequest link = new TestSuiteRequest();
                link.setTestSuiteId(id);
                link.setApiRequestId(caseId);
                link.setSortOrder(0);
                testSuiteRequestMapper.insert(link);
            }
        }

        log.info("更新测试套件: {}", testSuite.getSuiteName());
    }

    @Transactional
    public void delete(Long id) {
        testSuiteMapper.deleteById(id);
        testSuiteRequestMapper.deleteByTestSuiteId(id);
        log.info("删除测试套件: {}", id);
    }

    @Transactional
    public void enable(Long id) {
        TestSuite suite = testSuiteMapper.selectById(id);
        if (suite == null) {
            throw new BusinessException(ErrorCode.TEST_SUITE_NOT_FOUND.getCode(), "测试套件不存在");
        }
        suite.setStatus("ENABLED");
        testSuiteMapper.updateById(suite);
    }

    @Transactional
    public void disable(Long id) {
        TestSuite suite = testSuiteMapper.selectById(id);
        if (suite == null) {
            throw new BusinessException(ErrorCode.TEST_SUITE_NOT_FOUND.getCode(), "测试套件不存在");
        }
        suite.setStatus("DISABLED");
        testSuiteMapper.updateById(suite);
    }

    private void validateTestSuite(TestSuite testSuite) {
        if (testSuite.getSuiteName() == null || testSuite.getSuiteName().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER.getCode(), "套件名称不能为空");
        }
        if (testSuite.getProjectId() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER.getCode(), "项目ID不能为空");
        }
    }
}

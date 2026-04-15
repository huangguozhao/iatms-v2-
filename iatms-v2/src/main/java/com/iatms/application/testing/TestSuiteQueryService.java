package com.iatms.application.testing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.common.exception.BusinessException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.domain.model.entity.TestSuite;
import com.iatms.domain.model.entity.TestSuiteRequest;
import com.iatms.infrastructure.persistence.mapper.TestSuiteMapper;
import com.iatms.infrastructure.persistence.mapper.TestSuiteRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestSuiteQueryService {

    private final TestSuiteMapper testSuiteMapper;
    private final TestSuiteRequestMapper testSuiteRequestMapper;

    public Page<TestSuite> query(String keyword, Integer projectId, int pageNum, int pageSize) {
        LambdaQueryWrapper<TestSuite> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(TestSuite::getSuiteName, keyword);
        }
        if (projectId != null) {
            wrapper.eq(TestSuite::getProjectId, projectId);
        }
        wrapper.orderByDesc(TestSuite::getCreatedAt);

        Page<TestSuite> page = new Page<>(pageNum, pageSize);
        return testSuiteMapper.selectPage(page, wrapper);
    }

    public TestSuite getById(Long id) {
        TestSuite testSuite = testSuiteMapper.selectById(id);
        if (testSuite == null) {
            throw new BusinessException(ErrorCode.TEST_SUITE_NOT_FOUND.getCode(), "测试套件不存在");
        }
        return testSuite;
    }

    public List<Long> getCaseIds(Long testSuiteId) {
        List<TestSuiteRequest> links = testSuiteRequestMapper.selectByTestSuiteId(testSuiteId);
        return links.stream()
                .map(TestSuiteRequest::getApiRequestId)
                .collect(Collectors.toList());
    }

    public int getCaseCount(Long testSuiteId) {
        return testSuiteRequestMapper.selectByTestSuiteId(testSuiteId).size();
    }
}

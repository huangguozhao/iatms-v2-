package com.iatms.application.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.vo.TestCaseDetailVO;
import com.iatms.domain.model.vo.TestCaseSummaryVO;

import java.util.List;

/**
 * 测试用例查询服务接口
 */
public interface TestCaseQueryService {

    /**
     * 分页查询测试用例
     */
    ApiResponse.PageResult<TestCaseSummaryVO> queryTestCases(
            Long projectId, Long moduleId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取测试用例详情
     */
    TestCaseDetailVO getTestCaseDetail(Long caseId);

    /**
     * 执行测试用例
     */
    String executeTestCase(Long caseId, Long userId);
}

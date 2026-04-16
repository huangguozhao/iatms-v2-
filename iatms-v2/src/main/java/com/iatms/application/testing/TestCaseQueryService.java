package com.iatms.application.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.vo.ProjectTreeVO;
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

    /**
     * 获取项目树形结构（项目→模块→接口→用例）
     * @param projectId 项目ID，传入null则返回用户有权限的所有项目树
     * @param userId 当前用户ID，用于权限过滤
     */
    List<ProjectTreeVO> getProjectTree(Long projectId, Long userId);
}

package com.iatms.application.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.entity.TestExecution;
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
            Long projectId, Long moduleId, Long apiId, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 获取测试用例详情
     */
    TestCaseDetailVO getTestCaseDetail(Long caseId);

    /**
     * 执行测试用例
     * @param caseId 用例ID
     * @param async 是否异步执行
     * @param userId 用户ID
     * @return 执行ID
     */
    String executeTestCase(Long caseId, Boolean async, Long userId);

    /**
     * 同步执行测试用例并返回结果
     * @param caseId 用例ID
     * @param userId 用户ID
     * @return 执行结果
     */
    com.iatms.application.testing.dto.result.ExecuteTestCaseResult executeTestCaseSync(Long caseId, Long userId);

    /**
     * 获取项目树形结构（项目→模块→接口→用例）
     * @param projectId 项目ID，传入null则返回用户有权限的所有项目树
     * @param userId 当前用户ID，用于权限过滤
     */
    List<ProjectTreeVO> getProjectTree(Long projectId, Long userId);

    /**
     * 获取测试用例的执行历史
     * @param caseId 用例ID
     * @param limit 返回记录数限制
     * @return 执行历史记录列表
     */
    List<TestExecution> getTestCaseExecutionHistory(Long caseId, Integer limit);
}

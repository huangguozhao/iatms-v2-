package com.iatms.application.testing;

import com.iatms.application.testing.dto.command.CreateTestCaseCmd;
import com.iatms.domain.model.vo.TestCaseDetailVO;

/**
 * 测试用例命令服务接口
 */
public interface TestCaseCommandService {

    /**
     * 创建测试用例
     */
    TestCaseDetailVO createTestCase(CreateTestCaseCmd cmd, Long userId);

    /**
     * 更新测试用例
     */
    TestCaseDetailVO updateTestCase(Long caseId, CreateTestCaseCmd cmd, Long userId);

    /**
     * 删除测试用例
     */
    void deleteTestCase(Long caseId, Long userId);
}

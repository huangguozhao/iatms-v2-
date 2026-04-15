package com.iatms.application.report;

import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.entity.TestResult;

import java.util.List;
import java.util.Map;

/**
 * 报告服务接口
 */
public interface ReportService {

    /**
     * 生成 HTML 报告
     * @param executionId 执行ID
     * @return HTML 报告内容
     */
    String generateHtmlReport(String executionId);

    /**
     * 生成 Excel 报告
     * @param executionId 执行ID
     * @return Excel 文件字节数组
     */
    byte[] generateExcelReport(String executionId);

    /**
     * 生成 PDF 报告
     * @param executionId 执行ID
     * @return PDF 文件字节数组
     */
    byte[] generatePdfReport(String executionId);

    /**
     * 获取执行汇总
     * @param executionId 执行ID
     * @return 汇总信息
     */
    Map<String, Object> getExecutionSummary(String executionId);

    /**
     * 获取执行结果列表
     * @param executionId 执行ID
     * @return 结果列表
     */
    List<TestResult> getExecutionResults(String executionId);

    /**
     * 获取执行详情
     * @param executionId 执行ID
     * @return 完整执行信息
     */
    Map<String, Object> getExecutionDetails(String executionId);
}

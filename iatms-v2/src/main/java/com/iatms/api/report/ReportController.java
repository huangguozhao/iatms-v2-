package com.iatms.api.report;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.report.ReportService;
import com.iatms.domain.model.entity.TestExecution;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 报告控制器
 */
@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final TestExecutionMapper testExecutionMapper;

    /**
     * 查询执行记录列表
     */
    @GetMapping
    public ApiResponse<Page<TestExecution>> query(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<TestExecution> page = new Page<>(pageNum, pageSize);
        // TODO: 实现实际的分页查询
        return ApiResponse.success(page);
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/{executionId}")
    public ApiResponse<Map<String, Object>> getDetail(@PathVariable String executionId) {
        Map<String, Object> details = reportService.getExecutionDetails(executionId);
        return ApiResponse.success(details);
    }

    /**
     * 获取执行汇总
     */
    @GetMapping("/{executionId}/summary")
    public ApiResponse<Map<String, Object>> getSummary(@PathVariable String executionId) {
        Map<String, Object> summary = reportService.getExecutionSummary(executionId);
        return ApiResponse.success(summary);
    }

    /**
     * 生成 HTML 报告
     */
    @GetMapping("/{executionId}/html")
    public ApiResponse<String> getHtmlReport(@PathVariable String executionId) {
        String html = reportService.generateHtmlReport(executionId);
        return ApiResponse.success(html);
    }

    /**
     * 下载 HTML 报告
     */
    @GetMapping(value = "/{executionId}/download", params = "format=HTML")
    public ApiResponse<byte[]> downloadHtmlReport(@PathVariable String executionId) {
        byte[] html = reportService.generateHtmlReport(executionId).getBytes();
        return ApiResponse.success(html);
    }

    /**
     * 下载 Excel 报告
     */
    @GetMapping(value = "/{executionId}/download", params = "format=EXCEL")
    public ApiResponse<byte[]> downloadExcelReport(@PathVariable String executionId) {
        byte[] excel = reportService.generateExcelReport(executionId);
        return ApiResponse.success(excel);
    }

    /**
     * 下载 PDF 报告
     */
    @GetMapping(value = "/{executionId}/download", params = "format=PDF")
    public ApiResponse<byte[]> downloadPdfReport(@PathVariable String executionId) {
        byte[] pdf = reportService.generatePdfReport(executionId);
        return ApiResponse.success(pdf);
    }
}

package com.iatms.application.report.impl;

import com.iatms.application.report.ReportService;
import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.entity.TestResult;
import com.iatms.domain.model.enums.ExecutionStatus;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.infrastructure.persistence.mapper.TestResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TestExecutionMapper executionMapper;
    private final TestResultMapper resultMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String generateHtmlReport(String executionId) {
        TestExecution execution = executionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            return "<html><body><h1>执行记录不存在</h1></body></html>";
        }

        List<TestResult> results = resultMapper.selectByExecutionId(executionId);

        // 计算统计数据
        int passedCount = 0;
        int failedCount = 0;
        int totalCount = results.size();
        for (TestResult result : results) {
            if ("passed".equalsIgnoreCase(result.getStatus())) {
                passedCount++;
            } else if ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) {
                failedCount++;
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>测试执行报告 - %s</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f7fa; padding: 20px; }
                    .container { max-width: 1200px; margin: 0 auto; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; border-radius: 12px; margin-bottom: 20px; }
                    .header h1 { font-size: 28px; margin-bottom: 10px; }
                    .header .meta { opacity: 0.9; font-size: 14px; }
                    .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 20px; }
                    .summary-card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
                    .summary-card .label { color: #666; font-size: 14px; margin-bottom: 8px; }
                    .summary-card .value { font-size: 32px; font-weight: bold; }
                    .summary-card .value.passed { color: #52c41a; }
                    .summary-card .value.failed { color: #ff4d4f; }
                    .summary-card .value.total { color: #1890ff; }
                    .passed-card .value { color: #52c41a; }
                    .failed-card .value { color: #ff4d4f; }
                    .content { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); overflow: hidden; }
                    .content-header { padding: 20px; border-bottom: 1px solid #f0f0f0; font-weight: 600; font-size: 16px; }
                    table { width: 100%%; border-collapse: collapse; }
                    th { background: #fafafa; padding: 14px 16px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #f0f0f0; }
                    td { padding: 14px 16px; border-bottom: 1px solid #f0f0f0; }
                    tr:hover { background: #fafafa; }
                    .status { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
                    .status.passed { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
                    .status.failed { background: #fff2f0; color: #ff4d4f; border: 1px solid #ffccc7; }
                    .status.skipped { background: #fafafa; color: #999; border: 1px solid #d9d9d9; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>接口自动化测试报告</h1>
                        <div class="meta">
                            <span>执行ID: %s</span> |
                            <span>开始时间: %s</span> |
                            <span>耗时: %s秒</span>
                        </div>
                    </div>

                    <div class="summary">
                        <div class="summary-card passed-card">
                            <div class="label">通过</div>
                            <div class="value passed">%d</div>
                        </div>
                        <div class="summary-card failed-card">
                            <div class="label">失败</div>
                            <div class="value failed">%d</div>
                        </div>
                        <div class="summary-card">
                            <div class="label">总计</div>
                            <div class="value total">%d</div>
                        </div>
                        <div class="summary-card">
                            <div class="label">通过率</div>
                            <div class="value">%.2f%%</div>
                        </div>
                    </div>

                    <div class="content">
                        <div class="content-header">执行明细</div>
                        <table>
                            <thead>
                                <tr>
                                    <th>用例名称</th>
                                    <th>状态</th>
                                    <th>响应时间(ms)</th>
                                    <th>执行时间</th>
                                    <th>错误信息</th>
                                </tr>
                            </thead>
                            <tbody>
            """.formatted(
                    executionId,
                    executionId,
                    execution.getStartTime() != null ? execution.getStartTime().format(DATE_FORMATTER) : "-",
                    execution.getDurationSeconds() != null ? execution.getDurationSeconds() : 0,
                    passedCount,
                    failedCount,
                    totalCount,
                    totalCount > 0 ? (passedCount * 100.0 / totalCount) : 0.0
            ));

        for (TestResult result : results) {
            String statusClass = "passed".equalsIgnoreCase(result.getStatus()) ? "passed" :
                    ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) ? "failed" : "skipped";
            String statusText = "passed".equalsIgnoreCase(result.getStatus()) ? "通过" :
                    ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) ? "失败" : "跳过";

            html.append(String.format("""
                            <tr>
                                <td>%s</td>
                                <td><span class="status %s">%s</span></td>
                                <td>%d</td>
                                <td>%s</td>
                                <td>%s</td>
                            </tr>
                            """,
                    escapeHtml(result.getCaseName() != null ? result.getCaseName() : "-"),
                    statusClass,
                    statusText,
                    result.getResponseTime() != null ? result.getResponseTime() : 0,
                    result.getStartTime() != null ? result.getStartTime().format(DATE_FORMATTER) : "-",
                    escapeHtml(result.getErrorMessage() != null ? result.getErrorMessage() : "-")
            ));
        }

        html.append("""
                            </tbody>
                        </table>
                    </div>

                    <div class="footer">
                        <p>Generated by IATMS - Intelligent API Testing Management System</p>
                        <p>Report generated at: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(LocalDateTime.now().format(DATE_FORMATTER)));

        return html.toString();
    }

    @Override
    public byte[] generateExcelReport(String executionId) {
        TestExecution execution = executionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            return new byte[0];
        }

        List<TestResult> results = resultMapper.selectByExecutionId(executionId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Test Report");

            // 创建标题样式
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 生成执行汇总
            Row summaryRow = sheet.createRow(0);
            summaryRow.createCell(0).setCellValue("执行ID");
            summaryRow.createCell(1).setCellValue(executionId);
            summaryRow.getCell(0).setCellStyle(titleStyle);
            summaryRow.getCell(1).setCellStyle(titleStyle);

            Row statusRow = sheet.createRow(1);
            statusRow.createCell(0).setCellValue("执行状态");
            statusRow.createCell(1).setCellValue(execution.getStatus());
            statusRow.getCell(0).setCellStyle(titleStyle);
            statusRow.getCell(1).setCellStyle(titleStyle);

            Row startRow = sheet.createRow(2);
            startRow.createCell(0).setCellValue("开始时间");
            startRow.createCell(1).setCellValue(execution.getStartTime() != null ? execution.getStartTime().format(DATE_FORMATTER) : "-");

            Row durationRow = sheet.createRow(3);
            durationRow.createCell(0).setCellValue("执行时长(秒)");
            durationRow.createCell(1).setCellValue(execution.getDurationSeconds() != null ? execution.getDurationSeconds() : 0);

            // 计算通过率
            int passedCount = 0;
            int totalCount = results.size();
            for (TestResult result : results) {
                if ("passed".equalsIgnoreCase(result.getStatus())) {
                    passedCount++;
                }
            }
            double passRate = totalCount > 0 ? (passedCount * 100.0 / totalCount) : 0.0;

            Row passRateRow = sheet.createRow(4);
            passRateRow.createCell(0).setCellValue("通过率");
            passRateRow.createCell(1).setCellValue(String.format("%.2f%%", passRate));

            // 空行
            sheet.createRow(5);

            // 表头
            String[] headers = {"用例名称", "状态", "响应时间(ms)", "执行时间", "错误信息"};
            Row headerRow = sheet.createRow(6);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowNum = 7;
            for (TestResult result : results) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getCaseName() != null ? result.getCaseName() : "-");
                row.createCell(1).setCellValue(result.getStatus() != null ? result.getStatus() : "-");
                row.createCell(2).setCellValue(result.getResponseTime() != null ? result.getResponseTime() : 0);
                row.createCell(3).setCellValue(result.getStartTime() != null ? result.getStartTime().format(DATE_FORMATTER) : "-");
                row.createCell(4).setCellValue(result.getErrorMessage() != null ? result.getErrorMessage() : "-");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("生成Excel报告失败: executionId={}", executionId, e);
            return new byte[0];
        }
    }

    @Override
    public byte[] generatePdfReport(String executionId) {
        TestExecution execution = executionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            return new byte[0];
        }

        List<TestResult> results = resultMapper.selectByExecutionId(executionId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = PDRectangle.A4.getHeight() - 50;
            float leftMargin = 50;
            float fontSize = 12;
            float leading = 14.5f;

            PDType1Font font = PDType1Font.HELVETICA;
            PDType1Font boldFont = PDType1Font.HELVETICA_BOLD;

            PDPageContentStream content = new PDPageContentStream(document, page);

            // 标题
            content.beginText();
            content.setFont(boldFont, 18);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("接口自动化测试报告");
            content.endText();
            yPosition -= 30;

            // 执行信息
            content.beginText();
            content.setFont(font, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("执行ID: " + executionId);
            content.endText();
            yPosition -= leading;

            String startTime = execution.getStartTime() != null ? execution.getStartTime().format(DATE_FORMATTER) : "-";
            content.beginText();
            content.setFont(font, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("开始时间: " + startTime);
            content.endText();
            yPosition -= leading;

            content.beginText();
            content.setFont(font, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("执行时长: " + (execution.getDurationSeconds() != null ? execution.getDurationSeconds() : 0) + "秒");
            content.endText();
            yPosition -= leading * 2;

            // 汇总信息
            content.beginText();
            content.setFont(boldFont, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("执行汇总");
            content.endText();
            yPosition -= leading;

            int passedCount = 0;
            int failedCount = 0;
            for (TestResult result : results) {
                if ("passed".equalsIgnoreCase(result.getStatus())) {
                    passedCount++;
                } else if ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) {
                    failedCount++;
                }
            }
            int totalCount = results.size();
            double passRate = totalCount > 0 ? (passedCount * 100.0 / totalCount) : 0.0;

            content.beginText();
            content.setFont(font, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText(String.format("通过: %d  |  失败: %d  |  总计: %d  |  通过率: %.2f%%",
                    passedCount, failedCount, totalCount, passRate));
            content.endText();
            yPosition -= leading * 2;

            // 表头
            content.beginText();
            content.setFont(boldFont, fontSize);
            content.moveTextPositionByAmount(leftMargin, yPosition);
            content.showText("用例名称");
            content.endText();
            content.beginText();
            content.setFont(boldFont, fontSize);
            content.moveTextPositionByAmount(300, yPosition);
            content.showText("状态");
            content.endText();
            content.beginText();
            content.setFont(boldFont, fontSize);
            content.moveTextPositionByAmount(400, yPosition);
            content.showText("耗时(ms)");
            content.endText();
            yPosition -= leading;

            // 分隔线
            content.setLineWidth(0.5f);
            content.moveTo(leftMargin, yPosition + 5);
            content.lineTo(PDRectangle.A4.getWidth() - leftMargin, yPosition + 5);
            content.stroke();
            yPosition -= leading;

            // 结果列表 - 限制显示前50条
            int displayCount = Math.min(results.size(), 50);
            for (int i = 0; i < displayCount; i++) {
                TestResult result = results.get(i);

                if (yPosition < 80) {
                    content.beginText();
                    content.setFont(font, fontSize);
                    content.moveTextPositionByAmount(leftMargin, yPosition);
                    content.showText("... (共 " + results.size() + " 条结果)");
                    content.endText();
                    break;
                }

                String caseName = result.getCaseName() != null ? result.getCaseName() : "-";
                if (caseName.length() > 30) {
                    caseName = caseName.substring(0, 27) + "...";
                }
                String status = "passed".equalsIgnoreCase(result.getStatus()) ? "通过" :
                        ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) ? "失败" : "跳过";
                String respTime = String.valueOf(result.getResponseTime() != null ? result.getResponseTime() : 0);

                content.beginText();
                content.setFont(font, fontSize);
                content.moveTextPositionByAmount(leftMargin, yPosition);
                content.showText(caseName);
                content.endText();

                content.beginText();
                content.setFont(font, fontSize);
                content.moveTextPositionByAmount(300, yPosition);
                content.showText(status);
                content.endText();

                content.beginText();
                content.setFont(font, fontSize);
                content.moveTextPositionByAmount(400, yPosition);
                content.showText(respTime);
                content.endText();

                yPosition -= leading;
            }

            content.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成PDF报告失败: executionId={}", executionId, e);
            return new byte[0];
        }
    }

    @Override
    public Map<String, Object> getExecutionSummary(String executionId) {
        TestExecution execution = executionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            return Collections.emptyMap();
        }

        List<TestResult> results = resultMapper.selectByExecutionId(executionId);

        int passedCount = 0;
        int failedCount = 0;
        for (TestResult result : results) {
            if ("passed".equalsIgnoreCase(result.getStatus())) {
                passedCount++;
            } else if ("failed".equalsIgnoreCase(result.getStatus()) || "broken".equalsIgnoreCase(result.getStatus())) {
                failedCount++;
            }
        }
        int totalCount = results.size();
        double passRate = totalCount > 0 ? (passedCount * 100.0 / totalCount) : 0.0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("executionId", executionId);
        summary.put("status", execution.getStatus());
        summary.put("totalCases", totalCount);
        summary.put("passedCases", passedCount);
        summary.put("failedCases", failedCount);
        summary.put("passRate", passRate);
        summary.put("duration", execution.getDurationSeconds());
        summary.put("startedAt", execution.getStartTime());
        summary.put("completedAt", execution.getEndTime());
        summary.put("triggerType", execution.getExecutionType());
        summary.put("executedBy", execution.getExecutedBy());

        return summary;
    }

    @Override
    public List<TestResult> getExecutionResults(String executionId) {
        return resultMapper.selectByExecutionId(executionId);
    }

    @Override
    public Map<String, Object> getExecutionDetails(String executionId) {
        TestExecution execution = executionMapper.selectByExecutionId(executionId);
        if (execution == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("execution", execution);
        details.put("results", getExecutionResults(executionId));
        details.put("summary", getExecutionSummary(executionId));

        return details;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

package com.iatms.api.ai;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.service.AIProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 辅助控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIProviderService aiProviderService;

    /**
     * 生成测试用例
     */
    @PostMapping("/generate-test-cases")
    public ApiResponse<String> generateTestCases(
            @RequestParam String apiDescription,
            @RequestParam(defaultValue = "FUNCTIONAL") String testType) {

        log.info("AI生成测试用例: apiDescription={}, testType={}", apiDescription, testType);
        String result = aiProviderService.generateTestCases(apiDescription, testType);
        return ApiResponse.success(result);
    }

    /**
     * 补全参数描述
     */
    @PostMapping("/complete-descriptions")
    public ApiResponse<Map<String, String>> completeParameterDescriptions(
            @RequestBody Map<String, String> parameters) {

        log.info("AI补全参数描述: count={}", parameters.size());
        Map<String, String> result = aiProviderService.completeParameterDescriptions(parameters);
        return ApiResponse.success(result);
    }

    /**
     * 生成模拟数据
     */
    @PostMapping("/generate-mock-data")
    public ApiResponse<String> generateMockData(
            @RequestParam String fieldType,
            @RequestParam(required = false) String constraints) {

        log.info("AI生成模拟数据: fieldType={}", fieldType);
        String result = aiProviderService.generateMockData(fieldType, constraints);
        return ApiResponse.success(result);
    }

    /**
     * 诊断测试失败原因
     */
    @PostMapping("/diagnose-failure")
    public ApiResponse<Map<String, Object>> diagnoseFailure(@RequestBody Map<String, Object> params) {
        log.info("AI诊断测试失败: params={}", params);

        String caseName = (String) params.get("caseName");
        String expected = (String) params.get("expected");
        String actual = (String) params.get("actual");
        String errorMessage = (String) params.get("errorMessage");

        String diagnosis = aiProviderService.diagnoseFailure(caseName, expected, actual, errorMessage);

        // 解析诊断结果为结构化数据
        Map<String, Object> result = parseDiagnosisResult(diagnosis, params);

        return ApiResponse.success(result);
    }

    /**
     * 解析诊断结果
     */
    private Map<String, Object> parseDiagnosisResult(String diagnosis, Map<String, Object> originalParams) {
        Map<String, Object> result = new java.util.HashMap<>();

        // 设置原始参数
        result.put("caseName", originalParams.get("caseName"));
        result.put("errorMessage", originalParams.get("errorMessage"));

        // 解析诊断文本
        result.put("severity", analyzeSeverity(diagnosis));
        result.put("rootCause", extractRootCause(diagnosis));
        result.put("issues", extractIssues(diagnosis));
        result.put("suggestions", extractSuggestions(diagnosis));
        result.put("analysis", diagnosis);

        return result;
    }

    private String analyzeSeverity(String diagnosis) {
        if (diagnosis == null) return "low";
        String lower = diagnosis.toLowerCase();
        if (lower.contains("严重") || lower.contains("高风险") || lower.contains("关键")) {
            return "high";
        } else if (lower.contains("中等") || lower.contains("警告")) {
            return "medium";
        }
        return "low";
    }

    private String extractRootCause(String diagnosis) {
        if (diagnosis == null) return "无法确定根本原因";
        // 简单提取第一个明确的原因描述
        String[] lines = diagnosis.split("\n");
        for (String line : lines) {
            if (line.contains("原因") || line.contains("可能原因") || line.contains("根本原因")) {
                return line.replaceAll("^[#*\\s]+", "").trim();
            }
        }
        return diagnosis.length() > 200 ? diagnosis.substring(0, 200) + "..." : diagnosis;
    }

    private java.util.List<Map<String, String>> extractIssues(String diagnosis) {
        java.util.List<Map<String, String>> issues = new java.util.ArrayList<>();
        if (diagnosis == null) return issues;

        String[] lines = diagnosis.split("\n");
        for (String line : lines) {
            if (line.contains("问题") && line.length() < 200) {
                issues.add(java.util.Map.of(
                        "title", "发现的问题",
                        "severity", "medium",
                        "description", line.replaceAll("^[#*\\s]+", "").trim()
                ));
            }
        }

        if (issues.isEmpty()) {
            issues.add(java.util.Map.of(
                    "title", "需要关注",
                    "severity", "low",
                    "description", diagnosis.length() > 300 ? diagnosis.substring(0, 300) : diagnosis
            ));
        }

        return issues;
    }

    private java.util.List<Map<String, String>> extractSuggestions(String diagnosis) {
        java.util.List<Map<String, String>> suggestions = new java.util.ArrayList<>();
        if (diagnosis == null) return suggestions;

        String[] lines = diagnosis.split("\n");
        for (String line : lines) {
            if (line.contains("建议") || line.contains("修复") || line.contains("解决方案")) {
                suggestions.add(java.util.Map.of(
                        "title", "修复建议",
                        "content", line.replaceAll("^[#*\\s]+", "").trim(),
                        "priority", "medium"
                ));
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add(java.util.Map.of(
                    "title", "通用建议",
                    "content", "请检查API配置、请求参数和服务器响应，确保所有必需字段正确传递。",
                    "priority", "low"
            ));
        }

        return suggestions;
    }
}

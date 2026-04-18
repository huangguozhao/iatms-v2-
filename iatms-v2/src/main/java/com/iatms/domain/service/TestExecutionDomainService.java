package com.iatms.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestSuite;
import com.iatms.domain.model.entity.TestSuiteRequest;
import com.iatms.domain.model.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试执行领域服务 - 核心执行逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionDomainService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * 变量解析 - 替换 {{variable}} 格式的变量
     */
    public String resolveVariables(String content, Map<String, Object> context) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = resolveVariableValue(variableName, context);
            String replacement = value != null ? value.toString() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 解析变量值
     */
    private Object resolveVariableValue(String variableName, Map<String, Object> context) {
        // 支持嵌套属性，如 {{user.name}}
        if (variableName.contains(".")) {
            String[] parts = variableName.split("\\.", 2);
            Object value = context.get(parts[0]);
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                return map.get(parts[1]);
            }
            return null;
        }
        return context.get(variableName);
    }

    /**
     * 解析 JSON 格式的变量
     */
    public Map<String, Object> resolveJsonVariables(String jsonContent, Map<String, Object> context) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return new HashMap<>();
        }

        try {
            JSONObject json = JSON.parseObject(jsonContent);
            Map<String, Object> result = new HashMap<>();
            for (String key : json.keySet()) {
                Object value = json.get(key);
                if (value instanceof String) {
                    result.put(key, resolveVariables((String) value, context));
                } else {
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("解析JSON变量失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 执行断言
     */
    public AssertionResult executeAssertions(String assertionRules, ApiResponse response) {
        if (assertionRules == null || assertionRules.isEmpty()) {
            return AssertionResult.pass();
        }

        try {
            List<AssertionRule> rules = JSON.parseArray(assertionRules, AssertionRule.class);
            // 空数组或null规则视为无断言，使用HTTP状态码判断
            if (rules == null || rules.isEmpty()) {
                return AssertionResult.pass();
            }

            List<AssertionResult.Item> results = new java.util.ArrayList<>();

            for (AssertionRule rule : rules) {
                AssertionResult.Item item = executeSingleAssertion(rule, response);
                results.add(item);
            }

            boolean allPassed = results.stream().allMatch(AssertionResult.Item::isPassed);
            return new AssertionResult(allPassed, results);

        } catch (Exception e) {
            log.error("执行断言失败: {}", e.getMessage());
            return AssertionResult.fail("断言解析失败: " + e.getMessage());
        }
    }

    /**
     * 执行单个断言
     */
    private AssertionResult.Item executeSingleAssertion(AssertionRule rule, ApiResponse response) {
        try {
            String field = rule.getField();
            String actual = extractFieldValue(response, field);
            String expected = rule.getExpected();
            String operator = rule.getOperator();

            boolean passed = compareValues(actual, expected, operator);

            return AssertionResult.Item.builder()
                    .field(field)
                    .expected(expected)
                    .actual(actual)
                    .operator(operator)
                    .passed(passed)
                    .message(passed ? "断言通过" : "断言失败")
                    .build();

        } catch (Exception e) {
            return AssertionResult.Item.builder()
                    .field(rule.getField())
                    .passed(false)
                    .message("断言执行异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 提取字段值
     */
    private String extractFieldValue(ApiResponse response, String field) {
        if (field == null || field.isEmpty()) {
            return null;
        }

        // 处理特殊字段
        switch (field) {
            case "statusCode":
            case "status_code":
                return String.valueOf(response.getStatusCode());
            case "responseTime":
            case "response_time":
                return String.valueOf(response.getResponseTime());
            default:
                // JSONPath 提取
                if (field.startsWith("$.")) {
                    return extractJsonPath(response.getBody(), field);
                }
                return null;
        }
    }

    /**
     * 简单的 JSONPath 提取
     */
    private String extractJsonPath(String json, String path) {
        try {
            JSONObject body = JSON.parseObject(json);
            String[] parts = path.substring(2).split("\\.");
            Object current = body;

            for (String part : parts) {
                if (current instanceof JSONObject) {
                    current = ((JSONObject) current).get(part);
                } else {
                    return null;
                }
            }

            return current != null ? current.toString() : null;
        } catch (Exception e) {
            log.warn("JSONPath提取失败: path={}, error={}", path, e.getMessage());
            return null;
        }
    }

    /**
     * 比较值
     */
    private boolean compareValues(String actual, String expected, String operator) {
        if (actual == null) {
            return "isNull".equals(operator) || "null".equals(expected);
        }

        switch (operator) {
            case "equals":
                return actual.equals(expected);
            case "notEquals":
                return !actual.equals(expected);
            case "contains":
                return actual.contains(expected);
            case "notContains":
                return !actual.contains(expected);
            case "startsWith":
                return actual.startsWith(expected);
            case "endsWith":
                return actual.endsWith(expected);
            case "matches":
                return actual.matches(expected);
            case "greaterThan":
                return Double.parseDouble(actual) > Double.parseDouble(expected);
            case "lessThan":
                return Double.parseDouble(actual) < Double.parseDouble(expected);
            case "isNull":
                return actual == null;
            case "notNull":
                return actual != null;
            default:
                return actual.equals(expected);
        }
    }

    /**
     * 提取变量
     */
    public Map<String, Object> extractVariables(String extractorRules, ApiResponse response) {
        Map<String, Object> variables = new HashMap<>();

        if (extractorRules == null || extractorRules.isEmpty()) {
            return variables;
        }

        try {
            List<ExtractorRule> rules = JSON.parseArray(extractorRules, ExtractorRule.class);

            for (ExtractorRule rule : rules) {
                String value = extractJsonPath(response.getBody(), rule.getExpression());
                if (value != null) {
                    variables.put(rule.getName(), value);
                }
            }

        } catch (Exception e) {
            log.error("提取变量失败: {}", e.getMessage());
        }

        return variables;
    }

    // ========== 内部类 ==========

    /**
     * API 响应（模拟）
     */
    @lombok.Data
    public static class ApiResponse {
        private int statusCode;
        private String body;
        private long responseTime;
        private Map<String, String> headers;
    }

    /**
     * 断言规则
     */
    @lombok.Data
    public static class AssertionRule {
        private String field;
        private String operator = "equals";
        private String expected;
    }

    /**
     * 提取器规则
     */
    @lombok.Data
    public static class ExtractorRule {
        private String name;
        private String expression;
    }

    /**
     * 断言结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AssertionResult {
        private boolean passed;
        private List<Item> items;
        private String message;

        /**
         * Constructor for 2-argument usage (passed, items)
         */
        public AssertionResult(boolean passed, List<Item> items) {
            this.passed = passed;
            this.items = items;
            this.message = null;
        }

        public static AssertionResult pass() {
            return AssertionResult.builder().passed(true).message("所有断言通过").build();
        }

        public static AssertionResult fail(String message) {
            return AssertionResult.builder().passed(false).message(message).build();
        }

        @lombok.Data
        @lombok.Builder
        public static class Item {
            private String field;
            private String expected;
            private String actual;
            private String operator;
            private boolean passed;
            private String message;
        }
    }
}

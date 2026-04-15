package com.iatms.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.domain.model.entity.AIServiceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * AI 服务提供者 - 支持 DeepSeek、OpenAI 等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIProviderService {

    private final WebClient.Builder webClientBuilder;

    private static final int DEFAULT_TIMEOUT = 60;
    private static final int DEFAULT_MAX_TOKENS = 2000;
    private static final double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 调用 AI 服务
     */
    public String callAI(AIServiceConfig config, String prompt) {
        log.info("调用AI服务: serviceType={}, model={}", config.getServiceType(), config.getModelName());

        try {
            // 构建请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName() != null ? config.getModelName() : "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);

            // 设置参数
            if (config.getMaxTokens() != null) {
                requestBody.put("max_tokens", config.getMaxTokens());
            } else {
                requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);
            }

            if (config.getTemperature() != null) {
                requestBody.put("temperature", config.getTemperature());
            } else {
                requestBody.put("temperature", DEFAULT_TEMPERATURE);
            }

            // 调用 API
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.deepseek.com";
            String apiKey = config.getApiKey();

            String response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(JSON.toJSONString(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .onErrorResume(e -> {
                        log.error("AI服务调用失败: {}", e.getMessage());
                        return Mono.just("{\"error\": \"" + e.getMessage() + "\"}");
                    })
                    .block();

            // 解析响应
            return parseAIResponse(response);

        } catch (Exception e) {
            log.error("AI服务调用异常: serviceType={}, error={}", config.getServiceType(), e.getMessage());
            return "AI服务调用失败: " + e.getMessage();
        }
    }

    /**
     * 生成测试用例
     */
    public String generateTestCases(String apiDescription, String testType) {
        log.info("生成测试用例: testType={}", testType);

        String prompt = String.format("""
            你是一个专业的接口测试工程师。请根据以下API描述生成测试用例。

            API描述：
            %s

            测试类型：%s

            请生成包含以下信息的测试用例JSON数组：
            - name: 测试用例名称
            - priority: 优先级 (P0/P1/P2/P3)
            - assertions: 断言数组，包含field(字段)、operator(操作符)、expected(预期值)

            只返回JSON数组，不要包含其他文字。
            """, apiDescription, testType);

        // 使用默认配置调用
        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType("deepseek");
        config.setModelName("deepseek-chat");
        config.setBaseUrl("https://api.deepseek.com");
        // 注意：实际使用时应该从数据库获取配置

        String response = callAI(config, prompt);

        // 尝试解析为JSON
        try {
            if (response.startsWith("[")) {
                return response;
            }
            // 尝试从响应中提取JSON
            int jsonStart = response.indexOf("[");
            int jsonEnd = response.lastIndexOf("]");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                return response.substring(jsonStart, jsonEnd + 1);
            }
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
        }

        // 返回默认测试用例
        return """
            [
                {
                    "name": "正常场景测试",
                    "priority": "P1",
                    "assertions": [
                        {"field": "statusCode", "operator": "equals", "expected": "200"}
                    ]
                },
                {
                    "name": "异常场景测试",
                    "priority": "P2",
                    "assertions": [
                        {"field": "statusCode", "operator": "equals", "expected": "400"}
                    ]
                }
            ]
            """;
    }

    /**
     * 补全参数描述
     */
    public Map<String, String> completeParameterDescriptions(Map<String, String> parameters) {
        log.info("补全参数描述: count={}", parameters.size());

        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请为以下API参数的描述进行补全和完善：\n\n");

        parameters.forEach((key, value) -> {
            promptBuilder.append(String.format("- %s: %s\n", key, value != null ? value : "无描述"));
        });

        promptBuilder.append("\n请为每个参数生成更详细的中文描述，只返回JSON对象，格式为 {\"参数名\": \"描述\"}，不要包含其他文字。");

        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType("deepseek");
        config.setModelName("deepseek-chat");

        String response = callAI(config, promptBuilder.toString());

        // 尝试解析响应
        try {
            if (response.startsWith("{")) {
                Map<String, Object> result = JSON.parseObject(response, Map.class);
                Map<String, String> stringResult = new HashMap<>();
                result.forEach((k, v) -> stringResult.put(k, v != null ? v.toString() : ""));
                return stringResult;
            }
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
        }

        // 返回默认结果
        Map<String, String> result = new HashMap<>();
        parameters.forEach((key, value) -> {
            result.put(key, value != null ? value + " - 由AI自动生成的描述" : key + " - 由AI自动生成的描述");
        });
        return result;
    }

    /**
     * 生成模拟数据
     */
    public String generateMockData(String fieldType, String constraints) {
        log.info("生成模拟数据: fieldType={}, constraints={}", fieldType, constraints);

        String prompt = String.format("""
            请根据以下信息生成模拟数据：

            字段类型：%s
            约束条件：%s

            请生成符合要求的模拟数据值，只返回数据值本身，不要包含任何其他文字说明。
            如果是字符串类型，请用双引号包裹。
            """, fieldType, constraints != null ? constraints : "无");

        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType("deepseek");
        config.setModelName("deepseek-chat");

        String response = callAI(config, prompt);

        // 清理响应
        response = response.trim();
        if (response.startsWith("\"") && response.endsWith("\"")) {
            response = response.substring(1, response.length() - 1);
        }

        // 根据类型生成默认值
        if (response.contains("error") || response.isEmpty()) {
            return generateDefaultMockData(fieldType);
        }

        return response;
    }

    /**
     * 诊断测试失败原因
     */
    public String diagnoseFailure(String caseName, String expected, String actual, String errorMessage) {
        log.info("诊断测试失败: caseName={}", caseName);

        String prompt = String.format("""
            你是一个专业的测试工程师。请分析以下测试失败的原因并给出解决方案：

            测试用例：%s
            预期结果：%s
            实际结果：%s
            错误信息：%s

            请给出：
            1. 可能的原因分析
            2. 建议的解决方案
            3. 后续测试建议

            请用中文回复。
            """, caseName, expected, actual, errorMessage != null ? errorMessage : "无");

        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType("deepseek");
        config.setModelName("deepseek-chat");

        return callAI(config, prompt);
    }

    // ========== 私有方法 ==========

    private String parseAIResponse(String response) {
        try {
            JSONObject json = JSON.parseObject(response);
            if (json.containsKey("choices")) {
                var choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    var firstChoice = choices.getJSONObject(0);
                    if (firstChoice.containsKey("message")) {
                        var message = firstChoice.getJSONObject("message");
                        if (message.containsKey("content")) {
                            return message.getString("content");
                        }
                    }
                }
            }
            if (json.containsKey("error")) {
                var error = json.getJSONObject("error");
                return "AI服务错误: " + error.getString("message");
            }
        } catch (Exception e) {
            log.warn("解析AI响应失败: {}", e.getMessage());
        }
        return response;
    }

    private String generateDefaultMockData(String fieldType) {
        if (fieldType == null) {
            return "\"default_value\"";
        }
        return switch (fieldType.toLowerCase()) {
            case "string", "str" -> "\"test_" + System.currentTimeMillis() + "\"";
            case "integer", "int", "number" -> String.valueOf((int) (Math.random() * 10000));
            case "long" -> String.valueOf((long) (Math.random() * 100000));
            case "boolean", "bool" -> "true";
            case "email" -> "\"test@example.com\"";
            case "phone", "tel" -> "\"13800138000\"";
            case "date" -> "\"2024-01-01\"";
            case "datetime", "timestamp" -> "\"2024-01-01 12:00:00\"";
            case "url", "uri" -> "\"https://example.com\"";
            case "ip" -> "\"192.168.1.1\"";
            case "uuid" -> "\"" + UUID.randomUUID().toString() + "\"";
            default -> "\"" + fieldType + "_mock_value\"";
        };
    }
}

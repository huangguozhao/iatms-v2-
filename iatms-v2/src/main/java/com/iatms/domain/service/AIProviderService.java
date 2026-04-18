package com.iatms.domain.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.domain.model.entity.AIServiceConfig;
import com.iatms.infrastructure.config.DeepSeekConfig;
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
    private final DeepSeekConfig deepSeekConfig;

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
     * 流式调用 AI 服务 (SSE)
     * @return Flux<String> 每个元素是一个 content chunk
     */
    public reactor.core.publisher.Flux<String> callAIStream(AIServiceConfig config, String prompt) {
        log.info("流式调用AI服务: serviceType={}, model={}", config.getServiceType(), config.getModelName());

        try {
            // 构建请求 - 启用流式输出
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

            requestBody.put("stream", true);

            // 调用 API
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://api.deepseek.com";
            String apiKey = config.getApiKey();

            log.info("DeepSeek API 请求 - baseUrl: {}, model: {}, prompt长度: {}, apiKey长度: {}",
                    baseUrl, config.getModelName(), prompt.length(),
                    apiKey != null ? apiKey.length() : 0);

            return webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, "text/event-stream")
                    .bodyValue(JSON.toJSONString(requestBody))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .doOnNext(line -> log.info("收到原始响应行: [{}]", line))
                    .flatMap(line -> {
                        try {
                            if (line == null || line.isEmpty()) {
                                return reactor.core.publisher.Flux.empty();
                            }

                            String trimmedLine = line.trim();

                            // 跳过 SSE 的 "data: " 前缀
                            if (trimmedLine.startsWith("data:")) {
                                trimmedLine = trimmedLine.substring(5).trim();
                            }

                            // 检查是否是 [DONE] 或 [[DONE]]
                            if (trimmedLine.contains("[DONE]")) {
                                log.info("收到 [DONE]，流式结束");
                                return reactor.core.publisher.Flux.empty();
                            }

                            // 解析 JSON 可能是数组格式或单个对象
                            String content = extractContentFromJson(trimmedLine);
                            if (content != null && !content.isEmpty()) {
                                log.info("提取到AI内容: {}", content);
                                return reactor.core.publisher.Flux.just(content);
                            }
                        } catch (Exception e) {
                            log.warn("解析响应行失败: {}, error: {}", line, e.getMessage());
                        }
                        return reactor.core.publisher.Flux.empty();
                    })
                    .onErrorResume(e -> {
                        log.error("流式AI服务调用失败: {}", e.getMessage(), e);
                        return reactor.core.publisher.Flux.just("【错误】AI服务调用失败: " + e.getMessage());
                    });

        } catch (Exception e) {
            log.error("流式AI服务调用异常: serviceType={}, error={}", config.getServiceType(), e.getMessage());
            return reactor.core.publisher.Flux.just("【错误】AI服务调用失败: " + e.getMessage());
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
        config.setModelName(deepSeekConfig.getModel());
        config.setBaseUrl(deepSeekConfig.getBaseUrl());
        config.setApiKey(deepSeekConfig.getKey());
        config.setMaxTokens(deepSeekConfig.getMaxTokens());

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
        config.setModelName(deepSeekConfig.getModel());
        config.setApiKey(deepSeekConfig.getKey());
        config.setBaseUrl(deepSeekConfig.getBaseUrl());

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
        config.setModelName(deepSeekConfig.getModel());
        config.setApiKey(deepSeekConfig.getKey());
        config.setBaseUrl(deepSeekConfig.getBaseUrl());

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
        config.setModelName(deepSeekConfig.getModel());
        config.setApiKey(deepSeekConfig.getKey());
        config.setBaseUrl(deepSeekConfig.getBaseUrl());
        config.setMaxTokens(deepSeekConfig.getMaxTokens());

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

    /**
     * 从 JSON 字符串提取 AI 内容
     * 支持多种格式：
     * 1. JSON数组格式: [{"id":"...","choices":[{"delta":{"content":"xxx"}}]}]
     * 2. JSON对象格式: {"id":"...","choices":[{"delta":{"content":"xxx"}}]}
     */
    private String extractContentFromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            log.info("extractContentFromJson: 输入为空");
            return null;
        }

        log.info("extractContentFromJson 开始解析: {}", jsonStr.substring(0, Math.min(200, jsonStr.length())));

        try {
            // 尝试解析为 JSON 数组
            if (jsonStr.startsWith("[")) {
                JSONArray jsonArray = JSON.parseArray(jsonStr);
                log.info("解析为JSON数组, 大小: {}", jsonArray != null ? jsonArray.size() : 0);
                if (jsonArray != null && !jsonArray.isEmpty()) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        log.info("数组元素[{}] keys: {}", i, obj.keySet());
                        String content = extractFromJsonObject(obj);
                        if (content != null) {
                            log.info("从数组元素[{}]提取到内容: {}", i, content);
                            return content;
                        }
                    }
                }
            } else {
                // 尝试解析为单个 JSON 对象
                JSONObject json = JSON.parseObject(jsonStr);
                log.info("解析为JSON对象, keys: {}", json.keySet());
                return extractFromJsonObject(json);
            }
        } catch (Exception e) {
            log.warn("提取AI内容失败: {}, error: {}", jsonStr, e.getMessage());
        }
        log.info("extractContentFromJson 未提取到内容");
        return null;
    }

    /**
     * 从 JSON 对象中提取 content
     */
    private String extractFromJsonObject(JSONObject json) {
        if (json == null) return null;

        try {
            // 通用格式: choices[0].delta.content
            if (json.containsKey("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    if (choice != null) {
                        log.info("choice keys: {}", choice.keySet());
                        // delta 格式 (流式)
                        if (choice.containsKey("delta")) {
                            JSONObject delta = choice.getJSONObject("delta");
                            log.info("delta keys: {}", delta != null ? delta.keySet() : "null");
                            if (delta != null && delta.containsKey("content")) {
                                String content = delta.getString("content");
                                log.info("从delta.content提取到: {}", content);
                                return content;
                            }
                        }
                        // message 格式 (非流式)
                        if (choice.containsKey("message")) {
                            JSONObject message = choice.getJSONObject("message");
                            if (message != null && message.containsKey("content")) {
                                return message.getString("content");
                            }
                        }
                    }
                } else {
                    log.info("choices数组为空");
                }
            } else {
                log.info("json不包含choices键, keys: {}", json.keySet());
            }
        } catch (Exception e) {
            log.warn("从JSON对象提取内容失败: {}, error: {}", json.toJSONString(), e.getMessage());
        }
        return null;
    }
}

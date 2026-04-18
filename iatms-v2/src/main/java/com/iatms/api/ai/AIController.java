package com.iatms.api.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.iatms.api.common.ApiResponse;
import com.iatms.common.annotation.RequirePermission;
import com.iatms.domain.model.entity.AIServiceConfig;
import com.iatms.domain.model.enums.ProjectPermission;
import com.iatms.domain.service.AIProviderService;
import com.iatms.domain.service.TestDiagnosisService;
import com.iatms.infrastructure.config.DeepSeekConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI 辅助控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIProviderService aiProviderService;
    private final TestDiagnosisService testDiagnosisService;
    private final DeepSeekConfig deepSeekConfig;

    // SSE超时时间：10分钟
    private static final long SSE_TIMEOUT = TimeUnit.MINUTES.toMillis(10);

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
     * 诊断测试失败原因 - 异步SSE版本（推荐）
     * 前端可以通过SSE实时接收AI诊断结果，避免超时
     * 注意：此端点通过query参数传递token进行简单认证
     */
    @GetMapping(value = "/diagnose-failure/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter diagnoseFailureSse(@RequestParam String executionId) {
        log.info("AI诊断测试失败 (SSE): executionId={}", executionId);

        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        final reactor.core.Disposable[] subscriptionHolder = new reactor.core.Disposable[1];
        final boolean[] emitterCompleted = {false};  // 标记emitter是否已完成

        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("emitter.onTimeout: executionId={}", executionId);
            subscriptionHolder[0].dispose();
            SecurityContextHolder.clearContext();
        });

        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("emitter.onCompletion: executionId={}, emitterCompleted={}", executionId, emitterCompleted[0]);
            emitterCompleted[0] = true;
            if (subscriptionHolder[0] != null && !subscriptionHolder[0].isDisposed()) {
                subscriptionHolder[0].dispose();
                log.info("订阅已取消");
            }
            SecurityContextHolder.clearContext();
        });

        // 设置错误回调
        emitter.onError((Throwable t) -> {
            log.error("emitter.onError: executionId={}, error={}", executionId, t.getMessage());
            if (emitterCompleted[0]) {
                log.info("emitter已完成，忽略onError");
                return;
            }
            emitterCompleted[0] = true;
            if (subscriptionHolder[0] != null && !subscriptionHolder[0].isDisposed()) {
                subscriptionHolder[0].dispose();
            }
        });

        // 捕获当前安全上下文
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        // 创建异步任务
        Runnable task = () -> {
            // 将安全上下文设置到当前线程
            SecurityContextHolder.setContext(securityContext);

            try {
                // 1. 构建诊断上下文
                TestDiagnosisService.DiagnosisContext context = testDiagnosisService.buildDiagnosisContext(executionId);

                if (context.totalResults == 0) {
                    emitter.send(SseEmitter.event().name("error").data("未找到测试结果数据"));
                    emitter.complete();
                    return;
                }

                // 2. 发送初始上下文信息
                emitter.send(SseEmitter.event().name("context").data(JSON.toJSONString(context)));

                // 3. 构建prompt
                String prompt = testDiagnosisService.buildDiagnosisPrompt(context);
                log.info("AI诊断prompt长度: {}", prompt.length());
                log.info("DeepSeek配置 - baseUrl: {}, model: {}, key长度: {}",
                        deepSeekConfig.getBaseUrl(), deepSeekConfig.getModel(),
                        deepSeekConfig.getKey() != null ? deepSeekConfig.getKey().length() : 0);

                // 4. 构建AI配置
                AIServiceConfig config = new AIServiceConfig();
                config.setServiceType("deepseek");
                config.setModelName(deepSeekConfig.getModel());
                config.setApiKey(deepSeekConfig.getKey());
                config.setBaseUrl(deepSeekConfig.getBaseUrl());
                config.setMaxTokens(deepSeekConfig.getMaxTokens());

                // 5. 流式调用AI
                StringBuilder fullResponse = new StringBuilder();
                log.info("开始调用AI流式接口...");

                // 保存订阅对象到共享数组
                subscriptionHolder[0] = aiProviderService.callAIStream(config, prompt)
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String chunk) {
                                if (emitterCompleted[0]) {
                                    log.info("emitter已完成，忽略chunk");
                                    return;
                                }
                                try {
                                    fullResponse.append(chunk);
                                    log.info("SSE发送chunk给前端: length={}, content={}", chunk.length(), chunk);
                                    emitter.send(SseEmitter.event().name("chunk").data(chunk));
                                } catch (Exception e) {
                                    log.warn("SSE发送chunk失败: {}", e.getMessage());
                                }
                            }
                        }, (Consumer<Throwable>) error -> {
                            log.error("Flux onError: {}", error.getMessage());
                            if (emitterCompleted[0]) {
                                log.info("emitter已关闭，忽略onError");
                                return;
                            }
                            try {
                                log.info("发送error事件: AI调用失败");
                                emitter.send(SseEmitter.event().name("error").data("AI调用失败: " + error.getMessage()));
                                emitter.complete();
                            } catch (Exception e) {
                                log.warn("SSE error发送失败: {}", e.getMessage());
                            }
                        }, () -> {
                            log.info("Flux onComplete: 完整响应长度={}", fullResponse.length());
                            if (emitterCompleted[0]) {
                                log.info("emitter已完成，忽略done发送");
                                return;
                            }
                            try {
                                String responseStr = fullResponse.toString();
                                log.info("开始解析诊断结果，响应内容长度={}, 内容前100字符: {}",
                                    responseStr.length(),
                                    responseStr.substring(0, Math.min(100, responseStr.length())));
                                Map<String, Object> result = parseDiagnosisResult(responseStr, buildParamsFromContext(context));
                                log.info("解析诊断结果成功: severity={}, rootCause={}", result.get("severity"), result.get("rootCause"));
                                log.info("准备发送done事件");
                                emitter.send(SseEmitter.event().name("done").data(JSON.toJSONString(result)));
                                log.info("done事件发送成功，准备调用emitter.complete()");
                                emitter.complete();
                                log.info("emitter.complete()调用完成");
                            } catch (Exception e) {
                                log.error("发送诊断结果失败: {}, stack={}", e.getMessage(), e.getStackTrace());
                                try {
                                    log.info("准备发送error事件");
                                    emitter.send(SseEmitter.event().name("error").data("诊断结果解析失败: " + e.getMessage()));
                                    log.info("error事件发送成功");
                                    emitter.completeWithError(e);
                                } catch (Exception ex) {
                                    log.warn("发送error事件并complete失败: {}", ex.getMessage());
                                }
                            }
                        });

            } catch (Exception e) {
                log.error("AI诊断SSE异常: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("诊断失败: " + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.warn("SSE异常完成失败: {}", ex.getMessage());
                }
            } finally {
                // 清理安全上下文
                SecurityContextHolder.clearContext();
            }
        };

        // 使用 DelegatingSecurityContextRunnable 包装任务，自动传播 SecurityContext
        DelegatingSecurityContextRunnable wrappedTask = new DelegatingSecurityContextRunnable(task);

        // 使用单线程池执行
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(wrappedTask);

        return emitter;
    }

    /**
     * 诊断测试失败原因 - 同步版本（简单场景，单个结果）
     */
    @PostMapping("/diagnose-failure")
    @RequirePermission(value = ProjectPermission.AI_DIAGNOSE, requireProjectId = false)
    public ApiResponse<Map<String, Object>> diagnoseFailure(@RequestBody Map<String, Object> params) {
        log.info("AI诊断测试失败: params={}", params);

        String executionId = (String) params.get("executionId");

        if (executionId == null || executionId.isEmpty()) {
            return ApiResponse.error(400, "executionId不能为空");
        }

        // 构建诊断上下文
        TestDiagnosisService.DiagnosisContext context = testDiagnosisService.buildDiagnosisContext(executionId);

        if (context.totalResults == 0) {
            return ApiResponse.error(404, "未找到测试结果数据");
        }

        // 构建prompt
        String prompt = testDiagnosisService.buildDiagnosisPrompt(context);
        log.info("AI诊断prompt长度: {}", prompt.length());

        // 调用AI - 使用完整prompt
        AIServiceConfig config = new AIServiceConfig();
        config.setServiceType("deepseek");
        config.setModelName(deepSeekConfig.getModel());
        config.setApiKey(deepSeekConfig.getKey());
        config.setBaseUrl(deepSeekConfig.getBaseUrl());
        config.setMaxTokens(deepSeekConfig.getMaxTokens());

        // 直接调用callAI，使用构建好的完整prompt
        String diagnosis = aiProviderService.callAI(config, prompt);
        log.info("AI诊断响应长度: {}, 响应内容: {}", diagnosis.length(), diagnosis.substring(0, Math.min(200, diagnosis.length())));

        // 解析诊断结果
        Map<String, Object> result = parseDiagnosisResult(diagnosis, buildParamsFromContext(context));
        result.put("executionId", executionId);
        result.put("context", context);

        return ApiResponse.success(result);
    }

    /**
     * 从诊断上下文构建参数Map
     */
    private Map<String, Object> buildParamsFromContext(TestDiagnosisService.DiagnosisContext context) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("caseName", context.scopeName);
        params.put("totalCases", context.totalResults);
        params.put("passedCases", context.passedCount);
        params.put("failedCases", context.failedCount);
        params.put("environment", context.environment);
        params.put("errorMessage", context.errorMessage);
        return params;
    }

    /**
     * 解析诊断结果为结构化数据
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

        // 提取摘要 - 从Markdown中提取第一段主要内容作为摘要
        result.put("summary", extractSummary(diagnosis));

        // 提取关键发现 - 从Markdown表格中提取关键信息
        result.put("keyFindings", extractKeyFindings(diagnosis));

        // 保留原始分析文本（清理后的Markdown）
        result.put("analysis", cleanMarkdown(diagnosis));

        return result;
    }

    /**
     * 清理Markdown文本，移除表格等难以阅读的部分
     */
    private String cleanMarkdown(String markdown) {
        if (markdown == null) return "";
        // 移除表格行（| ... | 格式）
        String cleaned = markdown.replaceAll("(?m)^\\|.*\\|$", "");
        // 移除Markdown标题符号，保留标题文字
        cleaned = cleaned.replaceAll("(?m)^#+\\s*", "");
        // 移除加粗标记
        cleaned = cleaned.replaceAll("\\*\\*", "");
        // 移除多余的空行
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        return cleaned.trim();
    }

    /**
     * 从诊断文本中提取摘要
     */
    private String extractSummary(String diagnosis) {
        if (diagnosis == null) return "";
        String[] lines = diagnosis.split("\n");
        StringBuilder summary = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            // 跳过空行、表格行、标题行
            line = line.trim();
            if (line.isEmpty() || line.startsWith("|") || line.startsWith("#") || line.startsWith("**")) {
                continue;
            }
            // 移除 Markdown 格式符号
            line = line.replaceAll("^[*\\-\\d.\\s]+", "").trim();
            if (!line.isEmpty() && count < 3) {
                summary.append(line).append("；");
                count++;
            }
        }
        String result = summary.toString();
        if (result.endsWith("；")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.isEmpty() ? "AI正在分析中..." : result;
    }

    /**
     * 从诊断文本中提取关键发现
     */
    private java.util.List<Map<String, String>> extractKeyFindings(String diagnosis) {
        java.util.List<Map<String, String>> findings = new java.util.ArrayList<>();
        if (diagnosis == null) return findings;

        String[] lines = diagnosis.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 提取包含关键信息的行
            if ((line.contains("原因") || line.contains("问题") || line.contains("错误") || line.contains("风险"))
                && !line.startsWith("|") && !line.startsWith("#") && line.length() < 200) {
                // 清理Markdown符号
                String cleaned = line.replaceAll("^[*\\-\\d.\\s]+", "").replaceAll("\\*\\*", "").trim();
                if (!cleaned.isEmpty()) {
                    String severity = "medium";
                    if (cleaned.contains("高") || cleaned.contains("严重") || cleaned.contains("关键")) {
                        severity = "high";
                    }
                    findings.add(java.util.Map.of(
                            "title", cleaned.length() > 50 ? cleaned.substring(0, 50) + "..." : cleaned,
                            "severity", severity
                    ));
                }
            }
            if (findings.size() >= 5) break;  // 最多5条关键发现
        }
        return findings;
    }

    private String analyzeSeverity(String diagnosis) {
        if (diagnosis == null) return "low";
        String lower = diagnosis.toLowerCase();
        if (lower.contains("严重") || lower.contains("高风险") || lower.contains("关键") || lower.contains("blocking")) {
            return "high";
        } else if (lower.contains("中等") || lower.contains("警告") || lower.contains("warning")) {
            return "medium";
        }
        return "low";
    }

    private String extractRootCause(String diagnosis) {
        if (diagnosis == null) return "无法确定根本原因";
        // 简单提取第一个明确的原因描述
        String[] lines = diagnosis.split("\n");
        for (String line : lines) {
            if (line.contains("原因") || line.contains("可能原因") || line.contains("根本原因") || line.contains("root cause")) {
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
            if ((line.contains("问题") || line.contains("issue") || line.contains("issue")) && line.length() < 300) {
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
            if (line.contains("建议") || line.contains("修复") || line.contains("解决方案") ||
                    line.contains("suggestion") || line.contains("fix") || line.contains("solution")) {
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

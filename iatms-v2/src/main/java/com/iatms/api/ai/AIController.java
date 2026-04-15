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
}

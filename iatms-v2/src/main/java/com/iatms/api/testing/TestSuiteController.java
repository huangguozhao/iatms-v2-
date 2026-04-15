package com.iatms.api.testing;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.testing.TestSuiteCommandService;
import com.iatms.application.testing.TestSuiteQueryService;
import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.entity.TestSuite;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test-suites")
@RequiredArgsConstructor
public class TestSuiteController {

    private final TestSuiteCommandService testSuiteCommandService;
    private final TestSuiteQueryService testSuiteQueryService;

    @GetMapping
    public ApiResponse<Page<TestSuite>> query(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(testSuiteQueryService.query(keyword, projectId != null ? projectId.intValue() : null, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<TestSuite> getDetail(@PathVariable Long id) {
        return ApiResponse.success(testSuiteQueryService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody CreateTestSuiteRequest request) {
        TestSuite suite = new TestSuite();
        suite.setSuiteName(request.getName());
        suite.setProjectId(request.getProjectId() != null ? request.getProjectId().intValue() : null);
        suite.setDescription(request.getDescription());
        suite.setStatus("active");
        suite.setSuiteType("MODULE");
        Long id = testSuiteCommandService.create(suite, request.getCaseIds());
        return ApiResponse.success(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CreateTestSuiteRequest request) {
        TestSuite suite = new TestSuite();
        suite.setSuiteName(request.getName());
        suite.setProjectId(request.getProjectId() != null ? request.getProjectId().intValue() : null);
        suite.setDescription(request.getDescription());
        testSuiteCommandService.update(id, suite, request.getCaseIds());
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        testSuiteCommandService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        testSuiteCommandService.enable(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        testSuiteCommandService.disable(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<Long> execute(@PathVariable Long id) {
        // TODO: 触发执行
        return ApiResponse.success(id);
    }

    @GetMapping("/{id}/cases")
    public ApiResponse<List<Long>> getCaseIds(@PathVariable Long id) {
        return ApiResponse.success(testSuiteQueryService.getCaseIds(id));
    }

    public static class CreateTestSuiteRequest {
        private String name;
        private Long projectId;
        private List<Long> caseIds;
        private String executionStrategy;
        private String failStrategy;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public List<Long> getCaseIds() { return caseIds; }
        public void setCaseIds(List<Long> caseIds) { this.caseIds = caseIds; }
        public String getExecutionStrategy() { return executionStrategy; }
        public void setExecutionStrategy(String executionStrategy) { this.executionStrategy = executionStrategy; }
        public String getFailStrategy() { return failStrategy; }
        public void setFailStrategy(String failStrategy) { this.failStrategy = failStrategy; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

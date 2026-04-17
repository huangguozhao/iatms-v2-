package com.iatms.application.project.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.project.ModuleQueryService;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.Module;
import com.iatms.domain.model.entity.Project;
import com.iatms.domain.model.entity.TestCase;
import com.iatms.domain.model.entity.TestExecution;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ModuleDetailVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ModuleMapper;
import com.iatms.infrastructure.persistence.mapper.ProjectMapper;
import com.iatms.infrastructure.persistence.mapper.TestCaseMapper;
import com.iatms.infrastructure.persistence.mapper.TestExecutionMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模块查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleQueryServiceImpl implements ModuleQueryService {

    private final ModuleMapper moduleMapper;
    private final ProjectMapper projectMapper;
    private final ApiRequestMapper apiRequestMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestExecutionMapper testExecutionMapper;
    private final UserMapper userMapper;

    @Override
    public ModuleDetailVO getModuleDetail(Long moduleId) {
        Module module = moduleMapper.selectById(moduleId.intValue());
        if (module == null || module.getIsDeleted()) {
            throw new ResourceNotFoundException(ErrorCode.MODULE_NOT_FOUND.getCode(),
                    ErrorCode.MODULE_NOT_FOUND.getMessage());
        }

        // 获取项目信息
        Project project = null;
        if (module.getProjectId() != null) {
            project = projectMapper.selectById(module.getProjectId());
        }

        // 获取父模块信息
        String parentName = null;
        if (module.getParentModuleId() != null) {
            Module parent = moduleMapper.selectById(module.getParentModuleId());
            if (parent != null) {
                parentName = parent.getName();
            }
        }

        // 获取负责人信息
        String ownerName = null;
        if (module.getOwnerId() != null) {
            User owner = userMapper.selectById(module.getOwnerId());
            if (owner != null) {
                ownerName = owner.getDisplayName();
            }
        }

        // 获取创建人信息
        String creatorName = null;
        if (module.getCreatedBy() != null) {
            User creator = userMapper.selectById(module.getCreatedBy());
            if (creator != null) {
                creatorName = creator.getDisplayName();
            }
        }

        // 统计接口数量
        Integer apiCount = apiRequestMapper.selectCount(
                new LambdaQueryWrapper<ApiRequest>()
                        .eq(ApiRequest::getModuleId, moduleId.intValue())
                        .eq(ApiRequest::getDeleted, false)
        ).intValue();

        // 获取该模块下的所有API
        List<ApiRequest> moduleApis = apiRequestMapper.selectList(
                new LambdaQueryWrapper<ApiRequest>()
                        .eq(ApiRequest::getModuleId, moduleId.intValue())
                        .eq(ApiRequest::getDeleted, false)
        );
        List<Long> apiIds = moduleApis.stream()
                .filter(a -> a.getId() != null)
                .map(a -> a.getId().longValue())
                .collect(Collectors.toList());

        // 统计测试用例数量
        Integer testCaseCount = 0;
        Integer passedCount = 0;
        Integer failedCount = 0;
        Integer notExecutedCount = 0;

        if (!apiIds.isEmpty()) {
            testCaseCount = testCaseMapper.selectCount(
                    new LambdaQueryWrapper<TestCase>()
                            .in(TestCase::getApiId, apiIds.stream().map(Long::intValue).collect(Collectors.toList()))
                            .eq(TestCase::getDeleted, false)
            ).intValue();

            // 获取所有用例
            List<TestCase> testCases = testCaseMapper.selectList(
                    new LambdaQueryWrapper<TestCase>()
                            .in(TestCase::getApiId, apiIds.stream().map(Long::intValue).collect(Collectors.toList()))
                            .eq(TestCase::getDeleted, false)
            );

            notExecutedCount = testCaseCount; // 初始化为全部未执行

            for (TestCase tc : testCases) {
                if (tc.getId() == null) continue;

                // 查询该用例的最新执行记录
                List<TestExecution> executions = testExecutionMapper.selectList(
                        new LambdaQueryWrapper<TestExecution>()
                                .eq(TestExecution::getExecutionScope, "test_case")
                                .eq(TestExecution::getRefId, tc.getId().intValue())
                                .orderByDesc(TestExecution::getStartTime)
                                .last("LIMIT 1")
                );

                if (!executions.isEmpty()) {
                    TestExecution latestExecution = executions.get(0);
                    notExecutedCount--; // 有执行记录的用例，从未执行中扣除

                    // 根据执行状态统计
                    String status = latestExecution.getStatus();
                    if ("completed".equals(status) || "passed".equals(status) || "success".equals(status)) {
                        passedCount++;
                    } else if ("failed".equals(status) || "error".equals(status)) {
                        failedCount++;
                    }
                }
            }

            // 确保不会变成负数
            notExecutedCount = Math.max(0, notExecutedCount);
        }

        return ModuleDetailVO.builder()
                .id(module.getModuleId().longValue())
                .name(module.getName())
                .code(module.getModuleCode())
                .projectId(module.getProjectId() != null ? module.getProjectId().longValue() : null)
                .projectName(project != null ? project.getName() : null)
                .parentId(module.getParentModuleId() != null ? module.getParentModuleId().longValue() : null)
                .parentName(parentName)
                .description(module.getDescription())
                .status(module.getStatus())
                .ownerId(module.getOwnerId() != null ? module.getOwnerId().longValue() : null)
                .ownerName(ownerName)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .creatorName(creatorName)
                .stats(ModuleDetailVO.Stats.builder()
                        .apiCount(apiCount)
                        .testCaseCount(testCaseCount)
                        .passedCount(passedCount)
                        .failedCount(failedCount)
                        .notExecutedCount(notExecutedCount)
                        .build())
                .build();
    }

    @Override
    public List<ModuleDetailVO> getModulesByProject(Long projectId) {
        List<Module> modules = moduleMapper.selectList(
                new LambdaQueryWrapper<Module>()
                        .eq(Module::getProjectId, projectId.intValue())
                        .eq(Module::getIsDeleted, false)
                        .orderByAsc(Module::getSortOrder)
                        .orderByDesc(Module::getCreatedAt)
        );
        return modules.stream().map(module -> {
            Project proj = projectMapper.selectById(module.getProjectId());
            String parentName = null;
            if (module.getParentModuleId() != null) {
                Module parent = moduleMapper.selectById(module.getParentModuleId());
                if (parent != null) parentName = parent.getName();
            }
            return ModuleDetailVO.builder()
                    .id(module.getModuleId().longValue())
                    .name(module.getName())
                    .code(module.getModuleCode())
                    .projectId(module.getProjectId() != null ? module.getProjectId().longValue() : null)
                    .projectName(proj != null ? proj.getName() : null)
                    .parentId(module.getParentModuleId() != null ? module.getParentModuleId().longValue() : null)
                    .parentName(parentName)
                    .description(module.getDescription())
                    .status(module.getStatus())
                    .ownerId(module.getOwnerId() != null ? module.getOwnerId().longValue() : null)
                    .createdAt(module.getCreatedAt())
                    .updatedAt(module.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}

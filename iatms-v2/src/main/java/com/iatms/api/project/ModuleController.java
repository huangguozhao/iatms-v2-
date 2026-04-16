package com.iatms.api.project;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.project.ModuleQueryService;
import com.iatms.domain.model.vo.ModuleDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 模块管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleQueryService moduleQueryService;

    /**
     * 获取模块详情
     */
    @GetMapping("/{moduleId}")
    public ApiResponse<ModuleDetailVO> getModuleDetail(@PathVariable Long moduleId) {
        ModuleDetailVO result = moduleQueryService.getModuleDetail(moduleId);
        return ApiResponse.success(result);
    }
}

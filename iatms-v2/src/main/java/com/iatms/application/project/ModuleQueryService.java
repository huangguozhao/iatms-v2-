package com.iatms.application.project;

import com.iatms.domain.model.vo.ModuleDetailVO;

import java.util.List;

/**
 * 模块查询服务接口
 */
public interface ModuleQueryService {

    /**
     * 获取模块详情
     */
    ModuleDetailVO getModuleDetail(Long moduleId);

    /**
     * 获取项目下的所有模块
     */
    List<ModuleDetailVO> getModulesByProject(Long projectId);
}

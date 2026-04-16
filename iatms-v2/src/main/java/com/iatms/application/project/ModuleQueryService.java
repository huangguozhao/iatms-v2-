package com.iatms.application.project;

import com.iatms.domain.model.vo.ModuleDetailVO;

/**
 * 模块查询服务接口
 */
public interface ModuleQueryService {

    /**
     * 获取模块详情
     */
    ModuleDetailVO getModuleDetail(Long moduleId);
}

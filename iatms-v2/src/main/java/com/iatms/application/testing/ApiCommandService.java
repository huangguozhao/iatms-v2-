package com.iatms.application.testing;

import com.iatms.application.testing.dto.command.CreateApiRequestCmd;
import com.iatms.domain.model.vo.ApiDetailVO;

/**
 * API 命令服务接口
 */
public interface ApiCommandService {

    /**
     * 创建 API
     */
    ApiDetailVO createApi(CreateApiRequestCmd cmd, Long userId);

    /**
     * 更新 API
     */
    ApiDetailVO updateApi(Long apiId, CreateApiRequestCmd cmd, Long userId);

    /**
     * 删除 API
     */
    void deleteApi(Long apiId, Long userId);
}

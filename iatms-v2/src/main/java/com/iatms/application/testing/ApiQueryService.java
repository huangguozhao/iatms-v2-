package com.iatms.application.testing;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.testing.dto.query.ApiQuery;
import com.iatms.domain.model.vo.ApiDetailVO;
import com.iatms.domain.model.vo.ApiSummaryVO;

import java.util.List;

/**
 * API 查询服务接口
 */
public interface ApiQueryService {

    /**
     * 分页查询 API 列表
     */
    ApiResponse.PageResult<ApiSummaryVO> queryApis(ApiQuery query, Long userId);

    /**
     * 获取 API 详情
     */
    ApiDetailVO getApiDetail(Long apiId);

    /**
     * 获取项目下的所有 API（树形结构）
     */
    List<?> getApiTree(Long projectId);

    /**
     * 批量获取 API 详情
     */
    List<ApiDetailVO> getApiDetails(List<Long> apiIds);
}

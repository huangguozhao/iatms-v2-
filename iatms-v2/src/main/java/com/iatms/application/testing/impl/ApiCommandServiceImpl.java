package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.testing.ApiCommandService;
import com.iatms.application.testing.dto.command.CreateApiRequestCmd;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.ApiCollection;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ApiDetailVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ApiCollectionMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.BusinessException;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.domain.model.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API 命令服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCommandServiceImpl implements ApiCommandService {

    private final ApiRequestMapper apiRequestMapper;
    private final ApiCollectionMapper collectionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiDetailVO createApi(CreateApiRequestCmd cmd, Long userId) {
        // 验证集合存在
        ApiCollection collection = collectionMapper.selectById(cmd.getCollectionId());
        if (collection == null) {
            throw new ResourceNotFoundException("集合不存在");
        }

        ApiRequest api = new ApiRequest();
        api.setName(cmd.getName());
        api.setDescription(cmd.getDescription());
        api.setCollectionId(cmd.getCollectionId());
        api.setRequestType(cmd.getRequestType());
        api.setHttpMethod(cmd.getHttpMethod());
        api.setUrl(cmd.getUrl());
        api.setHeaders(cmd.getHeaders());
        api.setQueryParams(cmd.getQueryParams());
        api.setRequestBody(cmd.getRequestBody());
        api.setAuthConfig(cmd.getAuthConfig());
        api.setPreScript(cmd.getPreScript());
        api.setPostScript(cmd.getPostScript());
        api.setAssertions(cmd.getAssertions());
        api.setOrderNum(cmd.getOrderNum());
        api.setStatus(cmd.getStatus());
        api.setCreatedBy(userId);
        api.setUpdatedBy(userId);

        apiRequestMapper.insert(api);

        log.info("创建API成功: id={}, name={}, userId={}", api.getId(), api.getName(), userId);

        return getApiDetail(api.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiDetailVO updateApi(Long apiId, CreateApiRequestCmd cmd, Long userId) {
        ApiRequest api = apiRequestMapper.selectById(apiId);
        if (api == null) {
            throw new ResourceNotFoundException(ErrorCode.API_NOT_FOUND.getCode(),
                    ErrorCode.API_NOT_FOUND.getMessage());
        }

        if (cmd.getName() != null) api.setName(cmd.getName());
        if (cmd.getDescription() != null) api.setDescription(cmd.getDescription());
        if (cmd.getCollectionId() != null) api.setCollectionId(cmd.getCollectionId());
        if (cmd.getRequestType() != null) api.setRequestType(cmd.getRequestType());
        if (cmd.getHttpMethod() != null) api.setHttpMethod(cmd.getHttpMethod());
        if (cmd.getUrl() != null) api.setUrl(cmd.getUrl());
        if (cmd.getHeaders() != null) api.setHeaders(cmd.getHeaders());
        if (cmd.getQueryParams() != null) api.setQueryParams(cmd.getQueryParams());
        if (cmd.getRequestBody() != null) api.setRequestBody(cmd.getRequestBody());
        if (cmd.getAuthConfig() != null) api.setAuthConfig(cmd.getAuthConfig());
        if (cmd.getPreScript() != null) api.setPreScript(cmd.getPreScript());
        if (cmd.getPostScript() != null) api.setPostScript(cmd.getPostScript());
        if (cmd.getAssertions() != null) api.setAssertions(cmd.getAssertions());
        if (cmd.getOrderNum() != null) api.setOrderNum(cmd.getOrderNum());
        if (cmd.getStatus() != null) api.setStatus(cmd.getStatus());

        api.setUpdatedBy(userId);
        apiRequestMapper.updateById(api);

        log.info("更新API成功: id={}, userId={}", apiId, userId);

        return getApiDetail(apiId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApi(Long apiId, Long userId) {
        ApiRequest api = apiRequestMapper.selectById(apiId);
        if (api == null) {
            throw new ResourceNotFoundException(ErrorCode.API_NOT_FOUND.getCode(),
                    ErrorCode.API_NOT_FOUND.getMessage());
        }

        api.setDeleted(true);
        api.setUpdatedBy(userId);
        apiRequestMapper.updateById(api);

        log.info("删除API成功: id={}, userId={}", apiId, userId);
    }

    private ApiDetailVO getApiDetail(Long apiId) {
        ApiRequest api = apiRequestMapper.selectById(apiId);
        if (api == null) {
            throw new ResourceNotFoundException(ErrorCode.API_NOT_FOUND.getCode(),
                    ErrorCode.API_NOT_FOUND.getMessage());
        }

        ApiCollection collection = collectionMapper.selectById(api.getCollectionId());
        User creator = userMapper.selectById(api.getCreatedBy());

        return ApiDetailVO.builder()
                .id(api.getId())
                .name(api.getName())
                .description(api.getDescription())
                .requestType(api.getRequestType())
                .httpMethod(api.getHttpMethod())
                .url(api.getUrl())
                .headers(api.getHeaders())
                .queryParams(api.getQueryParams())
                .requestBody(api.getRequestBody())
                .authConfig(api.getAuthConfig())
                .preScript(api.getPreScript())
                .postScript(api.getPostScript())
                .assertions(api.getAssertions())
                .collectionId(api.getCollectionId())
                .collectionName(collection != null ? collection.getName() : null)
                .projectId(collection != null ? collection.getProjectId() : null)
                .orderNum(api.getOrderNum())
                .status(api.getStatus())
                .createdAt(api.getCreatedAt())
                .updatedAt(api.getUpdatedAt())
                .createdBy(api.getCreatedBy())
                .creatorName(creator != null ? creator.getDisplayName() : null)
                .build();
    }
}

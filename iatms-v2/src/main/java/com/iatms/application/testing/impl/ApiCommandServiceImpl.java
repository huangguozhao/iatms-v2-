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
        api.setCollectionId(cmd.getCollectionId() != null ? cmd.getCollectionId().intValue() : null);
        api.setRequestType(cmd.getRequestType());
        api.setHttpMethod(cmd.getHttpMethod());
        // 拆分url为path和baseUrl
        if (cmd.getUrl() != null) {
            String[] parts = splitUrl(cmd.getUrl());
            api.setBaseUrl(parts[0]);
            api.setPath(parts[1]);
        }
        if (cmd.getRequestHeaders() != null) api.setRequestHeaders(cmd.getRequestHeaders());
        if (cmd.getQueryParams() != null) api.setRequestParameters(cmd.getQueryParams());
        if (cmd.getRequestBody() != null) api.setRequestBody(cmd.getRequestBody());
        if (cmd.getAuthConfig() != null) api.setAuthConfig(cmd.getAuthConfig());
        if (cmd.getOrderNum() != null) api.setOrderNum(cmd.getOrderNum());
        if (cmd.getStatus() != null) api.setStatus(cmd.getStatus());
        if (cmd.getRequestBodyType() != null) api.setRequestBodyType(cmd.getRequestBodyType());
        if (cmd.getResponseBodyType() != null) api.setResponseBodyType(cmd.getResponseBodyType());
        if (cmd.getTags() != null) api.setTags(cmd.getTags());
        if (cmd.getTimeoutSeconds() != null) api.setTimeoutSeconds(cmd.getTimeoutSeconds());
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
        if (cmd.getCollectionId() != null) api.setCollectionId(cmd.getCollectionId().intValue());
        if (cmd.getRequestType() != null) api.setRequestType(cmd.getRequestType());
        if (cmd.getHttpMethod() != null) api.setHttpMethod(cmd.getHttpMethod());
        // 拆分url为path和baseUrl
        if (cmd.getUrl() != null) {
            String[] parts = splitUrl(cmd.getUrl());
            api.setBaseUrl(parts[0]);
            api.setPath(parts[1]);
        }
        if (cmd.getPath() != null) api.setPath(cmd.getPath());
        if (cmd.getBaseUrl() != null) api.setBaseUrl(cmd.getBaseUrl());
        if (cmd.getRequestHeaders() != null) api.setRequestHeaders(cmd.getRequestHeaders());
        if (cmd.getQueryParams() != null) api.setRequestParameters(cmd.getQueryParams());
        if (cmd.getRequestBody() != null) api.setRequestBody(cmd.getRequestBody());
        if (cmd.getAuthConfig() != null) api.setAuthConfig(cmd.getAuthConfig());
        if (cmd.getOrderNum() != null) api.setOrderNum(cmd.getOrderNum());
        if (cmd.getStatus() != null) api.setStatus(cmd.getStatus());
        if (cmd.getRequestBodyType() != null) api.setRequestBodyType(cmd.getRequestBodyType());
        if (cmd.getResponseBodyType() != null) api.setResponseBodyType(cmd.getResponseBodyType());
        if (cmd.getTags() != null) api.setTags(cmd.getTags());
        if (cmd.getTimeoutSeconds() != null) api.setTimeoutSeconds(cmd.getTimeoutSeconds());
        if (cmd.getVersion() != null) api.setApiVersion(cmd.getVersion());

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

        // 逻辑删除 - 使用 deleteById 触发 @TableLogic 注解
        apiRequestMapper.deleteById(apiId);

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
                .url(buildUrl(api.getBaseUrl(), api.getPath()))
                .baseUrl(api.getBaseUrl())
                .headers(api.getRequestHeaders())
                .queryParams(api.getRequestParameters())
                .requestBody(api.getRequestBody())
                .authConfig(api.getAuthConfig())
                .collectionId(api.getCollectionId() != null ? api.getCollectionId().longValue() : null)
                .collectionName(collection != null ? collection.getName() : null)
                .projectId(collection != null && collection.getProjectId() != null ? collection.getProjectId().longValue() : null)
                .orderNum(api.getOrderNum())
                .status(api.getStatus())
                .requestBodyType(api.getRequestBodyType())
                .responseBodyType(api.getResponseBodyType())
                .tags(api.getTags())
                .timeoutSeconds(api.getTimeoutSeconds())
                .createdAt(api.getCreatedAt())
                .updatedAt(api.getUpdatedAt())
                .createdBy(api.getCreatedBy())
                .creatorName(creator != null ? creator.getDisplayName() : null)
                .build();
    }

    /**
     * 将URL拆分为baseUrl和path
     * 如果URL包含协议://host，则拆分为baseUrl和path
     * 否则整个字符串作为path返回，baseUrl为空
     */
    private String[] splitUrl(String url) {
        if (url == null || url.isEmpty()) {
            return new String[]{"", ""};
        }
        // 查找协议://后的第一个/作为分割点
        int protocolEnd = url.indexOf("://");
        if (protocolEnd != -1) {
            int pathStart = url.indexOf("/", protocolEnd + 3);
            if (pathStart != -1) {
                return new String[]{url.substring(0, pathStart), url.substring(pathStart)};
            } else {
                return new String[]{url, "/"};
            }
        }
        // 没有协议，整个作为path
        return new String[]{"", url};
    }

    private String buildUrl(String baseUrl, String path) {
        if (baseUrl == null && path == null) return "";
        String base = baseUrl != null ? baseUrl : "";
        String p = path != null ? path : "";
        return base + p;
    }
}

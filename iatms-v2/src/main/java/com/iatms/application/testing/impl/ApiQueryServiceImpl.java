package com.iatms.application.testing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.testing.ApiQueryService;
import com.iatms.application.testing.dto.query.ApiQuery;
import com.iatms.domain.model.entity.ApiRequest;
import com.iatms.domain.model.entity.ApiCollection;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.ApiDetailVO;
import com.iatms.domain.model.vo.ApiSummaryVO;
import com.iatms.infrastructure.persistence.mapper.ApiRequestMapper;
import com.iatms.infrastructure.persistence.mapper.ApiCollectionMapper;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.ResourceNotFoundException;
import com.iatms.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API 查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiQueryServiceImpl implements ApiQueryService {

    private final ApiRequestMapper apiRequestMapper;
    private final ApiCollectionMapper collectionMapper;
    private final UserMapper userMapper;

    @Override
    public ApiResponse.PageResult<ApiSummaryVO> queryApis(ApiQuery query) {
        LambdaQueryWrapper<ApiRequest> wrapper = new LambdaQueryWrapper<>();

        if (query.getProjectId() != null) {
            // 需要通过 collection 关联查询
            List<Long> collectionIds = getCollectionIdsByProject(query.getProjectId());
            if (collectionIds.isEmpty()) {
                return ApiResponse.PageResult.of(new ArrayList<>(), 0, query.getPageNum(), query.getPageSize());
            }
            wrapper.in(ApiRequest::getCollectionId, collectionIds);
        }

        if (query.getCollectionId() != null) {
            wrapper.eq(ApiRequest::getCollectionId, query.getCollectionId());
        }

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(ApiRequest::getName, query.getKeyword())
                    .or().like(ApiRequest::getUrl, query.getKeyword()));
        }

        if (query.getHttpMethod() != null) {
            wrapper.eq(ApiRequest::getHttpMethod, query.getHttpMethod());
        }

        if (query.getStatus() != null) {
            wrapper.eq(ApiRequest::getStatus, query.getStatus());
        }

        wrapper.eq(ApiRequest::getDeleted, false);
        wrapper.orderByAsc(ApiRequest::getOrderNum);

        IPage<ApiRequest> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ApiRequest> result = apiRequestMapper.selectPage(page, wrapper);

        List<ApiSummaryVO> voList = convertToSummaryVO(result.getRecords());

        return ApiResponse.PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public ApiDetailVO getApiDetail(Long apiId) {
        ApiRequest api = apiRequestMapper.selectById(apiId);
        if (api == null || api.getDeleted()) {
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
                .collectionId(api.getCollectionId() != null ? api.getCollectionId().longValue() : null)
                .collectionName(collection != null ? collection.getName() : null)
                .projectId(collection != null && collection.getProjectId() != null ? collection.getProjectId().longValue() : null)
                .projectName(null)
                .orderNum(api.getOrderNum())
                .status(api.getStatus())
                .createdAt(api.getCreatedAt())
                .updatedAt(api.getUpdatedAt())
                .createdBy(api.getCreatedBy())
                .creatorName(creator != null ? creator.getDisplayName() : null)
                .build();
    }

    @Override
    public List<?> getApiTree(Long projectId) {
        List<Long> collectionIds = getCollectionIdsByProject(projectId);
        if (collectionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询所有集合
        LambdaQueryWrapper<ApiCollection> collectionWrapper = new LambdaQueryWrapper<>();
        collectionWrapper.eq(ApiCollection::getProjectId, projectId);
        collectionWrapper.eq(ApiCollection::getDeleted, false);
        List<ApiCollection> collections = collectionMapper.selectList(collectionWrapper);

        // 查询所有 API
        LambdaQueryWrapper<ApiRequest> apiWrapper = new LambdaQueryWrapper<>();
        apiWrapper.in(ApiRequest::getCollectionId, collectionIds);
        apiWrapper.eq(ApiRequest::getDeleted, false);
        List<ApiRequest> apis = apiRequestMapper.selectList(apiWrapper);

        // 构建树形结构
        return buildApiTree(collections, apis);
    }

    @Override
    public List<ApiDetailVO> getApiDetails(List<Long> apiIds) {
        if (apiIds == null || apiIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ApiRequest> apis = apiRequestMapper.selectBatchIds(apiIds);
        return apis.stream()
                .filter(a -> !a.getDeleted())
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());
    }

    private List<Long> getCollectionIdsByProject(Long projectId) {
        LambdaQueryWrapper<ApiCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCollection::getProjectId, projectId);
        wrapper.eq(ApiCollection::getDeleted, false);
        List<ApiCollection> collections = collectionMapper.selectList(wrapper);
        return collections.stream().map(ApiCollection::getId).collect(Collectors.toList());
    }

    private List<ApiSummaryVO> convertToSummaryVO(List<ApiRequest> apis) {
        if (apis.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取集合信息
        List<Integer> collectionIdsInt = apis.stream().map(ApiRequest::getCollectionId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Integer, ApiCollection> collectionMap = collectionMapper.selectBatchIds(collectionIdsInt).stream()
                .collect(Collectors.toMap(ApiCollection::getModuleId, c -> c));

        return apis.stream()
                .map(api -> {
                    ApiCollection collection = api.getCollectionId() != null ? collectionMap.get(api.getCollectionId()) : null;
                    return ApiSummaryVO.builder()
                            .id(api.getId())
                            .name(api.getName())
                            .httpMethod(api.getHttpMethod())
                            .url(api.getUrl())
                            .collectionId(api.getCollectionId() != null ? api.getCollectionId().longValue() : null)
                            .collectionName(collection != null ? collection.getName() : null)
                            .status(api.getStatus())
                            .updatedAt(api.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ApiDetailVO convertToDetailVO(ApiRequest api) {
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
                .assertions(api.getAssertions())
                .collectionId(api.getCollectionId() != null ? api.getCollectionId().longValue() : null)
                .orderNum(api.getOrderNum())
                .status(api.getStatus())
                .createdAt(api.getCreatedAt())
                .updatedAt(api.getUpdatedAt())
                .build();
    }

    private List<?> buildApiTree(List<ApiCollection> collections, List<ApiRequest> apis) {
        // 按 parentId 分组
        Map<Integer, List<ApiCollection>> childrenMap = collections.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ApiCollection::getParentId));

        // 按 collectionId 分组 API
        Map<Integer, List<ApiRequest>> apiMap = apis.stream()
                .collect(Collectors.groupingBy(ApiRequest::getCollectionId));

        // 构建树
        return collections.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> buildTreeNode(c, childrenMap, apiMap))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildTreeNode(ApiCollection collection,
                                               Map<Integer, List<ApiCollection>> childrenMap,
                                               Map<Integer, List<ApiRequest>> apiMap) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", collection.getModuleId());
        node.put("name", collection.getName());
        node.put("type", "collection");
        node.put("orderNum", collection.getOrderNum());

        // 子集合
        List<ApiCollection> children = childrenMap.get(collection.getModuleId());
        if (children != null && !children.isEmpty()) {
            node.put("children", children.stream()
                    .map(c -> buildTreeNode(c, childrenMap, apiMap))
                    .collect(Collectors.toList()));
        }

        // API 列表
        List<ApiRequest> collectionApis = apiMap.get(collection.getModuleId());
        if (collectionApis != null && !collectionApis.isEmpty()) {
            List<Map<String, Object>> apiList = collectionApis.stream()
                    .map(api -> {
                        Map<String, Object> apiNode = new HashMap<>();
                        apiNode.put("id", api.getId());
                        apiNode.put("name", api.getName());
                        apiNode.put("type", "api");
                        apiNode.put("httpMethod", api.getHttpMethod());
                        apiNode.put("url", api.getUrl());
                        apiNode.put("status", api.getStatus());
                        return apiNode;
                    })
                    .collect(Collectors.toList());
            node.put("apis", apiList);
        }

        return node;
    }
}

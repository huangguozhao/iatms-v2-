package com.iatms.application.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.user.UserQueryService;
import com.iatms.domain.model.dto.query.UserQuery;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.UserSummaryVO;
import com.iatms.api.common.ApiResponse;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户查询服务实现
 */
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserMapper userMapper;

    @Override
    public ApiResponse.PageResult<UserSummaryVO> queryUsers(UserQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(User::getName, query.getKeyword())
                    .or().like(User::getEmail, query.getKeyword()));
        }

        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(User::getStatus, query.getStatus());
        }

        if (query.getPosition() != null && !query.getPosition().isEmpty()) {
            wrapper.like(User::getPosition, query.getPosition());
        }

        if (query.getDepartmentId() != null) {
            wrapper.eq(User::getDepartmentId, query.getDepartmentId());
        }

        wrapper.eq(User::getIsDeleted, false);
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserSummaryVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ApiResponse.PageResult.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public UserSummaryVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public List<UserSummaryVO> searchUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(User::getName, keyword)
                .or().like(User::getEmail, keyword));
        wrapper.eq(User::getIsDeleted, false);
        wrapper.eq(User::getStatus, "active");
        wrapper.orderByDesc(User::getCreatedAt);
        wrapper.last("LIMIT 20");

        return userMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<?> listDepartments() {
        return List.of();
    }

    @Override
    public List<?> listDepartmentsTree() {
        return List.of();
    }

    private UserSummaryVO convertToVO(User user) {
        return UserSummaryVO.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .position(user.getPosition())
                .departmentId(user.getDepartmentId())
                .employeeId(user.getEmployeeId())
                .status(user.getStatus())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .lastLoginTime(user.getLastLoginTime() != null ? user.getLastLoginTime().toString() : null)
                .build();
    }
}

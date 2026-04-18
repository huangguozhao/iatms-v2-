package com.iatms.application.user.impl;

import com.iatms.application.user.UserCommandService;
import com.iatms.application.user.UserQueryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.domain.model.dto.command.CreateUserCmd;
import com.iatms.domain.model.dto.command.UpdateUserCmd;
import com.iatms.domain.model.entity.User;
import com.iatms.domain.model.vo.UserSummaryVO;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户命令服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserMapper userMapper;
    private final UserQueryService userQueryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserSummaryVO createUser(CreateUserCmd cmd, Long operatorId) {
        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailCheck = new LambdaQueryWrapper<>();
        emailCheck.eq(User::getEmail, cmd.getEmail()).eq(User::getIsDeleted, false);
        if (userMapper.selectCount(emailCheck) > 0) {
            throw new BusinessException("EMAIL_EXISTS", "该邮箱已被注册");
        }

        User user = new User();
        user.setName(cmd.getName());
        user.setEmail(cmd.getEmail());
        if (cmd.getPassword() != null && !cmd.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(cmd.getPassword()));
        }
        user.setPhone(cmd.getPhone());
        user.setPosition(cmd.getPosition());
        user.setDepartmentId(cmd.getDepartmentId());
        user.setEmployeeId(cmd.getEmployeeId());
        user.setDescription(cmd.getDescription());
        user.setStatus(cmd.getStatus() != null ? cmd.getStatus() : "pending");
        user.setRole(cmd.getRole() != null ? cmd.getRole() : "user");
        user.setCreatorId(operatorId != null ? operatorId.intValue() : null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsDeleted(false);

        userMapper.insert(user);
        log.info("创建用户: id={}, name={}, operatorId={}", user.getId(), user.getName(), operatorId);
        return userQueryService.getUserDetail(user.getId());
    }

    @Override
    @Transactional
    public UserSummaryVO updateUser(Long userId, UpdateUserCmd cmd, Long operatorId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        if (cmd.getName() != null) user.setName(cmd.getName());
        if (cmd.getEmail() != null) {
            // 检查邮箱唯一性
            LambdaQueryWrapper<User> emailCheck = new LambdaQueryWrapper<>();
            emailCheck.eq(User::getEmail, cmd.getEmail()).eq(User::getIsDeleted, false).ne(User::getId, userId);
            if (userMapper.selectCount(emailCheck) > 0) {
                throw new BusinessException("EMAIL_EXISTS", "该邮箱已被其他用户使用");
            }
            user.setEmail(cmd.getEmail());
        }
        if (cmd.getPhone() != null) user.setPhone(cmd.getPhone());
        if (cmd.getPosition() != null) user.setPosition(cmd.getPosition());
        if (cmd.getDepartmentId() != null) user.setDepartmentId(cmd.getDepartmentId());
        if (cmd.getEmployeeId() != null) user.setEmployeeId(cmd.getEmployeeId());
        if (cmd.getDescription() != null) user.setDescription(cmd.getDescription());
        if (cmd.getStatus() != null) user.setStatus(cmd.getStatus());
        if (cmd.getRole() != null) user.setRole(cmd.getRole());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("更新用户: id={}, operatorId={}", userId, operatorId);
        return userQueryService.getUserDetail(userId);
    }

    @Override
    public void updateUserStatus(Long userId, String status, Long operatorId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("更新用户状态: id={}, status={}, operatorId={}", userId, status, operatorId);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long operatorId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(operatorId != null ? operatorId.intValue() : null);
        userMapper.updateById(user);
        log.info("删除用户: id={}, operatorId={}", userId, operatorId);
    }
}

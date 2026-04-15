package com.iatms.application.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iatms.application.auth.AuthService;
import com.iatms.application.auth.dto.LoginRequest;
import com.iatms.application.auth.dto.LoginVO;
import com.iatms.application.auth.dto.RegisterRequest;
import com.iatms.domain.model.entity.User;
import com.iatms.infrastructure.config.JwtConfig;
import com.iatms.infrastructure.persistence.mapper.UserMapper;
import com.iatms.common.exception.BusinessException;
import com.iatms.domain.model.enums.ErrorCode;
import com.iatms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS.getCode(),
                    ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS.getCode(),
                    ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if ("inactive".equals(user.getStatus())) {
            throw new BusinessException("账户已被禁用");
        }

        // 生成 Token
        String token = jwtConfig.generateToken(user.getId(), user.getName());

        log.info("用户登录成功: userId={}, name={}", user.getId(), user.getName());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getName())
                .displayName(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        // 检查邮箱唯一性
        LambdaQueryWrapper<User> emailCheck = new LambdaQueryWrapper<>();
        emailCheck.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailCheck) > 0) {
            throw new BusinessException(ErrorCode.USER_EMAIL_EXISTS.getCode(),
                    ErrorCode.USER_EMAIL_EXISTS.getMessage());
        }

        // 创建用户
        User user = new User();
        user.setName(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus("active");
        user.setRole("user");

        userMapper.insert(user);

        // 生成 Token
        String token = jwtConfig.generateToken(user.getId(), user.getName());

        log.info("用户注册成功: userId={}, name={}", user.getId(), user.getName());

        return LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getName())
                .displayName(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public LoginVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND.getCode(),
                    ErrorCode.USER_NOT_FOUND.getMessage());
        }

        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getName())
                .displayName(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}

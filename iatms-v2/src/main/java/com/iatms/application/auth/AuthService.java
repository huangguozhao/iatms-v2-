package com.iatms.application.auth;

import com.iatms.application.auth.dto.LoginRequest;
import com.iatms.application.auth.dto.LoginVO;
import com.iatms.application.auth.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginVO register(RegisterRequest request);

    /**
     * 获取当前用户信息
     */
    LoginVO getCurrentUser(Long userId);
}

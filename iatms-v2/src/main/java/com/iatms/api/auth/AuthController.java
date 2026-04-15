package com.iatms.api.auth;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.auth.AuthService;
import com.iatms.application.auth.dto.LoginRequest;
import com.iatms.application.auth.dto.LoginVO;
import com.iatms.application.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody @Valid LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());
        LoginVO result = authService.login(request);
        return ApiResponse.success(result);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@RequestBody @Valid RegisterRequest request) {
        log.info("用户注册: username={}", request.getUsername());
        LoginVO result = authService.register(request);
        return ApiResponse.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ApiResponse<LoginVO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        LoginVO result = authService.getCurrentUser(userId);
        return ApiResponse.success(result);
    }
}

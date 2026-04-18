package com.iatms.api.user;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.user.UserCommandService;
import com.iatms.application.user.UserQueryService;
import com.iatms.domain.model.dto.command.CreateUserCmd;
import com.iatms.domain.model.dto.command.UpdateUserCmd;
import com.iatms.domain.model.dto.query.UserQuery;
import com.iatms.domain.model.vo.UserSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public ApiResponse<ApiResponse.PageResult<UserSummaryVO>> queryUsers(UserQuery query) {
        ApiResponse.PageResult<UserSummaryVO> result = userQueryService.queryUsers(query);
        return ApiResponse.pageSuccess(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserSummaryVO> getUserDetail(@PathVariable Long userId) {
        UserSummaryVO user = userQueryService.getUserDetail(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }

    /**
     * 搜索用户（按姓名或邮箱模糊匹配）
     */
    @GetMapping("/search")
    public ApiResponse<List<UserSummaryVO>> searchUsers(@RequestParam String keyword) {
        List<UserSummaryVO> users = userQueryService.searchUsers(keyword);
        return ApiResponse.success(users);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<UserSummaryVO> createUser(
            @RequestBody @Valid CreateUserCmd cmd,
            @RequestAttribute("userId") Long operatorId) {
        log.info("创建用户: cmd={}, operatorId={}", cmd, operatorId);
        UserSummaryVO user = userCommandService.createUser(cmd, operatorId);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    public ApiResponse<UserSummaryVO> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserCmd cmd,
            @RequestAttribute("userId") Long operatorId) {
        log.info("更新用户: userId={}, cmd={}, operatorId={}", userId, cmd, operatorId);
        UserSummaryVO user = userCommandService.updateUser(userId, cmd, operatorId);
        return ApiResponse.success(user);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body,
            @RequestAttribute("userId") Long operatorId) {
        String status = body.get("status");
        log.info("更新用户状态: userId={}, status={}, operatorId={}", userId, status, operatorId);
        userCommandService.updateUserStatus(userId, status, operatorId);
        return ApiResponse.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long operatorId) {
        log.info("删除用户: userId={}, operatorId={}", userId, operatorId);
        userCommandService.deleteUser(userId, operatorId);
        return ApiResponse.success();
    }

    /**
     * 获取部门列表
     */
    @GetMapping("/departments")
    public ApiResponse<List<?>> listDepartments() {
        return ApiResponse.success(userQueryService.listDepartments());
    }

    /**
     * 获取部门树
     */
    @GetMapping("/departments/tree")
    public ApiResponse<List<?>> listDepartmentsTree() {
        return ApiResponse.success(userQueryService.listDepartmentsTree());
    }
}

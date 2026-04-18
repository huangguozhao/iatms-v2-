package com.iatms.application.user;

import com.iatms.domain.model.dto.command.CreateUserCmd;
import com.iatms.domain.model.dto.command.UpdateUserCmd;
import com.iatms.domain.model.vo.UserSummaryVO;

/**
 * 用户命令服务接口
 */
public interface UserCommandService {

    /**
     * 创建用户
     */
    UserSummaryVO createUser(CreateUserCmd cmd, Long operatorId);

    /**
     * 更新用户
     */
    UserSummaryVO updateUser(Long userId, UpdateUserCmd cmd, Long operatorId);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, String status, Long operatorId);

    /**
     * 删除用户
     */
    void deleteUser(Long userId, Long operatorId);
}

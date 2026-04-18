package com.iatms.application.user;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.dto.command.CreateUserCmd;
import com.iatms.domain.model.dto.command.UpdateUserCmd;
import com.iatms.domain.model.dto.query.UserQuery;
import com.iatms.domain.model.vo.UserSummaryVO;

import java.util.List;

/**
 * 用户查询服务接口
 */
public interface UserQueryService {

    /**
     * 分页查询用户列表
     */
    ApiResponse.PageResult<UserSummaryVO> queryUsers(UserQuery query);

    /**
     * 获取用户详情
     */
    UserSummaryVO getUserDetail(Long userId);

    /**
     * 搜索用户（按姓名或邮箱模糊匹配）
     */
    List<UserSummaryVO> searchUsers(String keyword);

    /**
     * 获取所有部门列表
     */
    List<?> listDepartments();

    /**
     * 获取部门树
     */
    List<?> listDepartmentsTree();
}

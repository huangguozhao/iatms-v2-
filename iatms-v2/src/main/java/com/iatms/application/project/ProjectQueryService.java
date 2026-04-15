package com.iatms.application.project;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.project.dto.query.ProjectQuery;
import com.iatms.domain.model.vo.ProjectDetailVO;
import com.iatms.domain.model.vo.ProjectSummaryVO;

import java.util.List;

/**
 * 项目查询服务接口
 */
public interface ProjectQueryService {

    /**
     * 分页查询项目列表
     */
    ApiResponse.PageResult<ProjectSummaryVO> queryProjects(ProjectQuery query, Long userId);

    /**
     * 获取项目详情
     */
    ProjectDetailVO getProjectDetail(Long projectId, Long userId);

    /**
     * 获取用户最近访问的项目
     */
    List<ProjectSummaryVO> getRecentProjects(Long userId, int limit);

    /**
     * 获取用户参与的项目列表
     */
    List<ProjectSummaryVO> getUserProjects(Long userId);
}

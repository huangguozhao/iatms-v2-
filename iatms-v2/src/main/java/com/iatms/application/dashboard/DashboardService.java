package com.iatms.application.dashboard;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.vo.DashboardStatisticsVO;
import com.iatms.domain.model.vo.RecentActivityVO;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     *
     * @param userId 用户ID，管理员返回所有数据，普通用户返回有权限访问的数据
     * @return 统计数据
     */
    DashboardStatisticsVO getStatistics(Long userId);

    /**
     * 获取最近活动列表
     *
     * @param limit 返回数量限制
     * @param userId 用户ID，管理员返回所有活动，普通用户返回有权限访问的活动
     * @return 最近活动列表
     */
    List<RecentActivityVO> getRecentActivities(int limit, Long userId);
}

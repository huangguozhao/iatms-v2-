package com.iatms.api.dashboard;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.dashboard.DashboardService;
import com.iatms.domain.model.vo.DashboardStatisticsVO;
import com.iatms.domain.model.vo.RecentActivityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<DashboardStatisticsVO> getStatistics() {
        DashboardStatisticsVO result = dashboardService.getStatistics();
        return ApiResponse.success(result);
    }

    /**
     * 获取最近活动列表
     */
    @GetMapping("/activities")
    public ApiResponse<List<RecentActivityVO>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentActivityVO> result = dashboardService.getRecentActivities(limit);
        return ApiResponse.success(result);
    }
}

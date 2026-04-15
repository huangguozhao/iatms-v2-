package com.iatms.application.scheduling;

import com.iatms.api.common.ApiResponse;
import com.iatms.domain.model.vo.ScheduledTaskDetailVO;
import com.iatms.domain.model.vo.ScheduledTaskSummaryVO;

/**
 * 定时任务查询服务接口
 */
public interface ScheduledTaskQueryService {

    /**
     * 分页查询任务
     */
    ApiResponse.PageResult<ScheduledTaskSummaryVO> queryTasks(
            Long projectId, String status, Integer pageNum, Integer pageSize);

    /**
     * 获取任务详情
     */
    ScheduledTaskDetailVO getTaskDetail(Long taskId);
}

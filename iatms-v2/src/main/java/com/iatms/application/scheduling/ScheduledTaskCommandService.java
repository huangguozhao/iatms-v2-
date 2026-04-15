package com.iatms.application.scheduling;

import com.iatms.application.scheduling.dto.command.CreateScheduledTaskCmd;
import com.iatms.domain.model.vo.ScheduledTaskDetailVO;

/**
 * 定时任务命令服务接口
 */
public interface ScheduledTaskCommandService {

    /**
     * 创建定时任务
     */
    ScheduledTaskDetailVO createTask(CreateScheduledTaskCmd cmd, Long userId);

    /**
     * 更新定时任务
     */
    ScheduledTaskDetailVO updateTask(Long taskId, CreateScheduledTaskCmd cmd, Long userId);

    /**
     * 删除定时任务
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 立即执行
     */
    void runTaskNow(Long taskId, Long userId);

    /**
     * 暂停任务
     */
    void pauseTask(Long taskId, Long userId);

    /**
     * 恢复任务
     */
    void resumeTask(Long taskId, Long userId);
}

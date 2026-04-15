package com.iatms.application.notification;

import com.iatms.domain.model.entity.Notification;
import com.iatms.domain.model.vo.NotificationVO;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 分页查询通知列表
     */
    NotificationVO.PageResult<NotificationVO> queryNotifications(Long userId, String keyword, String type, Boolean read, int pageNum, int pageSize);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * 发送通知
     */
    void sendNotification(Long userId, String type, String title, String content, Long relatedId, String relatedType);
}

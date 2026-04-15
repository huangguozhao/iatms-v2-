package com.iatms.api.notification;

import com.iatms.api.common.ApiResponse;
import com.iatms.application.notification.NotificationService;
import com.iatms.domain.model.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询通知列表
     */
    @GetMapping
    public ApiResponse<NotificationVO.PageResult<NotificationVO>> queryNotifications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        NotificationVO.PageResult<NotificationVO> result =
                notificationService.queryNotifications(userId, keyword, type, read, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(@RequestAttribute("userId") Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestAttribute("userId") Long userId) {

        notificationService.markAsRead(notificationId, userId);
        return ApiResponse.success();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(
            @PathVariable Long notificationId,
            @RequestAttribute("userId") Long userId) {

        notificationService.deleteNotification(notificationId, userId);
        return ApiResponse.success();
    }
}

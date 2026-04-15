package com.iatms.application.notification.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iatms.application.notification.NotificationService;
import com.iatms.domain.model.entity.Notification;
import com.iatms.domain.model.vo.NotificationVO;
import com.iatms.infrastructure.persistence.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public NotificationVO.PageResult<NotificationVO> queryNotifications(
            Long userId, String keyword, String type, Boolean read, int pageNum, int pageSize) {

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Notification::getTitle, keyword)
                    .or()
                    .like(Notification::getContent, keyword));
        }

        if (StringUtils.hasText(type)) {
            wrapper.eq(Notification::getType, type);
        }

        if (read != null) {
            wrapper.eq(Notification::getRead, read);
        }

        IPage<Notification> page = new Page<>(pageNum, pageSize);
        IPage<Notification> resultPage = notificationMapper.selectPage(page, wrapper);

        List<NotificationVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return NotificationVO.PageResult.<NotificationVO>builder()
                .records(records)
                .total(resultPage.getTotal())
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getRead, false);
        return notificationMapper.selectCount(wrapper).intValue();
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectOne(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId)
        );

        if (notification != null && !Boolean.TRUE.equals(notification.getRead())) {
            notification.setRead(true);
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        Notification update = new Notification();
        update.setRead(true);
        notificationMapper.update(update,
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getRead, false));
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        notificationMapper.delete(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId));
    }

    @Override
    public void sendNotification(Long userId, String type, String title, String content,
                                  Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private NotificationVO convertToVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

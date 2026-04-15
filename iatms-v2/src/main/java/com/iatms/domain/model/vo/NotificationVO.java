package com.iatms.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知 VO
 */
@Data
@Builder
public class NotificationVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private String relatedType;
    private Boolean read;
    private LocalDateTime createdAt;

    /**
     * 分页结果
     */
    @Data
    @Builder
    public static class PageResult {
        private List<NotificationVO> records;
        private long total;
        private int pageNum;
        private int pageSize;
    }
}

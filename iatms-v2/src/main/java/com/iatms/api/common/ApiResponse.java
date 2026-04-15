package com.iatms.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一 API 响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 分页响应
     */
    public static <T> ApiResponse<PageResult<T>> pageSuccess(PageResult<T> pageData) {
        return ApiResponse.<PageResult<T>>builder()
                .code(200)
                .message("Success")
                .data(pageData)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 分页结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageResult<T> {
        private java.util.List<T> records;
        private long total;
        private int pageNum;
        private int pageSize;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public static <T> PageResult<T> of(java.util.List<T> records, long total, int pageNum, int pageSize) {
            int totalPages = (int) Math.ceil((double) total / pageSize);
            return PageResult.<T>builder()
                    .records(records)
                    .total(total)
                    .pageNum(pageNum)
                    .pageSize(pageSize)
                    .totalPages(totalPages)
                    .hasNext(pageNum < totalPages)
                    .hasPrevious(pageNum > 1)
                    .build();
        }
    }
}

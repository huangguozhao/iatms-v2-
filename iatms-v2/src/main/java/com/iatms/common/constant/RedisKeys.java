package com.iatms.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeys {

    private static final String PREFIX = "iatms:";

    // ========== 执行任务相关 ==========
    private static final String EXECUTION_PREFIX = PREFIX + "execution:";

    /**
     * 执行状态
     */
    public static String executionStatus(Long executionId) {
        return EXECUTION_PREFIX + executionId + ":status";
    }

    /**
     * 执行状态 (String版本)
     */
    public static String executionStatus(String executionId) {
        return EXECUTION_PREFIX + executionId + ":status";
    }

    /**
     * 执行进度
     */
    public static String executionProgress(Long executionId) {
        return EXECUTION_PREFIX + executionId + ":progress";
    }

    /**
     * 执行结果
     */
    public static String executionResult(Long executionId) {
        return EXECUTION_PREFIX + executionId + ":result";
    }

    /**
     * 待执行队列
     */
    public static String executionQueue() {
        return PREFIX + "queue:execution";
    }

    // ========== 定时任务相关 ==========
    private static final String TASK_PREFIX = PREFIX + "task:";

    /**
     * 任务分布式锁
     */
    public static String taskLock(Long taskId) {
        return TASK_PREFIX + taskId + ":lock";
    }

    /**
     * 任务进度
     */
    public static String taskProgress(Long taskId) {
        return TASK_PREFIX + taskId + ":progress";
    }

    // ========== 用户会话相关 ==========
    private static final String USER_PREFIX = PREFIX + "user:";

    /**
     * 用户 Token
     */
    public static String userToken(Long userId) {
        return USER_PREFIX + userId + ":token";
    }

    /**
     * 用户权限缓存
     */
    public static String userPermissions(Long userId) {
        return USER_PREFIX + userId + ":permissions";
    }

    // ========== 变量池相关 ==========
    private static final String POOL_PREFIX = PREFIX + "pool:";

    /**
     * 变量池
     */
    public static String variablePool(Long poolId) {
        return POOL_PREFIX + poolId;
    }
}

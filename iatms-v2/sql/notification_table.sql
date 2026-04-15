-- 通知表
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '通知类型: SYSTEM-系统通知, TASK-任务通知, REPORT-报告通知, TEAM-团队通知',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '通知内容',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID（如任务ID、报告ID等）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关联类型（如TASK, REPORT, PROJECT等）',
  `read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表' ROW_FORMAT=Dynamic;

/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : iatmsdb

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 18/04/2026 10:02:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for aitestcasegenerations
-- ----------------------------
DROP TABLE IF EXISTS `aitestcasegenerations`;
CREATE TABLE `aitestcasegenerations`  (
  `generation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '生成记录ID',
  `project_id` int NULL DEFAULT NULL COMMENT '项目ID',
  `api_id` int NULL DEFAULT NULL COMMENT '接口ID（单个生成时）',
  `user_id` int NOT NULL COMMENT '请求用户ID',
  `generation_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '生成类型：single/batch/requirement',
  `input_context` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '输入上下文（需求描述/接口信息JSON）',
  `generation_config` json NULL COMMENT '生成配置（用例数量、覆盖场景等）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending/processing/completed/failed',
  `generated_cases` json NULL COMMENT '生成的用例数据（JSON数组）',
  `case_count` int NULL DEFAULT 0 COMMENT '生成用例数量',
  `prompt_tokens` int NULL DEFAULT NULL COMMENT '提示词token数',
  `completion_tokens` int NULL DEFAULT NULL COMMENT '完成token数',
  `total_cost` decimal(10, 6) NULL DEFAULT NULL COMMENT '调用成本',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  PRIMARY KEY (`generation_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI测试用例生成记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for apipreconditions
-- ----------------------------
DROP TABLE IF EXISTS `apipreconditions`;
CREATE TABLE `apipreconditions`  (
  `precondition_id` int NOT NULL AUTO_INCREMENT COMMENT '前置条件ID，自增主键',
  `api_id` int NOT NULL COMMENT '接口ID，关联Apis表',
  `precondition_api_id` int NULL DEFAULT NULL COMMENT '前置接口id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前置条件名称',
  `type` enum('auth','setup','data','environment') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前置条件类型',
  `config` json NOT NULL COMMENT '配置信息，JSON格式',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '前置条件描述',
  `is_required` tinyint(1) NULL DEFAULT 1 COMMENT '是否必需',
  `created_by` int NOT NULL COMMENT '创建人ID，关联用户表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`precondition_id`) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1029 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口前置条件信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for apis
-- ----------------------------
DROP TABLE IF EXISTS `apis`;
CREATE TABLE `apis`  (
  `api_id` int NOT NULL AUTO_INCREMENT COMMENT '接口ID，自增主键',
  `api_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口编码，模块内唯一',
  `module_id` int NOT NULL COMMENT '模块ID，关联Modules表的主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口名称',
  `method` enum('GET','POST','PUT','DELETE','PATCH','HEAD','OPTIONS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求方法',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口路径',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '基础URL',
  `full_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS (concat(coalesce(`base_url`,_utf8mb4''),`path`)) STORED COMMENT '完整URL' NULL,
  `request_parameters` json NULL COMMENT '查询参数，JSON格式',
  `path_parameters` json NULL COMMENT '路径参数，JSON格式',
  `request_headers` json NULL COMMENT '请求头信息，JSON格式',
  `request_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求体内容',
  `request_body_type` enum('json','form-data','x-www-form-urlencoded','xml','raw') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求体类型',
  `response_body_type` enum('json','xml','html','text') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '响应体类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '接口描述',
  `status` enum('active','inactive','deprecated') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '接口状态',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `timeout_seconds` int NULL DEFAULT 30 COMMENT '超时时间(秒)',
  `auth_type` enum('none','basic','bearer','api_key','oauth2') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'none' COMMENT '认证类型',
  `auth_config` json NULL COMMENT '认证配置',
  `tags` json NULL COMMENT '标签，JSON数组格式',
  `examples` json NULL COMMENT '请求示例',
  `created_by` int NOT NULL COMMENT '创建人ID，关联用户表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`api_id`) USING BTREE,
  UNIQUE INDEX `uk_module_api_code`(`module_id` ASC, `api_code` ASC) USING BTREE,
  INDEX `idx_module_id`(`module_id` ASC) USING BTREE,
  INDEX `idx_method_path`(`method` ASC, `path` ASC) USING BTREE,
  INDEX `idx_api_code`(`api_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2032 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `department_id` int NOT NULL AUTO_INCREMENT COMMENT '部门ID，自增主键',
  `department_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门编码，唯一标识',
  `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` int NULL DEFAULT NULL COMMENT '父部门ID，关联本表主键',
  `manager_id` int NULL DEFAULT NULL COMMENT '部门负责人ID，关联Users表',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '部门描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `level` int NULL DEFAULT 1 COMMENT '部门层级（1为最高级）',
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门路径（存储所有上级部门ID）',
  `is_leaf` tinyint(1) NULL DEFAULT 1 COMMENT '是否为叶子部门（无子部门）',
  `status` enum('active','inactive','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '部门状态',
  `established_date` date NULL DEFAULT NULL COMMENT '成立日期',
  `created_by` int NOT NULL COMMENT '创建人ID，关联Users表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`department_id`) USING BTREE,
  UNIQUE INDEX `uk_department_code`(`department_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_department_code`(`department_code` ASC) USING BTREE,
  INDEX `idx_manager_id`(`manager_id` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE,
  INDEX `idx_path`(`path`(255) ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for environmentconfigs
-- ----------------------------
DROP TABLE IF EXISTS `environmentconfigs`;
CREATE TABLE `environmentconfigs`  (
  `env_id` int NOT NULL AUTO_INCREMENT COMMENT '环境ID，自增主键',
  `env_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '环境编码，唯一标识',
  `env_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '环境名称',
  `env_type` enum('development','testing','staging','production','performance','disaster_recovery') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'testing' COMMENT '环境类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '环境描述',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '基础URL',
  `domain` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '域名',
  `protocol` enum('http','https') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'https' COMMENT '协议',
  `port` int NULL DEFAULT NULL COMMENT '端口号',
  `database_config` json NULL COMMENT '数据库配置',
  `external_services` json NULL COMMENT '外部服务配置',
  `variables` json NULL COMMENT '环境变量',
  `auth_config` json NULL COMMENT '认证配置',
  `feature_flags` json NULL COMMENT '功能开关',
  `performance_config` json NULL COMMENT '性能配置',
  `monitoring_config` json NULL COMMENT '监控配置',
  `status` enum('active','inactive','maintenance') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '环境状态',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否为默认环境',
  `maintenance_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '维护信息',
  `deployment_info` json NULL COMMENT '部署信息',
  `last_deployed_at` timestamp NULL DEFAULT NULL COMMENT '最后部署时间',
  `deployed_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部署版本',
  `created_by` int NOT NULL COMMENT '创建人ID，关联Users表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`env_id`) USING BTREE,
  UNIQUE INDEX `uk_env_code`(`env_code` ASC) USING BTREE,
  INDEX `idx_env_code`(`env_code` ASC) USING BTREE,
  INDEX `idx_env_type`(`env_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '环境配置表，管理不同测试环境的配置信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for integration_settings
-- ----------------------------
DROP TABLE IF EXISTS `integration_settings`;
CREATE TABLE `integration_settings`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `integration_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '集成类型: api-接口集成, webhook-Webhook, jenkins-Jenkins, git-Git',
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '集成服务名称',
  `enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用',
  `api_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'v2.0' COMMENT 'API版本',
  `request_timeout` int NULL DEFAULT 30 COMMENT '请求超时时间(秒)',
  `max_retries` int NULL DEFAULT 3 COMMENT '最大重试次数',
  `auth_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'none' COMMENT '认证类型: none-无, basic-Basic, bearer-Bearer Token, oauth2-OAuth2',
  `auth_token` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '认证令牌',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户名(Basic认证用)',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '密码(Basic认证用)',
  `response_format` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'json' COMMENT '响应格式: json, xml, text',
  `webhook_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Webhook URL',
  `webhook_secret` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Webhook密钥',
  `jenkins_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Jenkins地址',
  `jenkins_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Jenkins用户名',
  `jenkins_token` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Jenkins API Token',
  `git_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Git仓库地址',
  `git_branch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'main' COMMENT 'Git分支',
  `git_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Git用户名',
  `git_token` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT 'Git Token',
  `extra_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '其他配置(JSON格式)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_integration_type`(`integration_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '集成设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logs
-- ----------------------------
DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs`  (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID，自增主键',
  `user_id` int NOT NULL COMMENT '用户ID',
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作动作',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：login, create, update, delete, export, import, execute等',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作对象类型：user, project, module, api, testcase, report, task等',
  `target_id` int NULL DEFAULT NULL COMMENT '操作对象ID',
  `target_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作对象名称',
  `project_id` int NULL DEFAULT NULL COMMENT '关联项目ID',
  `module_id` int NULL DEFAULT NULL COMMENT '关联模块ID',
  `status` enum('success','failure','warning') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'success' COMMENT '操作状态',
  `error_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误代码',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误详细信息',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP请求方法：GET, POST, PUT, DELETE等',
  `request_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求路径',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `request_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求体内容',
  `response_status` int NULL DEFAULT NULL COMMENT '响应状态码',
  `response_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应体内容（摘要）',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户代理信息',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型：web, mobile, tablet等',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `log_level` enum('DEBUG','INFO','WARN','ERROR','FATAL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'INFO' COMMENT '日志级别',
  `timestamp` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_module_id`(`module_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_timestamp`(`timestamp` ASC) USING BTREE,
  INDEX `idx_ip_address`(`ip_address` ASC) USING BTREE,
  INDEX `idx_log_level`(`log_level` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for modules
-- ----------------------------
DROP TABLE IF EXISTS `modules`;
CREATE TABLE `modules`  (
  `module_id` int NOT NULL AUTO_INCREMENT COMMENT '模块ID，自增主键',
  `module_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码，项目内唯一',
  `project_id` int NOT NULL COMMENT '项目ID，关联Projects表的主键',
  `parent_module_id` int NULL DEFAULT NULL COMMENT '父模块ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模块描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` enum('active','inactive','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '模块状态',
  `owner_id` int NULL DEFAULT NULL COMMENT '模块负责人',
  `tags` json NULL COMMENT '标签信息',
  `created_by` int NOT NULL COMMENT '创建人ID，关联用户表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`module_id`) USING BTREE,
  UNIQUE INDEX `uk_project_module_code`(`project_id` ASC, `module_code` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_parent_module_id`(`parent_module_id` ASC) USING BTREE,
  INDEX `idx_module_code`(`module_code` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模块信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification_settings
-- ----------------------------
DROP TABLE IF EXISTS `notification_settings`;
CREATE TABLE `notification_settings`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用邮件通知',
  `sms_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用短信通知',
  `system_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用系统通知',
  `email_host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '邮件服务器主机',
  `email_port` int NULL DEFAULT 587 COMMENT '邮件服务器端口',
  `email_username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '邮件用户名',
  `email_password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '邮件密码',
  `email_ssl` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用SSL',
  `email_from` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '邮件发件人',
  `sms_provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'aliyun' COMMENT '短信服务商: aliyun-阿里云, tencent-腾讯云, huawei-华为云',
  `sms_access_key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '短信AccessKey',
  `sms_secret_key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '短信SecretKey',
  `sms_sign_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '短信签名',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '通知类型: SYSTEM-系统通知, TASK-任务通知, REPORT-报告通知, TEAM-团队通知',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（如任务ID、报告ID等）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型（如TASK, REPORT, PROJECT等）',
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for projectmembers
-- ----------------------------
DROP TABLE IF EXISTS `projectmembers`;
CREATE TABLE `projectmembers`  (
  `member_id` int NOT NULL AUTO_INCREMENT COMMENT '成员关系ID，自增主键',
  `project_id` int NOT NULL COMMENT '项目ID，关联Projects表',
  `user_id` int NOT NULL COMMENT '用户ID，关联Users表',
  `permission_level` enum('read','write','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'read' COMMENT '权限级别：只读、可编辑、管理员',
  `project_role` enum('owner','manager','developer','tester','viewer') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'viewer' COMMENT '项目角色',
  `status` enum('active','inactive','removed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '成员状态',
  `join_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `leave_time` timestamp NULL DEFAULT NULL COMMENT '离开时间',
  `last_active_time` timestamp NULL DEFAULT NULL COMMENT '最后活跃时间',
  `additional_roles` json NULL COMMENT '附加角色信息',
  `custom_permissions` json NULL COMMENT '自定义权限配置',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注信息',
  `created_by` int NOT NULL COMMENT '添加人ID，关联Users表',
  `updated_by` int NULL DEFAULT NULL COMMENT '最后更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`member_id`) USING BTREE,
  UNIQUE INDEX `uk_project_user`(`project_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_permission_level`(`permission_level` ASC) USING BTREE,
  INDEX `idx_project_role`(`project_role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_join_time`(`join_time` ASC) USING BTREE,
  INDEX `idx_leave_time`(`leave_time` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目成员关联表，管理项目成员权限和角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for projects
-- ----------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects`  (
  `project_id` int NOT NULL AUTO_INCREMENT COMMENT '项目ID，自增主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目描述',
  `created_by` int NOT NULL COMMENT '创建人ID，关联用户表',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  `project_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编码，唯一',
  `project_type` enum('WEB','MOBILE','API','DESKTOP','HYBRID') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'API' COMMENT '项目类型',
  `status` enum('ACTIVE','INACTIVE','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE' COMMENT '项目状态',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目头像',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `version` int NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`project_id`) USING BTREE,
  UNIQUE INDEX `project_code`(`project_code` ASC) USING BTREE,
  INDEX `idx_project_code`(`project_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `BLOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  INDEX `SCHED_NAME`(`SCHED_NAME` ASC, `TRIGGER_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `CALENDAR_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `CRON_EXPRESSION` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TIME_ZONE_ID` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ENTRY_ID` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `INSTANCE_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`) USING BTREE,
  INDEX `IDX_QRTZ_FT_TRIG_INST_NAME`(`SCHED_NAME` ASC, `INSTANCE_NAME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY`(`SCHED_NAME` ASC, `INSTANCE_NAME` ASC, `REQUESTS_RECOVERY` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_J_G`(`SCHED_NAME` ASC, `JOB_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_JG`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_T_G`(`SCHED_NAME` ASC, `TRIGGER_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_FT_TG`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `IS_DURABLE` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `IS_NONCONCURRENT` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `IS_UPDATE_DATA` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) USING BTREE,
  INDEX `IDX_QRTZ_J_REQ_RECOVERY`(`SCHED_NAME` ASC, `REQUESTS_RECOVERY` ASC) USING BTREE,
  INDEX `IDX_QRTZ_J_GRP`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `LOCK_NAME` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `INSTANCE_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `STR_PROP_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `STR_PROP_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `STR_PROP_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `INT_PROP_1` int NULL DEFAULT NULL,
  `INT_PROP_2` int NULL DEFAULT NULL,
  `LONG_PROP_1` bigint NULL DEFAULT NULL,
  `LONG_PROP_2` bigint NULL DEFAULT NULL,
  `DEC_PROP_1` decimal(13, 4) NULL DEFAULT NULL,
  `DEC_PROP_2` decimal(13, 4) NULL DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `qrtz_triggers` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `SCHED_NAME` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `JOB_GROUP` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `DESCRIPTION` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint NULL DEFAULT NULL,
  `PREV_FIRE_TIME` bigint NULL DEFAULT NULL,
  `PRIORITY` int NULL DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `TRIGGER_TYPE` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint NULL DEFAULT NULL,
  `CALENDAR_NAME` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `MISFIRE_INSTR` smallint NULL DEFAULT NULL,
  `JOB_DATA` blob NULL,
  PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) USING BTREE,
  INDEX `IDX_QRTZ_T_J`(`SCHED_NAME` ASC, `JOB_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_JG`(`SCHED_NAME` ASC, `JOB_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_C`(`SCHED_NAME` ASC, `CALENDAR_NAME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_G`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_STATE`(`SCHED_NAME` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_N_STATE`(`SCHED_NAME` ASC, `TRIGGER_NAME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_N_G_STATE`(`SCHED_NAME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NEXT_FIRE_TIME`(`SCHED_NAME` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST`(`SCHED_NAME` ASC, `TRIGGER_STATE` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_MISFIRE`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP`(`SCHED_NAME` ASC, `MISFIRE_INSTR` ASC, `NEXT_FIRE_TIME` ASC, `TRIGGER_GROUP` ASC, `TRIGGER_STATE` ASC) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `qrtz_job_details` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scheduledtaskexecutions
-- ----------------------------
DROP TABLE IF EXISTS `scheduledtaskexecutions`;
CREATE TABLE `scheduledtaskexecutions`  (
  `execution_id` bigint NOT NULL AUTO_INCREMENT COMMENT '执行记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID，关联ScheduledTestTasks',
  `test_execution_record_id` bigint NULL DEFAULT NULL COMMENT '测试执行记录ID，关联TestExecutionRecords',
  `quartz_job_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Quartz任务ID',
  `status` enum('pending','running','success','failed','skipped','timeout','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '执行状态',
  `scheduled_time` datetime NOT NULL COMMENT '计划执行时间',
  `actual_start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际结束时间',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '执行耗时（秒）',
  `delay_seconds` int NULL DEFAULT NULL COMMENT '延迟时间（秒，actual_start_time - scheduled_time）',
  `total_cases` int NULL DEFAULT 0 COMMENT '总用例数',
  `passed_cases` int NULL DEFAULT 0 COMMENT '通过数',
  `failed_cases` int NULL DEFAULT 0 COMMENT '失败数',
  `skipped_cases` int NULL DEFAULT 0 COMMENT '跳过数',
  `success_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '成功率',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '堆栈跟踪',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `is_retry` tinyint(1) NULL DEFAULT 0 COMMENT '是否是重试',
  `original_execution_id` bigint NULL DEFAULT NULL COMMENT '原始执行ID（如果是重试）',
  `notification_sent` tinyint(1) NULL DEFAULT 0 COMMENT '是否发送通知',
  `notification_channels` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知渠道（email,webhook等）',
  `notification_timestamp` datetime NULL DEFAULT NULL COMMENT '通知时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`execution_id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_test_execution_record_id`(`test_execution_record_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_scheduled_time`(`scheduled_time` ASC) USING BTREE,
  INDEX `idx_actual_start_time`(`actual_start_time` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `scheduledtaskexecutions_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `scheduledtesttasks` (`task_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务执行记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scheduledtesttasks
-- ----------------------------
DROP TABLE IF EXISTS `scheduledtesttasks`;
CREATE TABLE `scheduledtesttasks`  (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务描述',
  `task_type` enum('single_case','module','project','test_suite','api') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类型',
  `target_id` int NOT NULL COMMENT '目标ID（取决于task_type）',
  `target_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标名称',
  `case_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用例ID列表（JSON格式）',
  `trigger_type` enum('cron','simple','daily','weekly','monthly') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器类型',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Cron表达式（trigger_type=cron时必填）',
  `simple_repeat_interval` bigint NULL DEFAULT NULL COMMENT '重复间隔（毫秒，trigger_type=simple时使用）',
  `simple_repeat_count` int NULL DEFAULT -1 COMMENT '重复次数（-1表示无限）',
  `daily_hour` int NULL DEFAULT NULL COMMENT '每日执行时刻-小时（0-23）',
  `daily_minute` int NULL DEFAULT NULL COMMENT '每日执行时刻-分钟（0-59）',
  `weekly_days` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '每周执行的天数（1-7，逗号分隔，1=周一）',
  `monthly_day` int NULL DEFAULT NULL COMMENT '每月执行的日期（1-31）',
  `execution_environment` enum('dev','test','prod','staging') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'test' COMMENT '执行环境',
  `base_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '基础URL覆盖',
  `timeout_seconds` int NULL DEFAULT 30 COMMENT '超时时间（秒）',
  `concurrency` int NULL DEFAULT 1 COMMENT '并发数',
  `execution_strategy` enum('sequential','parallel','by_module','smart') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'sequential' COMMENT '执行策略',
  `retry_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否启用重试',
  `max_retry_attempts` int NULL DEFAULT 0 COMMENT '最大重试次数',
  `retry_delay_ms` int NULL DEFAULT 1000 COMMENT '重试延迟（毫秒）',
  `notify_on_success` tinyint(1) NULL DEFAULT 0 COMMENT '成功时通知',
  `notify_on_failure` tinyint(1) NULL DEFAULT 1 COMMENT '失败时通知',
  `notification_recipients` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知接收者（邮箱，逗号分隔）',
  `skip_if_previous_failed` tinyint(1) NULL DEFAULT 0 COMMENT '前次失败时跳过',
  `max_duration_seconds` int NULL DEFAULT NULL COMMENT '最大执行时长（秒）',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `next_trigger_time` datetime NULL DEFAULT NULL COMMENT '下次触发时间',
  `last_execution_time` datetime NULL DEFAULT NULL COMMENT '最后一次执行时间',
  `created_by` int NOT NULL COMMENT '创建人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` int NULL DEFAULT NULL COMMENT '最后修改人ID',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `total_executions` int NULL DEFAULT 0 COMMENT '总执行次数',
  `successful_executions` int NULL DEFAULT 0 COMMENT '成功次数',
  `failed_executions` int NULL DEFAULT 0 COMMENT '失败次数',
  `skipped_executions` int NULL DEFAULT 0 COMMENT '跳过次数',
  `last_execution_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后执行状态',
  `version` int NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE INDEX `uk_task_name`(`task_name` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_is_enabled`(`is_enabled` ASC) USING BTREE,
  INDEX `idx_next_trigger_time`(`next_trigger_time` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时测试任务定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for suitecasemappings
-- ----------------------------
DROP TABLE IF EXISTS `suitecasemappings`;
CREATE TABLE `suitecasemappings`  (
  `mapping_id` int NOT NULL AUTO_INCREMENT COMMENT '映射ID，自增主键',
  `suite_id` int NOT NULL COMMENT '测试集合ID，关联TestSuites表',
  `case_id` int NOT NULL COMMENT '用例ID，关联TestCases表',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `relation_type` enum('PRIMARY','SECONDARY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PRIMARY' COMMENT '关联类型',
  PRIMARY KEY (`mapping_id`) USING BTREE,
  UNIQUE INDEX `uk_suite_case`(`suite_id` ASC, `case_id` ASC) USING BTREE,
  INDEX `idx_mapping_id`(`mapping_id` ASC) USING BTREE,
  INDEX `idx_suite_id`(`suite_id` ASC) USING BTREE,
  INDEX `idx_case_id`(`case_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试用例与套件关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_basic_settings
-- ----------------------------
DROP TABLE IF EXISTS `system_basic_settings`;
CREATE TABLE `system_basic_settings`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '接口自动化管理系统' COMMENT '系统名称',
  `system_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'V2.5.3' COMMENT '系统版本',
  `system_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '系统ID',
  `system_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '系统描述',
  `deployment_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'private' COMMENT '部署模式: private-私有化部署, cloud-云端部署, hybrid-混合部署',
  `theme_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'blue' COMMENT '主题色: blue-蓝色, green-绿色, purple-紫色, orange-橙色',
  `custom_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#2C6FD1' COMMENT '自定义主题色',
  `layout` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'sidebar' COMMENT '布局: sidebar-侧边布局, top-顶部布局, right-右侧布局',
  `table_density` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'standard' COMMENT '表格密度: compact-紧凑, standard-标准, loose-宽松',
  `language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'zh-CN' COMMENT '语言: zh-CN-简体中文, en-US-English, zh-TW-繁體中文',
  `theme_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'light' COMMENT '主题模式: light-浅色, dark-深色, auto-跟随系统',
  `animation_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用动画',
  `default_homepage` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'dashboard' COMMENT '默认首页',
  `default_project_view` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'list' COMMENT '默认项目视图: list-列表, card-卡片, tree-树形',
  `default_page_size` int NULL DEFAULT 10 COMMENT '默认分页大小',
  `time_format` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'YYYY-MM-DD HH:mm:ss' COMMENT '时间格式',
  `auto_save` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用自动保存',
  `show_welcome_page` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示欢迎页',
  `remember_tabs` tinyint(1) NULL DEFAULT 1 COMMENT '是否记住标签页',
  `session_timeout` int NULL DEFAULT 30 COMMENT '会话超时时间(分钟)',
  `max_failures` int NULL DEFAULT 5 COMMENT '登录失败锁定次数',
  `lockout_duration` int NULL DEFAULT 30 COMMENT '账户锁定时长(分钟)',
  `two_factor_auth` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用双因素认证',
  `ip_restriction` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off' COMMENT '登录IP限制: off-关闭, whitelist-白名单',
  `password_policy` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '高强度 (至少8位,包含大小写字母、数字和特殊字符)' COMMENT '密码策略',
  `data_retention` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '30天' COMMENT '数据保留周期',
  `backup_strategy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'daily' COMMENT '备份策略: daily-每日, weekly-每周, monthly-每月',
  `log_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'debug' COMMENT '日志级别: debug, info, warn, error',
  `log_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/var/log/apiops/' COMMENT '日志存储路径',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统基本设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'test_case' COMMENT '任务类型: test_case(测试用例), bug_fix(缺陷修复), api_test(接口测试), performance(性能测试), manual(手动测试)',
  `task_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `task_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务描述',
  `priority` int NULL DEFAULT 3 COMMENT '优先级: 1-紧急, 2-高, 3-中, 4-低',
  `severity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '严重程度: critical, high, medium, low',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '状态: pending(待处理), in_progress(进行中), completed(已完成), cancelled(已取消)',
  `progress` int NULL DEFAULT 0 COMMENT '进度百分比: 0-100',
  `due_date` datetime NULL DEFAULT NULL COMMENT '截止日期',
  `start_date` datetime NULL DEFAULT NULL COMMENT '开始日期',
  `completed_date` datetime NULL DEFAULT NULL COMMENT '完成日期',
  `assignee_id` int NULL DEFAULT NULL COMMENT '被分配人ID',
  `assigner_id` int NULL DEFAULT NULL COMMENT '分配人ID',
  `project_id` int NULL DEFAULT NULL COMMENT '关联项目ID',
  `test_case_id` int NULL DEFAULT NULL COMMENT '关联测试用例ID',
  `execution_id` bigint NULL DEFAULT NULL COMMENT '关联执行记录ID',
  `report_id` bigint NULL DEFAULT NULL COMMENT '关联报告ID',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签, 多个用逗号分隔',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` int NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `idx_assignee`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE,
  INDEX `idx_project`(`project_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testassertions
-- ----------------------------
DROP TABLE IF EXISTS `testassertions`;
CREATE TABLE `testassertions`  (
  `assertion_id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL COMMENT '关联TestCaseResults',
  `step_id` bigint NULL DEFAULT NULL COMMENT '关联TestStepResults',
  `assertion_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '断言名称',
  `assertion_type` enum('EQUALS','NOT_EQUALS','CONTAINS','NOT_CONTAINS','GREATER_THAN','LESS_THAN','IS_NULL','NOT_NULL','IS_TRUE','IS_FALSE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `expected_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '期望值',
  `actual_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '实际值',
  `assertion_status` enum('PASSED','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `failure_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '失败信息',
  `comparison_details` json NULL COMMENT '比较详情',
  `assertion_order` int NULL DEFAULT 0 COMMENT '断言顺序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`assertion_id`) USING BTREE,
  INDEX `idx_result_id`(`result_id` ASC) USING BTREE,
  INDEX `idx_step_id`(`step_id` ASC) USING BTREE,
  INDEX `idx_assertion_status`(`assertion_status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试断言结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testattachments
-- ----------------------------
DROP TABLE IF EXISTS `testattachments`;
CREATE TABLE `testattachments`  (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `result_id` bigint NOT NULL COMMENT '关联TestCaseResults',
  `step_id` bigint NULL DEFAULT NULL COMMENT '关联TestStepResults',
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件名称',
  `attachment_type` enum('SCREENSHOT','VIDEO','LOG','HAR','JSON','XML','TEXT','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件URL',
  `attachment_size` bigint NULL DEFAULT 0 COMMENT '附件大小(字节)',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '附件描述',
  `screenshot_type` enum('FULL_PAGE','VIEWPORT','ELEMENT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '截图类型',
  `element_selector` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '元素选择器',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`) USING BTREE,
  INDEX `idx_result_id`(`result_id` ASC) USING BTREE,
  INDEX `idx_step_id`(`step_id` ASC) USING BTREE,
  INDEX `idx_attachment_type`(`attachment_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testcaseresults
-- ----------------------------
DROP TABLE IF EXISTS `testcaseresults`;
CREATE TABLE `testcaseresults`  (
  `result_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结果ID，自增主键',
  `execution_record_id` bigint NOT NULL COMMENT '测试执行记录ID，关联TestExecutionRecords表',
  `report_id` bigint NULL DEFAULT NULL COMMENT '外键，关联TestReportSummaries表',
  `execution_id` bigint NULL DEFAULT NULL COMMENT '执行记录ID，关联TaskExecutionHistory表',
  `execution_id_str` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行ID字符串',
  `task_type` enum('test_suite','test_case','project','module','api_monitor') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'test_suite' COMMENT '任务类型',
  `ref_id` int NOT NULL COMMENT '根据task_type关联对应表的ID',
  `full_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Allure中的完整名称（包含路径）',
  `status` enum('passed','failed','broken','skipped','unknown') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '该用例的执行状态',
  `duration` bigint NULL DEFAULT NULL COMMENT '该用例执行耗时（毫秒）',
  `start_time` datetime NULL DEFAULT NULL COMMENT '用例开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '用例结束时间',
  `failure_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '失败信息',
  `failure_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '失败堆栈跟踪',
  `failure_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败类型',
  `error_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误代码',
  `steps_json` json NULL COMMENT '测试步骤执行详情',
  `parameters_json` json NULL COMMENT '测试参数信息',
  `attachments_json` json NULL COMMENT '附件信息',
  `logs_link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '该用例详细日志的链接',
  `screenshot_link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '截图链接',
  `video_link` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '视频录制链接',
  `environment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行环境',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作系统',
  `device` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备信息',
  `tags_json` json NULL COMMENT '标签信息',
  `severity` enum('blocker','critical','high','normal','minor','trivial') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '严重程度',
  `priority` enum('P0','P1','P2','P3') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '优先级',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `flaky` tinyint(1) NULL DEFAULT 0 COMMENT '是否是不稳定用例',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  `test_layer` enum('UNIT','INTEGRATION','API','E2E','PERFORMANCE','SECURITY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'API' COMMENT '测试层级',
  `test_type` enum('POSITIVE','NEGATIVE','BOUNDARY','SECURITY','PERFORMANCE','USABILITY') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'POSITIVE' COMMENT '测试类型',
  `flaky_count` int NULL DEFAULT 0 COMMENT '不稳定次数',
  `last_flaky_time` datetime NULL DEFAULT NULL COMMENT '最后一次不稳定时间',
  `history_trend` json NULL COMMENT '历史趋势数据',
  `custom_labels` json NULL COMMENT '自定义标签',
  `root_cause_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '根因分析',
  `impact_assessment` enum('HIGH','MEDIUM','LOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '影响评估',
  `retest_result` enum('PASSED','FAILED','NOT_RETESTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOT_RETESTED' COMMENT '复测结果',
  `retest_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '复测备注',
  `case_id` int NULL DEFAULT NULL COMMENT '关联TestCases',
  PRIMARY KEY (`result_id`) USING BTREE,
  INDEX `idx_report_id`(`report_id` ASC) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_report_status`(`report_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_duration`(`duration` ASC) USING BTREE,
  INDEX `idx_environment`(`environment` ASC) USING BTREE,
  INDEX `idx_browser`(`browser` ASC) USING BTREE,
  INDEX `ids_is_deletd`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_execution_record_id`(`execution_record_id` ASC) USING BTREE,
  INDEX `idx_test_layer`(`test_layer` ASC) USING BTREE,
  INDEX `idx_execution_id_str`(`execution_id_str` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1796 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试用例结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testcases
-- ----------------------------
DROP TABLE IF EXISTS `testcases`;
CREATE TABLE `testcases`  (
  `case_id` int NOT NULL AUTO_INCREMENT COMMENT '用例ID，自增主键',
  `case_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用例编码，接口内唯一',
  `api_id` int NOT NULL COMMENT '接口ID，关联Apis表的主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用例名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用例描述',
  `test_type` enum('functional','performance','security','compatibility','smoke','regression') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'functional' COMMENT '测试类型：functional-功能, performance-性能, security-安全, compatibility-兼容性, smoke-冒烟, regression-回归',
  `priority` enum('P0','P1','P2','P3') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'P2' COMMENT '优先级',
  `severity` enum('critical','high','medium','low') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '严重程度',
  `tags` json NULL COMMENT '标签，JSON数组格式',
  `pre_conditions` json NULL COMMENT '前置条件，JSON格式',
  `test_steps` json NULL COMMENT '测试步骤，JSON格式',
  `request_override` json NULL COMMENT '请求参数覆盖',
  `expected_http_status` int NULL DEFAULT NULL COMMENT '预期HTTP状态码',
  `expected_response_schema` json NULL COMMENT '预期响应Schema，JSON格式',
  `expected_response_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '预期响应体',
  `assertions` json NULL COMMENT '断言规则，JSON格式',
  `extractors` json NULL COMMENT '响应提取规则',
  `validators` json NULL COMMENT '验证器配置',
  `is_enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `is_template` tinyint(1) NULL DEFAULT 0 COMMENT '是否为模板用例',
  `template_id` int NULL DEFAULT NULL COMMENT '模板用例ID',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `created_by` int NOT NULL COMMENT '创建人ID，关联用户表',
  `updated_by` int NULL DEFAULT NULL COMMENT '更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`case_id`) USING BTREE,
  UNIQUE INDEX `uk_api_case_code`(`api_id` ASC, `case_code` ASC) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_case_code`(`case_code` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_is_enabled`(`is_enabled` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_test_type`(`test_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10068 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用例信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testexecutionrecords
-- ----------------------------
DROP TABLE IF EXISTS `testexecutionrecords`;
CREATE TABLE `testexecutionrecords`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，自增主键',
  `execution_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行ID字符串，用于外部引用',
  `execution_scope` enum('api','module','project','test_suite','test_case') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行范围类型',
  `ref_id` int NOT NULL COMMENT '根据execution_scope关联对应表的ID',
  `scope_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行范围的名称',
  `executed_by` int NOT NULL COMMENT '执行人ID，关联Users表',
  `execution_type` enum('manual','scheduled','triggered') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'manual' COMMENT '执行类型',
  `environment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试环境',
  `status` enum('running','completed','failed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'running' COMMENT '执行状态',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration_seconds` int NULL DEFAULT 0 COMMENT '执行耗时(秒)',
  `total_cases` int NULL DEFAULT 0 COMMENT '总用例数',
  `failed_cases` int NULL DEFAULT 0 COMMENT '失败用例数',
  `success_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '成功率',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器类型',
  `app_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用版本',
  `execution_config` json NULL COMMENT '执行配置信息',
  `report_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报告访问地址',
  `log_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文件路径',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `triggered_task_id` bigint NULL DEFAULT NULL COMMENT '触发任务ID，关联ScheduledTasks表',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_execution_scope_ref`(`execution_scope` ASC, `ref_id` ASC) USING BTREE,
  INDEX `idx_executed_by`(`executed_by` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_environment`(`environment` ASC) USING BTREE,
  INDEX `idx_triggered_task_id`(`triggered_task_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 679 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试执行记录表，记录手动或自动的测试执行历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testreportconfigs
-- ----------------------------
DROP TABLE IF EXISTS `testreportconfigs`;
CREATE TABLE `testreportconfigs`  (
  `config_id` int NOT NULL AUTO_INCREMENT,
  `report_type` enum('ISO_ENTERPRISE','EXECUTION_SUMMARY','DEFECT_ANALYSIS','TREND_ANALYSIS') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ISO_ENTERPRISE',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称',
  `sections_config` json NULL COMMENT '模块配置',
  `charts_config` json NULL COMMENT '图表配置',
  `style_config` json NULL COMMENT '样式配置',
  `data_filters` json NULL COMMENT '数据过滤条件',
  `metrics_config` json NULL COMMENT '指标计算配置',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  `is_default` tinyint(1) NULL DEFAULT 0,
  `created_by` int NOT NULL,
  `updated_by` int NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_name`(`config_name` ASC) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试报告配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testreportgenerations
-- ----------------------------
DROP TABLE IF EXISTS `testreportgenerations`;
CREATE TABLE `testreportgenerations`  (
  `generation_id` bigint NOT NULL AUTO_INCREMENT,
  `report_id` bigint NOT NULL COMMENT '关联TestReportSummaries',
  `config_id` int NOT NULL COMMENT '关联TestReportConfigs',
  `report_format` enum('HTML','PDF','EXCEL','JSON') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'HTML',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint NULL DEFAULT 0,
  `download_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('GENERATING','COMPLETED','FAILED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'GENERATING',
  `generation_start_time` datetime NOT NULL,
  `generation_end_time` datetime NULL DEFAULT NULL,
  `generation_duration` bigint NULL DEFAULT 0,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `error_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `generation_params` json NULL COMMENT '生成参数',
  `data_snapshot` json NULL COMMENT '数据快照',
  `created_by` int NOT NULL,
  `updated_by` int NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`generation_id`) USING BTREE,
  INDEX `idx_report_id`(`report_id` ASC) USING BTREE,
  INDEX `idx_config_id`(`config_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试报告生成记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testreportsummaries
-- ----------------------------
DROP TABLE IF EXISTS `testreportsummaries`;
CREATE TABLE `testreportsummaries`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID，自增主键',
  `report_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报告名称',
  `report_type` enum('execution','coverage','trend','comparison','custom') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'execution' COMMENT '报告类型',
  `execution_id` bigint NULL DEFAULT NULL COMMENT '执行记录ID，关联TaskExecutionHistory表',
  `project_id` int NOT NULL COMMENT '项目ID，关联Projects表',
  `environment` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试环境',
  `start_time` datetime NOT NULL COMMENT '测试开始时间',
  `end_time` datetime NOT NULL COMMENT '测试结束时间',
  `duration` bigint NOT NULL COMMENT '总耗时（毫秒）',
  `total_cases` int NOT NULL DEFAULT 0 COMMENT '总用例数',
  `executed_cases` int NOT NULL DEFAULT 0 COMMENT '已执行用例数',
  `passed_cases` int NOT NULL DEFAULT 0 COMMENT '通过用例数',
  `failed_cases` int NOT NULL DEFAULT 0 COMMENT '失败用例数',
  `broken_cases` int NOT NULL DEFAULT 0 COMMENT '中断用例数',
  `skipped_cases` int NOT NULL DEFAULT 0 COMMENT '跳过用例数',
  `success_rate` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '成功率',
  `total_duration` bigint NOT NULL DEFAULT 0 COMMENT '总执行耗时（毫秒）',
  `avg_duration` bigint NOT NULL DEFAULT 0 COMMENT '平均用例耗时（毫秒）',
  `max_duration` bigint NOT NULL DEFAULT 0 COMMENT '最大用例耗时（毫秒）',
  `min_duration` bigint NOT NULL DEFAULT 0 COMMENT '最小用例耗时（毫秒）',
  `report_status` enum('generating','completed','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'generating' COMMENT '报告状态',
  `file_format` enum('html','pdf','excel','json','xml') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'html' COMMENT '报告格式',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报告文件存储路径',
  `file_size` bigint NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `download_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下载地址',
  `generated_by` int NOT NULL COMMENT '生成人员ID，关联Users表',
  `tags_json` json NULL COMMENT '报告标签',
  `summary_json` json NULL COMMENT '汇总统计信息',
  `trend_data_json` json NULL COMMENT '趋势数据',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  `report_config_id` int NULL DEFAULT NULL COMMENT '报告配置ID',
  `iso_metrics` json NULL COMMENT 'ISO标准指标数据',
  `executive_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '执行摘要',
  `conclusion_recommendation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '结论建议',
  `risk_assessment` json NULL COMMENT '风险评估数据',
  `defect_analysis` json NULL COMMENT '缺陷分析数据',
  `environment_details` json NULL COMMENT '环境详细信息',
  `test_scope_details` json NULL COMMENT '测试范围详情',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_execution_id`(`execution_id` ASC) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC) USING BTREE,
  INDEX `idx_environment`(`environment` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_report_status`(`report_status` ASC) USING BTREE,
  INDEX `idx_generated_by`(`generated_by` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 651 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试报告汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for testsuites
-- ----------------------------
DROP TABLE IF EXISTS `testsuites`;
CREATE TABLE `testsuites`  (
  `suite_id` int NOT NULL AUTO_INCREMENT COMMENT '测试集合ID，自增主键',
  `suite_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试集合名称',
  `suite_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '测试集合编码',
  `project_id` int NOT NULL COMMENT '项目ID，关联Projects表',
  `module_id` int NULL DEFAULT NULL COMMENT '模块ID，关联Modules表',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '测试集合描述',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `status` enum('active','inactive','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '状态',
  `created_by` int NOT NULL COMMENT '创建人ID，关联Users表',
  `updated_by` int NULL DEFAULT NULL COMMENT '最后更新人ID',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  `parent_suite_id` int NULL DEFAULT NULL COMMENT '父套件ID',
  `suite_type` enum('MODULE','FEATURE','EPIC','PACKAGE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MODULE' COMMENT '套件类型',
  `suite_order` int NULL DEFAULT 0 COMMENT '套件顺序',
  `tags` json NULL COMMENT '标签',
  `custom_properties` json NULL COMMENT '自定义属性',
  PRIMARY KEY (`suite_id`) USING BTREE,
  UNIQUE INDEX `unique_suite_project`(`suite_code` ASC, `project_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_module_id`(`module_id` ASC) USING BTREE,
  INDEX `idx_suite_code`(`suite_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_by`(`created_by` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE,
  INDEX `idx_parent_suite_id`(`parent_suite_id` ASC) USING BTREE,
  INDEX `idx_suite_type`(`suite_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1016 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '测试集合信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户邮箱，唯一',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户手机号码，允许为空',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码，加密存储',
  `department_id` int NULL DEFAULT NULL COMMENT '部门ID，关联部门表',
  `employee_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '员工工号',
  `created_by` int NULL DEFAULT NULL COMMENT '创建人ID，关联用户表',
  `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注/描述',
  `status` enum('active','inactive','pending') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '用户状态：激活、非激活、待审核',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：admin-管理员，user-普通用户',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '删除时间',
  `deleted_by` int NULL DEFAULT NULL COMMENT '删除人ID',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for v_scheduled_task_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_scheduled_task_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_scheduled_task_summary` AS select `st`.`task_id` AS `task_id`,`st`.`task_name` AS `task_name`,`st`.`task_type` AS `task_type`,`st`.`target_name` AS `target_name`,`st`.`execution_environment` AS `execution_environment`,`st`.`is_enabled` AS `is_enabled`,`st`.`total_executions` AS `total_executions`,`st`.`successful_executions` AS `successful_executions`,`st`.`failed_executions` AS `failed_executions`,`st`.`skipped_executions` AS `skipped_executions`,round((case when (`st`.`total_executions` > 0) then ((`st`.`successful_executions` * 100.0) / `st`.`total_executions`) else 0 end),2) AS `success_rate`,`st`.`next_trigger_time` AS `next_trigger_time`,`st`.`last_execution_time` AS `last_execution_time`,`st`.`last_execution_status` AS `last_execution_status`,`st`.`created_at` AS `created_at` from `scheduledtesttasks` `st` where (`st`.`is_deleted` = 0);

SET FOREIGN_KEY_CHECKS = 1;

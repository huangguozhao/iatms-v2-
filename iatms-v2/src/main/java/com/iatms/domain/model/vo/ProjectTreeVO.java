package com.iatms.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 项目树形结构VO - 包含完整的项目→模块→接口→用例层级
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTreeVO {

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型: project, module, api, testcase
     */
    private String type;

    /**
     * 项目ID（模块/接口/用例所属项目）
     */
    private Long projectId;

    /**
     * 模块ID（接口/用例所属模块）
     */
    private Long moduleId;

    /**
     * 接口ID（用例所属接口）
     */
    private Long apiId;

    /**
     * 父节点ID（用于模块的层级关系）
     */
    private Long parentId;

    /**
     * 节点编码
     */
    private String code;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 状态
     */
    private String status;

    /**
     * HTTP方法（仅接口节点）
     */
    private String httpMethod;

    /**
     * 接口路径（仅接口节点）
     */
    private String path;

    /**
     * 优先级（仅用例节点）
     */
    private String priority;

    /**
     * 测试类型（仅用例节点）：functional, performance, security, compatibility, smoke, regression
     */
    private String testType;

    /**
     * 测试数据（仅用例节点）
     */
    private String testData;

    /**
     * 请求体（仅用例节点）
     */
    private String requestBody;

    /**
     * 预期结果（仅用例节点）
     */
    private String expectedResult;

    /**
     * 预期HTTP状态码（仅用例节点）
     */
    private Integer expectedHttpStatus;

    /**
     * 是否启用（仅用例节点）
     */
    private Boolean isEnabled;

    /**
     * 子模块列表
     */
    private List<ProjectTreeVO> children;

    /**
     * 接口列表（仅模块节点）
     */
    private List<ProjectTreeVO> apis;

    /**
     * 用例列表（仅接口节点）
     */
    private List<ProjectTreeVO> testCases;

    /**
     * 统计信息
     */
    private Stats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private Integer moduleCount;
        private Integer apiCount;
        private Integer testCaseCount;
    }
}

package org.dromara.demo.project.dto;

/**
 * 项目分页/导出查询条件。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ProjectQuery {

    private Long pageNum;
    private Long pageSize;
    private String projectType;
    private Long deptId;
    private String status;
    private String name;

    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

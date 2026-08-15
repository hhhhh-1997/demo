package org.dromara.demo.autotest.dto;

/**
 * 运行记录分页查询条件。
 *
 * @author demo
 * @since 2026-08-15
 */
public class RunQuery {

    private Long pageNum;
    private Long pageSize;
    private Long scenarioId;
    private Integer status;

    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

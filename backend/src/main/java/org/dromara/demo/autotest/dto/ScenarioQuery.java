package org.dromara.demo.autotest.dto;

/**
 * 场景分页查询条件。
 *
 * @author demo
 * @since 2026-08-15
 */
public class ScenarioQuery {

    private Long pageNum;
    private Long pageSize;
    private String name;

    public Long getPageNum() { return pageNum; }
    public void setPageNum(Long pageNum) { this.pageNum = pageNum; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

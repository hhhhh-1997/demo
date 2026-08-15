package org.dromara.demo.autotest.vo;

import java.time.LocalDateTime;

/**
 * 运行记录展示对象。
 *
 * @author demo
 * @since 2026-08-15
 */
public class RunVO {

    private Long id;
    private Long scenarioId;
    private Integer status;
    private Long failStepId;
    private String errorMsg;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long triggerBy;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getFailStepId() { return failStepId; }
    public void setFailStepId(Long failStepId) { this.failStepId = failStepId; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getTriggerBy() { return triggerBy; }
    public void setTriggerBy(Long triggerBy) { this.triggerBy = triggerBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

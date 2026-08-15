package org.dromara.demo.autotest.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行详情展示对象（运行记录 + 每步结果）。
 *
 * @author demo
 * @since 2026-08-15
 */
public class RunDetailVO {

    private Long id;
    private Long scenarioId;
    private Integer status;
    private Long failStepId;
    private String errorMsg;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<StepResultVO> steps;

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
    public List<StepResultVO> getSteps() { return steps; }
    public void setSteps(List<StepResultVO> steps) { this.steps = steps; }
}

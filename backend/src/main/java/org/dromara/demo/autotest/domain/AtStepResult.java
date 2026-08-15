package org.dromara.demo.autotest.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 自动化测试步骤执行结果。
 *
 * @author demo
 * @since 2026-08-15
 */
@TableName("at_step_result")
public class AtStepResult {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long runId;
    private Long stepId;
    private Integer stepOrder;
    private String name;
    private Integer status;
    private String requestSnapshot;
    private String responseSnapshot;
    private String assertDetail;
    private String errorMsg;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }
    public Long getStepId() { return stepId; }
    public void setStepId(Long stepId) { this.stepId = stepId; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRequestSnapshot() { return requestSnapshot; }
    public void setRequestSnapshot(String requestSnapshot) { this.requestSnapshot = requestSnapshot; }
    public String getResponseSnapshot() { return responseSnapshot; }
    public void setResponseSnapshot(String responseSnapshot) { this.responseSnapshot = responseSnapshot; }
    public String getAssertDetail() { return assertDetail; }
    public void setAssertDetail(String assertDetail) { this.assertDetail = assertDetail; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}

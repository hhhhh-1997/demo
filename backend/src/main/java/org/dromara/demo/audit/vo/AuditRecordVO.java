package org.dromara.demo.audit.vo;

import java.time.LocalDateTime;

/**
 * 审核记录展示对象。
 *
 * @author demo
 * @since 2026-08-14
 */
public class AuditRecordVO {

    private String opinion;
    private String result;
    private LocalDateTime auditTime;

    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
}

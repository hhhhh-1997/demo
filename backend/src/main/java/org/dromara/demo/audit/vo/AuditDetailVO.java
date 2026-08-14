package org.dromara.demo.audit.vo;

import org.dromara.demo.project.vo.ProjectVO;

/**
 * 审核详情展示对象（项目 + 最新审核记录 + 最新论证结论摘要）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class AuditDetailVO {

    private ProjectVO project;
    private AuditRecordVO audit;
    private String reviewResult;
    private String reviewOpinion;

    public ProjectVO getProject() { return project; }
    public void setProject(ProjectVO project) { this.project = project; }
    public AuditRecordVO getAudit() { return audit; }
    public void setAudit(AuditRecordVO audit) { this.audit = audit; }
    public String getReviewResult() { return reviewResult; }
    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public String getReviewOpinion() { return reviewOpinion; }
    public void setReviewOpinion(String reviewOpinion) { this.reviewOpinion = reviewOpinion; }
}

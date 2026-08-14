package org.dromara.demo.reserve.vo;

import org.dromara.demo.audit.vo.AuditRecordVO;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.review.vo.ReviewRecordVO;

/**
 * 统一储备库详情展示对象（项目 + 最新论证 + 最新审核记录）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ReserveDetailVO {

    private ProjectVO project;
    private ReviewRecordVO review;
    private AuditRecordVO audit;

    public ProjectVO getProject() { return project; }
    public void setProject(ProjectVO project) { this.project = project; }
    public ReviewRecordVO getReview() { return review; }
    public void setReview(ReviewRecordVO review) { this.review = review; }
    public AuditRecordVO getAudit() { return audit; }
    public void setAudit(AuditRecordVO audit) { this.audit = audit; }
}

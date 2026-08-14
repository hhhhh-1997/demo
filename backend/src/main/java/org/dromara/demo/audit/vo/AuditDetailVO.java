package org.dromara.demo.audit.vo;

import org.dromara.demo.project.vo.ProjectVO;

/**
 * 审核详情展示对象（项目 + 最新审核记录）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class AuditDetailVO {

    private ProjectVO project;
    private AuditRecordVO audit;

    public ProjectVO getProject() { return project; }
    public void setProject(ProjectVO project) { this.project = project; }
    public AuditRecordVO getAudit() { return audit; }
    public void setAudit(AuditRecordVO audit) { this.audit = audit; }
}

package org.dromara.demo.audit.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 审核执行请求体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class AuditCommand {

    private String opinion;

    @NotNull(message = "审核结论不能为空")
    private Boolean pass;

    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
    public Boolean getPass() { return pass; }
    public void setPass(Boolean pass) { this.pass = pass; }
}

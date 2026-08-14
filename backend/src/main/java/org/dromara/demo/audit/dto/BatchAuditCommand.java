package org.dromara.demo.audit.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量审核请求体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class BatchAuditCommand {

    @NotEmpty(message = "请选择要审核的项目")
    private List<Long> ids;

    @NotNull(message = "审核结论不能为空")
    private Boolean pass;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
    public Boolean getPass() { return pass; }
    public void setPass(Boolean pass) { this.pass = pass; }
}

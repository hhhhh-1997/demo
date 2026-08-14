package org.dromara.demo.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.audit.dto.AuditCommand;
import org.dromara.demo.audit.dto.BatchAuditCommand;
import org.dromara.demo.audit.vo.AuditDetailVO;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.service.ProjectService;
import org.dromara.demo.project.vo.ProjectVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 储备项目审核接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final ProjectService projectService;

    public AuditController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @SaCheckPermission("audit:view")
    @GetMapping("/page")
    public Result<PageResult<ProjectVO>> page(ProjectQuery q) {
        return Result.success(projectService.auditPage(q));
    }

    @SaCheckPermission("audit:view")
    @GetMapping("/{id}")
    public Result<AuditDetailVO> detail(@PathVariable Long id) {
        return Result.success(projectService.auditDetail(id));
    }

    @SaCheckPermission("audit:execute")
    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id, @Validated @RequestBody AuditCommand cmd) {
        projectService.audit(id, cmd);
        return Result.success();
    }

    @SaCheckPermission("audit:batch")
    @PostMapping("/batch")
    public Result<Void> batch(@Validated @RequestBody BatchAuditCommand cmd) {
        projectService.auditBatch(cmd.getIds(), cmd.getPass());
        return Result.success();
    }

    @SaCheckPermission("audit:view")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(projectService.auditStats());
    }
}

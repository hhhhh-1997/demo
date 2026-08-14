package org.dromara.demo.project.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.dto.ProjectSaveDTO;
import org.dromara.demo.project.service.ProjectService;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.system.domain.SysDept;
import org.dromara.demo.system.mapper.SysDeptMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 储备项目维护接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectService projectService;
    private final SysDeptMapper deptMapper;

    public ProjectController(ProjectService projectService, SysDeptMapper deptMapper) {
        this.projectService = projectService;
        this.deptMapper = deptMapper;
    }

    @SaCheckLogin
    @GetMapping("/page")
    public Result<PageResult<ProjectVO>> page(ProjectQuery q) {
        return Result.success(projectService.page(q));
    }

    @SaCheckLogin
    @GetMapping("/{id}")
    public Result<ProjectVO> get(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @SaCheckPermission("project:add")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody ProjectSaveDTO dto) {
        return Result.success(projectService.create(dto));
    }

    @SaCheckPermission("project:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ProjectSaveDTO dto) {
        projectService.update(id, dto);
        return Result.success();
    }

    @SaCheckPermission("project:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @SaCheckPermission("project:submit")
    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        projectService.submit(id);
        return Result.success();
    }

    @SaCheckPermission("project:submit")
    @PostMapping("/submit/batch")
    public Result<Void> submitBatch(@RequestBody List<Long> ids) {
        projectService.submitBatch(ids);
        return Result.success();
    }

    @SaCheckPermission("project:export")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(ProjectQuery q) {
        List<Project> list = projectService.exportList(q);
        Map<Long, String> deptNames = deptMapper.selectList(
                        new LambdaQueryWrapper<SysDept>().eq(SysDept::getStatus, 1)).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        String csv = toCsv(list, deptNames);
        byte[] bytes = ("﻿" + csv).getBytes(StandardCharsets.UTF_8);
        String filename = "储备项目_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @SaCheckLogin
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(projectService.stats());
    }

    private String toCsv(List<Project> list, Map<Long, String> deptNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("项目编码,项目名称,二级分类,投资金额(元),所属单位,状态,创建时间,下达时间,描述\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Project p : list) {
            sb.append(escape(p.getProjectCode())).append(',')
                    .append(escape(p.getProjectName())).append(',')
                    .append(escape(p.getProjectType())).append(',')
                    .append(p.getInvestmentAmount() == null ? "" : p.getInvestmentAmount().toPlainString()).append(',')
                    .append(escape(p.getDeptId() == null ? "" : deptNames.getOrDefault(p.getDeptId(), ""))).append(',')
                    .append(escape(p.getStatus())).append(',')
                    .append(p.getCreateTime() == null ? "" : dtf.format(p.getCreateTime())).append(',')
                    .append(p.getIssueTime() == null ? "" : dtf.format(p.getIssueTime())).append(',')
                    .append(escape(p.getDescription())).append('\n');
        }
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

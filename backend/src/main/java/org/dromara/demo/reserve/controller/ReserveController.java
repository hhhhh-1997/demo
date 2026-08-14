package org.dromara.demo.reserve.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.service.ProjectService;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.reserve.vo.ReserveDetailVO;
import org.dromara.demo.reserve.vo.ReserveStatsVO;
import org.dromara.demo.system.domain.SysDept;
import org.dromara.demo.system.mapper.SysDeptMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
 * 统一储备库接口（待下达 / 已下达项目的查看、下达、导出与图表统计）。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/reserve")
public class ReserveController {

    private final ProjectService projectService;
    private final SysDeptMapper deptMapper;

    public ReserveController(ProjectService projectService, SysDeptMapper deptMapper) {
        this.projectService = projectService;
        this.deptMapper = deptMapper;
    }

    @SaCheckPermission("reserve:view")
    @GetMapping("/page")
    public Result<PageResult<ProjectVO>> page(ProjectQuery q) {
        return Result.success(projectService.reservePage(q));
    }

    @SaCheckPermission("reserve:view")
    @GetMapping("/{id}")
    public Result<ReserveDetailVO> detail(@PathVariable Long id) {
        return Result.success(projectService.reserveDetail(id));
    }

    @SaCheckPermission("reserve:issue")
    @PostMapping("/{id}/issue")
    public Result<Void> issue(@PathVariable Long id) {
        projectService.issue(id);
        return Result.success();
    }

    @SaCheckPermission("reserve:export")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(ProjectQuery q) {
        List<Project> list = projectService.reserveExportList(q);
        Map<Long, String> deptNames = deptMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
        String csv = toCsv(list, deptNames);
        byte[] bytes = ("﻿" + csv).getBytes(StandardCharsets.UTF_8);
        String filename = "统一储备库_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    @SaCheckPermission("reserve:view")
    @GetMapping("/stats")
    public Result<ReserveStatsVO> stats() {
        return Result.success(projectService.reserveStats());
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

package org.dromara.demo.project.service;

import org.dromara.demo.common.BusinessException;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.domain.ProjectStatus;
import org.dromara.demo.project.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 项目状态机与维护服务。
 *
 * @author demo
 * @since 2026-08-14
 */
@Service
public class ProjectService {

    private final ProjectMapper projectMapper;

    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    /**
     * 项目提报：草稿 / 论证退回 / 审核退回 → 待论证。
     *
     * @param id 项目 ID
     */
    public void submit(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectStatus s = ProjectStatus.fromCode(p.getStatus());
        if (s != ProjectStatus.DRAFT && s != ProjectStatus.REVIEW_REJECTED && s != ProjectStatus.AUDIT_REJECTED) {
            throw new BusinessException("当前状态不允许提报");
        }
        p.setStatus(ProjectStatus.PENDING_REVIEW.getCode());
        p.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    /**
     * 项目下达：待下达 → 已下达。
     *
     * @param id 项目 ID
     */
    public void issue(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        if (ProjectStatus.fromCode(p.getStatus()) != ProjectStatus.PENDING_ISSUE) {
            throw new BusinessException("仅待下达项目可下达");
        }
        p.setStatus(ProjectStatus.ISSUED.getCode());
        p.setIssueTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    /**
     * 生成项目编号：XM + yyyyMM + 3 位序号。
     *
     * @return 项目编号
     */
    public String generateCode() {
        String prefix = "XM" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String max = projectMapper.selectMaxCodeByMonth(prefix);
        int seq = 1;
        if (max != null && max.startsWith(prefix)) {
            seq = Integer.parseInt(max.substring(prefix.length())) + 1;
        }
        return prefix + String.format("%03d", seq);
    }
}

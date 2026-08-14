package org.dromara.demo.project.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.domain.ProjectStatus;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.dto.ProjectSaveDTO;
import org.dromara.demo.project.mapper.ProjectMapper;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.system.domain.SysDept;
import org.dromara.demo.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目状态机与维护服务。
 *
 * @author demo
 * @since 2026-08-14
 */
@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final SysDeptMapper deptMapper;

    public ProjectService(ProjectMapper projectMapper, SysDeptMapper deptMapper) {
        this.projectMapper = projectMapper;
        this.deptMapper = deptMapper;
    }

    /**
     * 项目提报：草稿 / 论证退回 / 审核退回 → 待论证。
     *
     * @param id 项目 ID
     */
    @Transactional(rollbackFor = Exception.class)
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
     * 批量提报：任一失败整体回滚。
     *
     * @param ids 项目 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要提报的项目");
        }
        for (Long id : ids) {
            submit(id);
        }
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

    /**
     * 分页查询项目（可选筛选，按创建时间倒序）。
     *
     * @param q 查询条件
     * @return 分页结果
     */
    public PageResult<ProjectVO> page(ProjectQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<Project> wrapper = buildWrapper(q);
        wrapper.orderByDesc(Project::getCreateTime);
        Page<Project> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, String> deptNames = deptNameMap();
        List<ProjectVO> list = page.getRecords().stream().map(p -> toVO(p, deptNames)).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    /**
     * 查询项目详情。
     *
     * @param id 项目 ID
     * @return 项目展示对象
     */
    public ProjectVO getById(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        return toVO(p, deptNameMap());
    }

    /**
     * 新增项目：默认草稿，生成项目编号，来源手动录入。
     *
     * @param dto 项目请求体
     * @return 新项目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectSaveDTO dto) {
        Project p = new Project();
        p.setProjectName(dto.getProjectName());
        p.setProjectType(dto.getProjectType());
        p.setInvestmentAmount(dto.getInvestmentAmount());
        p.setDeptId(dto.getDeptId());
        p.setDescription(dto.getDescription());
        p.setPlanStartDate(dto.getPlanStartDate());
        p.setPlanEndDate(dto.getPlanEndDate());
        p.setValidUntil(dto.getValidUntil());
        p.setStatus(ProjectStatus.DRAFT.getCode());
        p.setProjectCode(generateCode());
        p.setSource("MANUAL");
        p.setCreateBy(StpUtil.getLoginIdAsLong());
        projectMapper.insert(p);
        return p.getId();
    }

    /**
     * 编辑项目：仅草稿 / 论证退回 / 审核退回可编辑。
     *
     * @param id  项目 ID
     * @param dto 项目请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProjectSaveDTO dto) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectStatus s = ProjectStatus.fromCode(p.getStatus());
        if (s != ProjectStatus.DRAFT && s != ProjectStatus.REVIEW_REJECTED && s != ProjectStatus.AUDIT_REJECTED) {
            throw new BusinessException("当前状态不允许编辑");
        }
        p.setProjectName(dto.getProjectName());
        p.setProjectType(dto.getProjectType());
        p.setInvestmentAmount(dto.getInvestmentAmount());
        p.setDeptId(dto.getDeptId());
        p.setDescription(dto.getDescription());
        p.setPlanStartDate(dto.getPlanStartDate());
        p.setPlanEndDate(dto.getPlanEndDate());
        p.setValidUntil(dto.getValidUntil());
        p.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    /**
     * 删除项目：仅草稿可删除。
     *
     * @param id 项目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        if (ProjectStatus.fromCode(p.getStatus()) != ProjectStatus.DRAFT) {
            throw new BusinessException("仅草稿项目可删除");
        }
        projectMapper.deleteById(id);
    }

    /**
     * 统计卡片：总数 / 草稿 / 论证退回 / 审核退回。
     *
     * @return 统计结果
     */
    public Map<String, Object> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", projectMapper.selectCount(null));
        result.put("draft", countByStatus(ProjectStatus.DRAFT));
        result.put("reviewRejected", countByStatus(ProjectStatus.REVIEW_REJECTED));
        result.put("auditRejected", countByStatus(ProjectStatus.AUDIT_REJECTED));
        return result;
    }

    /**
     * 导出：按查询条件筛选（不分页），用于 CSV 导出。
     *
     * @param q 查询条件
     * @return 项目列表
     */
    public List<Project> exportList(ProjectQuery q) {
        LambdaQueryWrapper<Project> wrapper = buildWrapper(q);
        wrapper.orderByDesc(Project::getCreateTime);
        return projectMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<Project> buildWrapper(ProjectQuery q) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(q.getProjectType()), Project::getProjectType, q.getProjectType());
        wrapper.eq(q.getDeptId() != null, Project::getDeptId, q.getDeptId());
        wrapper.eq(StringUtils.hasText(q.getStatus()), Project::getStatus, q.getStatus());
        wrapper.like(StringUtils.hasText(q.getName()), Project::getProjectName, q.getName());
        return wrapper;
    }

    private long countByStatus(ProjectStatus status) {
        return projectMapper.selectCount(new LambdaQueryWrapper<Project>().eq(Project::getStatus, status.getCode()));
    }

    private Map<Long, String> deptNameMap() {
        return deptMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (a, b) -> a));
    }

    private ProjectVO toVO(Project p, Map<Long, String> deptNames) {
        ProjectVO vo = new ProjectVO();
        vo.setId(p.getId());
        vo.setProjectCode(p.getProjectCode());
        vo.setProjectName(p.getProjectName());
        vo.setProjectType(p.getProjectType());
        vo.setInvestmentAmount(p.getInvestmentAmount());
        vo.setDeptId(p.getDeptId());
        vo.setDeptName(p.getDeptId() == null ? null : deptNames.get(p.getDeptId()));
        vo.setDescription(p.getDescription());
        vo.setStatus(p.getStatus());
        vo.setCreateTime(p.getCreateTime());
        vo.setIssueTime(p.getIssueTime());
        return vo;
    }
}

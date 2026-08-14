package org.dromara.demo.project.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.demo.audit.domain.ProjectAudit;
import org.dromara.demo.audit.dto.AuditCommand;
import org.dromara.demo.audit.mapper.ProjectAuditMapper;
import org.dromara.demo.audit.vo.AuditDetailVO;
import org.dromara.demo.audit.vo.AuditRecordVO;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.domain.ProjectStatus;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.dto.ProjectSaveDTO;
import org.dromara.demo.project.mapper.ProjectMapper;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.reserve.vo.ReserveDetailVO;
import org.dromara.demo.reserve.vo.ReserveStatsVO;
import org.dromara.demo.review.domain.ProjectReview;
import org.dromara.demo.review.dto.ReviewCommand;
import org.dromara.demo.review.mapper.ProjectReviewMapper;
import org.dromara.demo.review.vo.ReviewDetailVO;
import org.dromara.demo.review.vo.ReviewRecordVO;
import org.dromara.demo.system.domain.SysDept;
import org.dromara.demo.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ProjectReviewMapper projectReviewMapper;
    private final ProjectAuditMapper projectAuditMapper;

    public ProjectService(ProjectMapper projectMapper, SysDeptMapper deptMapper, ProjectReviewMapper projectReviewMapper,
            ProjectAuditMapper projectAuditMapper) {
        this.projectMapper = projectMapper;
        this.deptMapper = deptMapper;
        this.projectReviewMapper = projectReviewMapper;
        this.projectAuditMapper = projectAuditMapper;
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
     * 项目论证：待论证 → 待审核（通过）或 论证退回（不通过），并落一条论证记录。
     *
     * @param id  项目 ID
     * @param cmd 论证请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, ReviewCommand cmd) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        if (ProjectStatus.fromCode(p.getStatus()) != ProjectStatus.PENDING_REVIEW) {
            throw new BusinessException("仅待论证项目可论证");
        }
        ProjectReview record = new ProjectReview();
        record.setProjectId(id);
        record.setInfoComplete(cmd.getInfoComplete());
        record.setThreeImportant(cmd.getThreeImportant());
        record.setSplitProject(cmd.getSplitProject());
        record.setInterfaceConfusion(cmd.getInterfaceConfusion());
        record.setOpinion(cmd.getOpinion());
        record.setResult(cmd.getPass() ? "通过" : "不通过");
        record.setReviewBy(StpUtil.getLoginIdAsLong());
        record.setReviewTime(LocalDateTime.now());
        projectReviewMapper.insert(record);
        p.setStatus(cmd.getPass() ? ProjectStatus.PENDING_AUDIT.getCode() : ProjectStatus.REVIEW_REJECTED.getCode());
        p.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    /**
     * 项目审核：待审核 → 待下达（通过）或 审核退回（不通过），并落一条审核记录。
     *
     * @param id  项目 ID
     * @param cmd 审核请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, AuditCommand cmd) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        if (ProjectStatus.fromCode(p.getStatus()) != ProjectStatus.PENDING_AUDIT) {
            throw new BusinessException("仅待审核项目可审核");
        }
        ProjectAudit record = new ProjectAudit();
        record.setProjectId(id);
        record.setOpinion(cmd.getOpinion());
        record.setResult(cmd.getPass() ? "通过" : "退回");
        record.setAuditBy(StpUtil.getLoginIdAsLong());
        record.setAuditTime(LocalDateTime.now());
        projectAuditMapper.insert(record);
        p.setStatus(cmd.getPass() ? ProjectStatus.PENDING_ISSUE.getCode() : ProjectStatus.AUDIT_REJECTED.getCode());
        p.setUpdateTime(LocalDateTime.now());
        projectMapper.updateById(p);
    }

    /**
     * 批量审核：任一失败整体回滚。
     *
     * @param ids  项目 ID 列表
     * @param pass 是否通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void auditBatch(List<Long> ids, boolean pass) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要审核的项目");
        }
        for (Long id : ids) {
            AuditCommand cmd = new AuditCommand();
            cmd.setOpinion("批量审核");
            cmd.setPass(pass);
            audit(id, cmd);
        }
    }

    /**
     * 项目下达：待下达 → 已下达。
     *
     * @param id 项目 ID
     */
    @Transactional(rollbackFor = Exception.class)
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
     * 论证分页：仅待论证 / 待审核（论证通过）/ 论证退回，可选筛选，按创建时间倒序。
     *
     * @param q 查询条件
     * @return 分页结果
     */
    public PageResult<ProjectVO> reviewPage(ProjectQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Project::getStatus, ProjectStatus.PENDING_REVIEW.getCode(),
                ProjectStatus.PENDING_AUDIT.getCode(), ProjectStatus.REVIEW_REJECTED.getCode());
        if (StringUtils.hasText(q.getStatus())) {
            String mapped = "论证通过".equals(q.getStatus()) ? ProjectStatus.PENDING_AUDIT.getCode() : q.getStatus();
            wrapper.eq(Project::getStatus, mapped);
        }
        wrapper.eq(StringUtils.hasText(q.getProjectType()), Project::getProjectType, q.getProjectType());
        wrapper.like(StringUtils.hasText(q.getName()), Project::getProjectName, q.getName());
        wrapper.orderByDesc(Project::getCreateTime);
        Page<Project> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, String> deptNames = deptNameMap();
        List<ProjectVO> list = page.getRecords().stream().map(p -> toVO(p, deptNames)).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    /**
     * 审核分页：仅待审核 / 待下达（审核通过）/ 审核退回，可选筛选，按创建时间倒序。
     *
     * @param q 查询条件
     * @return 分页结果
     */
    public PageResult<ProjectVO> auditPage(ProjectQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Project::getStatus, ProjectStatus.PENDING_AUDIT.getCode(),
                ProjectStatus.PENDING_ISSUE.getCode(), ProjectStatus.AUDIT_REJECTED.getCode());
        if (StringUtils.hasText(q.getStatus())) {
            String mapped = "审核通过".equals(q.getStatus()) ? ProjectStatus.PENDING_ISSUE.getCode() : q.getStatus();
            wrapper.eq(Project::getStatus, mapped);
        }
        wrapper.eq(StringUtils.hasText(q.getProjectType()), Project::getProjectType, q.getProjectType());
        wrapper.like(StringUtils.hasText(q.getName()), Project::getProjectName, q.getName());
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
     * 论证详情：项目 + 最新论证记录。
     *
     * @param id 项目 ID
     * @return 论证详情
     */
    public ReviewDetailVO reviewDetail(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectReview record = projectReviewMapper.selectOne(new LambdaQueryWrapper<ProjectReview>()
                .eq(ProjectReview::getProjectId, id)
                .orderByDesc(ProjectReview::getReviewTime)
                .last("LIMIT 1"));
        ReviewDetailVO vo = new ReviewDetailVO();
        vo.setProject(toVO(p, deptNameMap()));
        vo.setReview(record == null ? null : toReviewVO(record));
        return vo;
    }

    /**
     * 审核详情：项目 + 最新审核记录。
     *
     * @param id 项目 ID
     * @return 审核详情
     */
    public AuditDetailVO auditDetail(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectAudit record = projectAuditMapper.selectOne(new LambdaQueryWrapper<ProjectAudit>()
                .eq(ProjectAudit::getProjectId, id)
                .orderByDesc(ProjectAudit::getAuditTime)
                .last("LIMIT 1"));
        AuditDetailVO vo = new AuditDetailVO();
        vo.setProject(toVO(p, deptNameMap()));
        vo.setAudit(record == null ? null : toAuditVO(record));
        return vo;
    }

    /**
     * 储备库分页：仅待下达 / 已下达，可选筛选，按创建时间倒序。
     *
     * @param q 查询条件
     * @return 分页结果
     */
    public PageResult<ProjectVO> reservePage(ProjectQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<Project> wrapper = buildReserveWrapper(q);
        wrapper.orderByDesc(Project::getCreateTime);
        Page<Project> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, String> deptNames = deptNameMap();
        List<ProjectVO> list = page.getRecords().stream().map(p -> toVO(p, deptNames)).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    /**
     * 储备库详情：项目 + 最新论证 + 最新审核记录。
     *
     * @param id 项目 ID
     * @return 储备库详情
     */
    public ReserveDetailVO reserveDetail(Long id) {
        Project p = projectMapper.selectById(id);
        if (p == null) {
            throw new BusinessException("项目不存在");
        }
        ProjectReview review = projectReviewMapper.selectOne(new LambdaQueryWrapper<ProjectReview>()
                .eq(ProjectReview::getProjectId, id)
                .orderByDesc(ProjectReview::getReviewTime)
                .last("LIMIT 1"));
        ProjectAudit audit = projectAuditMapper.selectOne(new LambdaQueryWrapper<ProjectAudit>()
                .eq(ProjectAudit::getProjectId, id)
                .orderByDesc(ProjectAudit::getAuditTime)
                .last("LIMIT 1"));
        ReserveDetailVO vo = new ReserveDetailVO();
        vo.setProject(toVO(p, deptNameMap()));
        vo.setReview(review == null ? null : toReviewVO(review));
        vo.setAudit(audit == null ? null : toAuditVO(audit));
        return vo;
    }

    /**
     * 储备库统计：总数 / 投资金额合计 / 待下达 / 已下达 / 分类与单位分布。
     *
     * @return 统计结果
     */
    public ReserveStatsVO reserveStats() {
        String pendingCode = ProjectStatus.PENDING_ISSUE.getCode();
        String issuedCode = ProjectStatus.ISSUED.getCode();
        long pending = countByStatus(ProjectStatus.PENDING_ISSUE);
        long issued = countByStatus(ProjectStatus.ISSUED);
        ReserveStatsVO vo = new ReserveStatsVO();
        vo.setTotal(pending + issued);
        vo.setTotalAmountYuan(projectMapper.sumInvestmentAmount(pendingCode, issuedCode));
        vo.setPending(pending);
        vo.setIssued(issued);
        vo.setCategoryDist(projectMapper.countGroupByType(pendingCode, issuedCode));
        vo.setDeptDist(projectMapper.countGroupByDept(pendingCode, issuedCode));
        return vo;
    }

    /**
     * 储备库导出：仅待下达 / 已下达，按查询条件筛选（不分页）。
     *
     * @param q 查询条件
     * @return 项目列表
     */
    public List<Project> reserveExportList(ProjectQuery q) {
        LambdaQueryWrapper<Project> wrapper = buildReserveWrapper(q);
        wrapper.orderByDesc(Project::getCreateTime);
        return projectMapper.selectList(wrapper);
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
     * 论证统计：待论证 / 论证通过（待审核）/ 论证退回 / 通过率。
     *
     * @return 统计结果
     */
    public Map<String, Object> reviewStats() {
        long pending = countByStatus(ProjectStatus.PENDING_REVIEW);
        long passed = countByStatus(ProjectStatus.PENDING_AUDIT);
        long rejected = countByStatus(ProjectStatus.REVIEW_REJECTED);
        long denominator = passed + rejected;
        BigDecimal passRate = denominator == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(passed).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("pending", pending);
        result.put("passed", passed);
        result.put("rejected", rejected);
        result.put("passRate", passRate);
        return result;
    }

    /**
     * 审核统计：待审核 / 审核通过（待下达）/ 审核退回 / 通过率。
     *
     * @return 统计结果
     */
    public Map<String, Object> auditStats() {
        long pending = countByStatus(ProjectStatus.PENDING_AUDIT);
        long passed = countByStatus(ProjectStatus.PENDING_ISSUE);
        long rejected = countByStatus(ProjectStatus.AUDIT_REJECTED);
        long denominator = passed + rejected;
        BigDecimal passRate = denominator == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(passed).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("pending", pending);
        result.put("passed", passed);
        result.put("rejected", rejected);
        result.put("passRate", passRate);
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

    private LambdaQueryWrapper<Project> buildReserveWrapper(ProjectQuery q) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Project::getStatus, ProjectStatus.PENDING_ISSUE.getCode(), ProjectStatus.ISSUED.getCode());
        wrapper.eq(StringUtils.hasText(q.getStatus()), Project::getStatus, q.getStatus());
        wrapper.eq(StringUtils.hasText(q.getProjectType()), Project::getProjectType, q.getProjectType());
        wrapper.eq(q.getDeptId() != null, Project::getDeptId, q.getDeptId());
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

    private ReviewRecordVO toReviewVO(ProjectReview r) {
        ReviewRecordVO vo = new ReviewRecordVO();
        vo.setInfoComplete(r.getInfoComplete());
        vo.setThreeImportant(r.getThreeImportant());
        vo.setSplitProject(r.getSplitProject());
        vo.setInterfaceConfusion(r.getInterfaceConfusion());
        vo.setOpinion(r.getOpinion());
        vo.setResult(r.getResult());
        vo.setReviewTime(r.getReviewTime());
        return vo;
    }

    private AuditRecordVO toAuditVO(ProjectAudit a) {
        AuditRecordVO vo = new AuditRecordVO();
        vo.setOpinion(a.getOpinion());
        vo.setResult(a.getResult());
        vo.setAuditTime(a.getAuditTime());
        return vo;
    }
}

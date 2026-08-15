package org.dromara.demo.autotest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.demo.autotest.domain.AtRun;
import org.dromara.demo.autotest.domain.AtStepResult;
import org.dromara.demo.autotest.dto.RunQuery;
import org.dromara.demo.autotest.mapper.AtRunMapper;
import org.dromara.demo.autotest.mapper.AtStepResultMapper;
import org.dromara.demo.autotest.vo.RunDetailVO;
import org.dromara.demo.autotest.vo.RunVO;
import org.dromara.demo.autotest.vo.StepResultVO;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自动化测试运行历史服务。
 *
 * @author demo
 * @since 2026-08-15
 */
@Service
public class RunService {

    private final AtRunMapper runMapper;
    private final AtStepResultMapper stepResultMapper;

    public RunService(AtRunMapper runMapper, AtStepResultMapper stepResultMapper) {
        this.runMapper = runMapper;
        this.stepResultMapper = stepResultMapper;
    }

    public PageResult<RunVO> page(RunQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<AtRun> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(q.getScenarioId() != null, AtRun::getScenarioId, q.getScenarioId());
        wrapper.eq(q.getStatus() != null, AtRun::getStatus, q.getStatus());
        wrapper.orderByDesc(AtRun::getCreateTime);
        Page<AtRun> page = runMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<RunVO> list = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    public RunDetailVO getById(Long id) {
        AtRun run = runMapper.selectById(id);
        if (run == null) {
            throw new BusinessException("运行记录不存在");
        }
        List<AtStepResult> results = stepResultMapper.selectList(new LambdaQueryWrapper<AtStepResult>()
                .eq(AtStepResult::getRunId, id)
                .orderByAsc(AtStepResult::getStepOrder));
        RunDetailVO vo = new RunDetailVO();
        vo.setId(run.getId());
        vo.setScenarioId(run.getScenarioId());
        vo.setStatus(run.getStatus());
        vo.setFailStepId(run.getFailStepId());
        vo.setErrorMsg(run.getErrorMsg());
        vo.setStartTime(run.getStartTime());
        vo.setEndTime(run.getEndTime());
        vo.setSteps(results.stream().map(this::toStepResultVO).toList());
        return vo;
    }

    private RunVO toVO(AtRun r) {
        RunVO vo = new RunVO();
        vo.setId(r.getId());
        vo.setScenarioId(r.getScenarioId());
        vo.setStatus(r.getStatus());
        vo.setFailStepId(r.getFailStepId());
        vo.setErrorMsg(r.getErrorMsg());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setTriggerBy(r.getTriggerBy());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }

    private StepResultVO toStepResultVO(AtStepResult r) {
        StepResultVO vo = new StepResultVO();
        vo.setId(r.getId());
        vo.setStepId(r.getStepId());
        vo.setStepOrder(r.getStepOrder());
        vo.setName(r.getName());
        vo.setStatus(r.getStatus());
        vo.setRequestSnapshot(r.getRequestSnapshot());
        vo.setResponseSnapshot(r.getResponseSnapshot());
        vo.setAssertDetail(r.getAssertDetail());
        vo.setErrorMsg(r.getErrorMsg());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        return vo;
    }
}

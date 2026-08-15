package org.dromara.demo.autotest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.demo.autotest.domain.AtScenario;
import org.dromara.demo.autotest.domain.AtStep;
import org.dromara.demo.autotest.domain.StepParams;
import org.dromara.demo.autotest.dto.ScenarioQuery;
import org.dromara.demo.autotest.dto.ScenarioSaveDTO;
import org.dromara.demo.autotest.dto.StepDTO;
import org.dromara.demo.autotest.mapper.AtScenarioMapper;
import org.dromara.demo.autotest.mapper.AtStepMapper;
import org.dromara.demo.autotest.vo.ScenarioVO;
import org.dromara.demo.autotest.vo.StepVO;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 自动化测试场景服务。
 *
 * @author demo
 * @since 2026-08-15
 */
@Service
public class ScenarioService {

    private final AtScenarioMapper scenarioMapper;
    private final AtStepMapper stepMapper;
    private final ObjectMapper objectMapper;

    public ScenarioService(AtScenarioMapper scenarioMapper, AtStepMapper stepMapper, ObjectMapper objectMapper) {
        this.scenarioMapper = scenarioMapper;
        this.stepMapper = stepMapper;
        this.objectMapper = objectMapper;
    }

    public PageResult<ScenarioVO> page(ScenarioQuery q) {
        long pageNum = q.getPageNum() == null ? 1 : q.getPageNum();
        long pageSize = q.getPageSize() == null ? 10 : q.getPageSize();
        LambdaQueryWrapper<AtScenario> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(q.getName()), AtScenario::getName, q.getName());
        wrapper.orderByDesc(AtScenario::getCreateTime);
        Page<AtScenario> page = scenarioMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ScenarioVO> list = page.getRecords().stream().map(s -> toVO(s, null)).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    public ScenarioVO getById(Long id) {
        AtScenario s = scenarioMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("场景不存在");
        }
        List<AtStep> steps = stepMapper.selectList(new LambdaQueryWrapper<AtStep>()
                .eq(AtStep::getScenarioId, id)
                .orderByAsc(AtStep::getStepOrder));
        return toVO(s, steps);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(ScenarioSaveDTO dto) {
        AtScenario s = new AtScenario();
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        s.setBaseUrl(defaultBaseUrl(dto.getBaseUrl()));
        s.setVariables(writeJson(dto.getVariables()));
        scenarioMapper.insert(s);
        saveSteps(s.getId(), dto.getSteps());
        return s.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ScenarioSaveDTO dto) {
        AtScenario s = scenarioMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("场景不存在");
        }
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        s.setBaseUrl(defaultBaseUrl(dto.getBaseUrl()));
        s.setVariables(writeJson(dto.getVariables()));
        scenarioMapper.updateById(s);
        stepMapper.delete(new LambdaQueryWrapper<AtStep>().eq(AtStep::getScenarioId, id));
        saveSteps(id, dto.getSteps());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AtScenario s = scenarioMapper.selectById(id);
        if (s == null) {
            throw new BusinessException("场景不存在");
        }
        stepMapper.delete(new LambdaQueryWrapper<AtStep>().eq(AtStep::getScenarioId, id));
        scenarioMapper.deleteById(id);
    }

    private void saveSteps(Long scenarioId, List<StepDTO> steps) {
        int order = 1;
        for (StepDTO dto : steps) {
            AtStep step = new AtStep();
            step.setScenarioId(scenarioId);
            step.setStepOrder(order++);
            step.setName(dto.getName());
            step.setParams(writeJson(toParams(dto)));
            stepMapper.insert(step);
        }
    }

    private StepParams toParams(StepDTO dto) {
        StepParams p = new StepParams();
        p.setMethod(dto.getMethod());
        p.setPath(dto.getPath());
        p.setHeaders(dto.getHeaders());
        p.setQuery(dto.getQuery());
        p.setBody(dto.getBody());
        p.setAsserts(dto.getAsserts());
        p.setExtracts(dto.getExtracts());
        return p;
    }

    private String defaultBaseUrl(String baseUrl) {
        return (baseUrl == null || baseUrl.isBlank()) ? "http://localhost:8080" : baseUrl;
    }

    private ScenarioVO toVO(AtScenario s, List<AtStep> steps) {
        ScenarioVO vo = new ScenarioVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setDescription(s.getDescription());
        vo.setBaseUrl(s.getBaseUrl());
        vo.setVariables(readJson(s.getVariables(), new TypeReference<Map<String, Object>>() {}));
        vo.setCreateTime(s.getCreateTime());
        vo.setSteps(steps == null ? null : steps.stream().map(this::toStepVO).toList());
        return vo;
    }

    private StepVO toStepVO(AtStep step) {
        StepParams p = readJson(step.getParams(), StepParams.class);
        StepVO vo = new StepVO();
        vo.setId(step.getId());
        vo.setStepOrder(step.getStepOrder());
        vo.setName(step.getName());
        vo.setMethod(p.getMethod());
        vo.setPath(p.getPath());
        vo.setHeaders(p.getHeaders());
        vo.setQuery(p.getQuery());
        vo.setBody(p.getBody());
        vo.setAsserts(p.getAsserts());
        vo.setExtracts(p.getExtracts());
        return vo;
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON 序列化失败");
        }
    }

    private <T> T readJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON 解析失败");
        }
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON 解析失败");
        }
    }
}

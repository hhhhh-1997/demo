package org.dromara.demo.autotest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import org.dromara.demo.autotest.dto.ScenarioQuery;
import org.dromara.demo.autotest.dto.ScenarioSaveDTO;
import org.dromara.demo.autotest.engine.ApiChainEngine;
import org.dromara.demo.autotest.service.ScenarioService;
import org.dromara.demo.autotest.vo.ScenarioVO;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自动化测试场景接口。
 *
 * @author demo
 * @since 2026-08-15
 */
@RestController
@RequestMapping("/api/autotest/scenario")
public class ScenarioController {

    private final ScenarioService scenarioService;
    private final ApiChainEngine engine;

    public ScenarioController(ScenarioService scenarioService, ApiChainEngine engine) {
        this.scenarioService = scenarioService;
        this.engine = engine;
    }

    @SaCheckPermission("autotest:scenario:list")
    @GetMapping("/page")
    public Result<PageResult<ScenarioVO>> page(ScenarioQuery q) {
        return Result.success(scenarioService.page(q));
    }

    @SaCheckPermission("autotest:scenario:list")
    @GetMapping("/{id}")
    public Result<ScenarioVO> get(@PathVariable Long id) {
        return Result.success(scenarioService.getById(id));
    }

    @SaCheckPermission("autotest:scenario:add")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody ScenarioSaveDTO dto) {
        return Result.success(scenarioService.create(dto));
    }

    @SaCheckPermission("autotest:scenario:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ScenarioSaveDTO dto) {
        scenarioService.update(id, dto);
        return Result.success();
    }

    @SaCheckPermission("autotest:scenario:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        scenarioService.delete(id);
        return Result.success();
    }

    @SaCheckPermission("autotest:scenario:run")
    @PostMapping("/{id}/run")
    public Result<Long> run(@PathVariable Long id) {
        Long triggerBy = StpUtil.getLoginIdAsLong();
        return Result.success(engine.run(id, triggerBy));
    }
}

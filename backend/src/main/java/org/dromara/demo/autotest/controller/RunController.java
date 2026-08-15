package org.dromara.demo.autotest.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.autotest.dto.RunQuery;
import org.dromara.demo.autotest.service.RunService;
import org.dromara.demo.autotest.vo.RunDetailVO;
import org.dromara.demo.autotest.vo.RunVO;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自动化测试运行历史接口。
 *
 * @author demo
 * @since 2026-08-15
 */
@RestController
@RequestMapping("/api/autotest/run")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @SaCheckPermission("autotest:scenario:list")
    @GetMapping("/page")
    public Result<PageResult<RunVO>> page(RunQuery q) {
        return Result.success(runService.page(q));
    }

    @SaCheckPermission("autotest:scenario:list")
    @GetMapping("/{id}")
    public Result<RunDetailVO> get(@PathVariable Long id) {
        return Result.success(runService.getById(id));
    }
}

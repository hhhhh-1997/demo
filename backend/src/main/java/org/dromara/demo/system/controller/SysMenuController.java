package org.dromara.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.domain.SysMenu;
import org.dromara.demo.system.service.SysMenuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @SaCheckPermission("system:menu")
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.listTree());
    }

    @SaCheckLogin
    @GetMapping("/routers")
    public Result<List<SysMenu>> routers() {
        return Result.success(menuService.listRouters());
    }

    @SaCheckPermission("system:menu")
    @PostMapping
    public Result<Void> create(@Validated @RequestBody SysMenu menu) {
        menuService.create(menu);
        return Result.success();
    }

    @SaCheckPermission("system:menu")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody SysMenu menu) {
        menuService.update(id, menu);
        return Result.success();
    }

    @SaCheckPermission("system:menu")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}

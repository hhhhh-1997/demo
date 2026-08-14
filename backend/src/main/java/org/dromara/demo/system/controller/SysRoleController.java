package org.dromara.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.domain.SysRole;
import org.dromara.demo.system.dto.RoleDTO;
import org.dromara.demo.system.service.SysRoleService;
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
 * 角色管理接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @SaCheckPermission("system:role")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @SaCheckPermission("system:role")
    @PostMapping
    public Result<Void> create(@Validated @RequestBody RoleDTO dto) {
        roleService.create(dto);
        return Result.success();
    }

    @SaCheckPermission("system:role")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody RoleDTO dto) {
        roleService.update(id, dto);
        return Result.success();
    }

    @SaCheckPermission("system:role")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }
}

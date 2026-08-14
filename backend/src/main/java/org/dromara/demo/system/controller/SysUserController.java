package org.dromara.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.dto.SysUserDTO;
import org.dromara.demo.system.service.SysUserService;
import org.dromara.demo.system.vo.SysUserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @SaCheckPermission("system:user")
    @GetMapping("/page")
    public Result<PageResult<SysUserVO>> page(@RequestParam long pageNum, @RequestParam long pageSize,
            @RequestParam(required = false) String username) {
        return Result.success(userService.page(pageNum, pageSize, username));
    }

    @SaCheckPermission("system:user")
    @PostMapping
    public Result<Void> create(@Validated(SysUserDTO.Create.class) @RequestBody SysUserDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @SaCheckPermission("system:user")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated(SysUserDTO.Update.class) @RequestBody SysUserDTO dto) {
        userService.update(id, dto);
        return Result.success();
    }

    @SaCheckPermission("system:user")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}

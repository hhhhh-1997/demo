package org.dromara.demo.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.domain.LoginBody;
import org.dromara.demo.system.domain.LoginUser;
import org.dromara.demo.system.domain.SysUser;
import org.dromara.demo.system.mapper.SysUserMapper;
import org.dromara.demo.system.service.StpInterfaceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserMapper userMapper;
    private final StpInterfaceImpl stpInterface;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(SysUserMapper userMapper, StpInterfaceImpl stpInterface) {
        this.userMapper = userMapper;
        this.stpInterface = stpInterface;
    }

    @PostMapping("/login")
    public Result<LoginUser> login(@Valid @RequestBody LoginBody body) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, body.getUsername()));
        if (user == null || !encoder.matches(body.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被停用");
        }
        StpUtil.login(user.getId());
        LoginUser lu = new LoginUser();
        lu.setToken(StpUtil.getTokenValue());
        lu.setUserId(user.getId());
        lu.setUsername(user.getUsername());
        lu.setNickname(user.getNickname());
        lu.setRoles(stpInterface.getRoleList(user.getId(), StpUtil.getLoginType()));
        lu.setPermissions(stpInterface.getPermissionList(user.getId(), StpUtil.getLoginType()));
        return Result.success(lu);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    @GetMapping("/me")
    public Result<LoginUser> me() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        LoginUser lu = new LoginUser();
        lu.setUserId(userId);
        lu.setUsername(user.getUsername());
        lu.setNickname(user.getNickname());
        lu.setRoles(stpInterface.getRoleList(userId, StpUtil.getLoginType()));
        lu.setPermissions(stpInterface.getPermissionList(userId, StpUtil.getLoginType()));
        return Result.success(lu);
    }
}

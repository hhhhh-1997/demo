package org.dromara.demo.system.service;

import cn.dev33.satoken.stp.StpInterface;
import org.dromara.demo.system.mapper.SysMenuMapper;
import org.dromara.demo.system.mapper.SysRoleMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限注入：从用户-角色/角色-菜单实时加载权限与角色。
 *
 * @author demo
 * @since 2026-08-14
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    public StpInterfaceImpl(SysRoleMapper roleMapper, SysMenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return menuMapper.selectPermsByUserId(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleMapper.selectRoleKeysByUserId(Long.valueOf(loginId.toString()));
    }
}

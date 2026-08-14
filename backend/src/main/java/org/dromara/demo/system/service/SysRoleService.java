package org.dromara.demo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.system.domain.SysRole;
import org.dromara.demo.system.domain.SysRoleMenu;
import org.dromara.demo.system.dto.RoleDTO;
import org.dromara.demo.system.mapper.SysRoleMapper;
import org.dromara.demo.system.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务。
 *
 * @author demo
 * @since 2026-08-14
 */
@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SysRoleService(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    /**
     * 查询全部角色。
     *
     * @return 角色列表
     */
    public List<SysRole> list() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSort)
                .orderByAsc(SysRole::getId));
    }

    /**
     * 新增角色并分配菜单权限。
     *
     * @param dto 角色请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(RoleDTO dto) {
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        roleMapper.insert(role);
        assignMenus(role.getId(), dto.getMenuIds());
    }

    /**
     * 修改角色并重建菜单权限。
     *
     * @param id  角色 ID
     * @param dto 角色请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setSort(dto.getSort());
        role.setStatus(dto.getStatus());
        roleMapper.updateById(role);
        assignMenus(id, dto.getMenuIds());
    }

    /**
     * 删除角色及其菜单关联。
     *
     * @param id 角色 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
    }

    /**
     * 分配菜单权限：先清空再批量插入。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
}

package org.dromara.demo.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.demo.common.BusinessException;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.system.domain.SysUser;
import org.dromara.demo.system.domain.SysUserRole;
import org.dromara.demo.system.dto.SysUserDTO;
import org.dromara.demo.system.mapper.SysUserMapper;
import org.dromara.demo.system.mapper.SysUserRoleMapper;
import org.dromara.demo.system.vo.SysUserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户管理服务。
 *
 * @author demo
 * @since 2026-08-14
 */
@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public SysUserService(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 分页查询用户（返回体不含密码）。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊匹配，可空）
     * @return 分页结果
     */
    public PageResult<SysUserVO> page(long pageNum, long pageSize, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysUser::getId, SysUser::getUsername, SysUser::getNickname,
                SysUser::getDeptId, SysUser::getStatus, SysUser::getCreateTime);
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username);
        wrapper.orderByAsc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<SysUserVO> list = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(page.getTotal(), list);
    }

    /**
     * 新增用户（BCrypt 加密密码，并保存角色关联）。
     *
     * @param dto 用户请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserDTO dto) {
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        userMapper.insert(user);
        saveUserRoles(user.getId(), dto.getRoleIds());
    }

    /**
     * 修改用户（密码仅当传入时加密更新，并重建角色关联）。
     *
     * @param id  用户 ID
     * @param dto 用户请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SysUserDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(encoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
        saveUserRoles(id, dto.getRoleIds());
    }

    /**
     * 删除用户（禁止删除当前登录用户）。
     *
     * @param id 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id != null && id.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能删除当前登录用户");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}

package org.dromara.demo.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.demo.system.domain.SysRole;

import java.util.List;

/**
 * 角色 Mapper。
 *
 * @author demo
 * @since 2026-08-14
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户 ID 查询其角色标识集合。
     *
     * @param userId 用户 ID
     * @return 角色标识集合
     */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}

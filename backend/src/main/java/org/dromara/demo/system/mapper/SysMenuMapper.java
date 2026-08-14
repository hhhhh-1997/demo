package org.dromara.demo.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.dromara.demo.system.domain.SysMenu;

import java.util.List;

/**
 * 菜单 Mapper。
 *
 * @author demo
 * @since 2026-08-14
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户 ID 查询其权限标识集合。
     *
     * @param userId 用户 ID
     * @return 权限标识集合
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}

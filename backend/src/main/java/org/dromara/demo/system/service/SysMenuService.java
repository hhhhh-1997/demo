package org.dromara.demo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.demo.system.domain.SysMenu;
import org.dromara.demo.system.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单管理服务。
 *
 * @author demo
 * @since 2026-08-14
 */
@Service
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public SysMenuService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 查询全部菜单并组装为树。
     *
     * @return 菜单树（根节点集合）
     */
    public List<SysMenu> listTree() {
        List<SysMenu> all = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        Map<Long, List<SysMenu>> childrenMap = all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() != 0L)
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : all) {
            menu.setChildren(childrenMap.get(menu.getId()));
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                roots.add(menu);
            }
        }
        return roots;
    }

    /**
     * 新增菜单。
     *
     * @param menu 菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(SysMenu menu) {
        menu.setId(null);
        menuMapper.insert(menu);
    }

    /**
     * 修改菜单。
     *
     * @param id   菜单 ID
     * @param menu 菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SysMenu menu) {
        menu.setId(id);
        menuMapper.updateById(menu);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        menuMapper.deleteById(id);
    }
}

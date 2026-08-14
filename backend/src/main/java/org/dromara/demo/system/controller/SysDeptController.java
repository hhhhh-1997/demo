package org.dromara.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.domain.SysDept;
import org.dromara.demo.system.mapper.SysDeptMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门接口（共享只读）。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/system/dept")
public class SysDeptController {

    private final SysDeptMapper deptMapper;

    public SysDeptController(SysDeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    @SaCheckLogin
    @GetMapping("/tree")
    public Result<List<SysDept>> tree() {
        List<SysDept> all = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSort)
                .orderByAsc(SysDept::getId));
        Map<Long, List<SysDept>> childrenMap = all.stream()
                .filter(d -> d.getParentId() != null && d.getParentId() != 0L)
                .collect(Collectors.groupingBy(SysDept::getParentId));
        List<SysDept> roots = new ArrayList<>();
        for (SysDept dept : all) {
            dept.setChildren(childrenMap.get(dept.getId()));
            if (dept.getParentId() == null || dept.getParentId() == 0L) {
                roots.add(dept);
            }
        }
        return Result.success(roots);
    }
}

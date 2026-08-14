package org.dromara.demo.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.demo.common.Result;
import org.dromara.demo.system.domain.SysDictData;
import org.dromara.demo.system.mapper.SysDictDataMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典接口（共享只读）。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {

    private final SysDictDataMapper dictDataMapper;

    public SysDictController(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @SaCheckLogin
    @GetMapping("/{type}")
    public Result<List<SysDictData>> listByType(@PathVariable String type) {
        List<SysDictData> list = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, type)
                .orderByAsc(SysDictData::getSort)
                .orderByAsc(SysDictData::getId));
        return Result.success(list);
    }
}

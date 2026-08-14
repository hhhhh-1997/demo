package org.dromara.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dromara.demo.domain.Performance;
import org.dromara.demo.dto.PerformanceQuery;
import org.dromara.demo.mapper.PerformanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PerformanceService {

    private final PerformanceMapper mapper;

    public PerformanceService(PerformanceMapper mapper) {
        this.mapper = mapper;
    }

    public List<Performance> list(PerformanceQuery query) {
        return mapper.selectList(buildWrapper(query));
    }

    /**
     * 检索：三个条件空值不拼入；month 为定宽字符串直接等值比较（不转日期）；
     * 排序固定 month DESC, id ASC（等级项不参与排序）。
     */
    QueryWrapper<Performance> buildWrapper(PerformanceQuery query) {
        QueryWrapper<Performance> w = new QueryWrapper<>();
        if (query.getTopDeptId() != null) {
            w.eq("top_dept_id", query.getTopDeptId());
        }
        if (StringUtils.hasText(query.getRole())) {
            w.eq("role", query.getRole());
        }
        if (StringUtils.hasText(query.getMonth())) {
            w.eq("month", query.getMonth());
        }
        w.orderByDesc("month").orderByAsc("id");
        return w;
    }
}

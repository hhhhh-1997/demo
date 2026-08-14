package org.dromara.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dromara.demo.domain.Performance;
import org.dromara.demo.dto.PerformanceQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PerformanceServiceTest {

    private final PerformanceService service = new PerformanceService(mock(org.dromara.demo.mapper.PerformanceMapper.class));

    @Test
    void emptyQuery_noFilters_sortByMonthDescIdAsc() {
        QueryWrapper<Performance> w = service.buildWrapper(new PerformanceQuery());
        String sql = w.getSqlSegment();
        assertFalse(sql.contains("top_dept_id"));
        assertFalse(sql.contains("role"));
        assertFalse(sql.contains("month ="));
        assertTrue(sql.contains("month DESC"));
        assertTrue(sql.contains("id ASC"));
    }

    @Test
    void fullQuery_filtersByAllThreeConditions() {
        PerformanceQuery q = new PerformanceQuery();
        q.setTopDeptId(10);
        q.setRole("dev");
        q.setMonth("2026-05");
        QueryWrapper<Performance> w = service.buildWrapper(q);
        String sql = w.getSqlSegment();
        assertTrue(sql.contains("top_dept_id"));
        assertTrue(sql.contains("role"));
        assertTrue(sql.contains("month"));
    }

    @Test
    void monthFilter_isStringEquality_notDateConversion() {
        PerformanceQuery q = new PerformanceQuery();
        q.setMonth("2026-05");
        QueryWrapper<Performance> w = service.buildWrapper(q);
        String sql = w.getSqlSegment();
        assertTrue(sql.contains("month"));
        assertFalse(sql.contains("STR_TO_DATE"));
        assertFalse(sql.contains("DATE("));
    }
}

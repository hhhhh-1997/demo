package org.dromara.demo.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * 分页响应体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class PageResult<T> {

    private long total;
    private List<T> list;

    public PageResult(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    public long getTotal() { return total; }
    public List<T> getList() { return list; }
}

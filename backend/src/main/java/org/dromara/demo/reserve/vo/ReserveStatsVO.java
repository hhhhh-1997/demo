package org.dromara.demo.reserve.vo;

import org.dromara.demo.project.vo.NameValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统一储备库统计展示对象。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ReserveStatsVO {

    private long total;
    private BigDecimal totalAmountYuan;
    private long pending;
    private long issued;
    private List<NameValue> categoryDist;
    private List<NameValue> deptDist;

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public BigDecimal getTotalAmountYuan() { return totalAmountYuan; }
    public void setTotalAmountYuan(BigDecimal totalAmountYuan) { this.totalAmountYuan = totalAmountYuan; }
    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }
    public long getIssued() { return issued; }
    public void setIssued(long issued) { this.issued = issued; }
    public List<NameValue> getCategoryDist() { return categoryDist; }
    public void setCategoryDist(List<NameValue> categoryDist) { this.categoryDist = categoryDist; }
    public List<NameValue> getDeptDist() { return deptDist; }
    public void setDeptDist(List<NameValue> deptDist) { this.deptDist = deptDist; }
}

package org.dromara.demo.project.vo;

/**
 * 名称-数值统计项（用于图表聚合）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class NameValue {

    private String name;
    private Long value;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}

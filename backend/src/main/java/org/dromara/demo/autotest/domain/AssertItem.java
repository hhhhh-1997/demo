package org.dromara.demo.autotest.domain;

/**
 * 断言项（STATUS / JSON）。
 *
 * @author demo
 * @since 2026-08-15
 */
public class AssertItem {

    private String type;
    private String expected;
    private String jsonPath;
    private String op;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }
    public String getJsonPath() { return jsonPath; }
    public void setJsonPath(String jsonPath) { this.jsonPath = jsonPath; }
    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }
}

package org.dromara.demo.autotest.vo;

import org.dromara.demo.autotest.domain.AssertItem;
import org.dromara.demo.autotest.domain.ExtractItem;

import java.util.List;
import java.util.Map;

/**
 * 步骤展示对象。
 *
 * @author demo
 * @since 2026-08-15
 */
public class StepVO {

    private Long id;
    private Integer stepOrder;
    private String name;
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> query;
    private Map<String, Object> body;
    private List<AssertItem> asserts;
    private List<ExtractItem> extracts;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public Map<String, String> getQuery() { return query; }
    public void setQuery(Map<String, String> query) { this.query = query; }
    public Map<String, Object> getBody() { return body; }
    public void setBody(Map<String, Object> body) { this.body = body; }
    public List<AssertItem> getAsserts() { return asserts; }
    public void setAsserts(List<AssertItem> asserts) { this.asserts = asserts; }
    public List<ExtractItem> getExtracts() { return extracts; }
    public void setExtracts(List<ExtractItem> extracts) { this.extracts = extracts; }
}

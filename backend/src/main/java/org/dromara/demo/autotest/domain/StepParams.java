package org.dromara.demo.autotest.domain;

import java.util.List;
import java.util.Map;

/**
 * 步骤定义 JSON（at_step.params 的内容）。
 *
 * @author demo
 * @since 2026-08-15
 */
public class StepParams {

    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> query;
    private Map<String, Object> body;
    private List<AssertItem> asserts;
    private List<ExtractItem> extracts;

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

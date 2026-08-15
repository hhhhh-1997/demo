package org.dromara.demo.autotest.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 场景展示对象。
 *
 * @author demo
 * @since 2026-08-15
 */
public class ScenarioVO {

    private Long id;
    private String name;
    private String description;
    private String baseUrl;
    private Map<String, Object> variables;
    private LocalDateTime createTime;
    private List<StepVO> steps;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<StepVO> getSteps() { return steps; }
    public void setSteps(List<StepVO> steps) { this.steps = steps; }
}

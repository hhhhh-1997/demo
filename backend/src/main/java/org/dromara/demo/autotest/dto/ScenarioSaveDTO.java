package org.dromara.demo.autotest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * 场景新增/编辑请求体。
 *
 * @author demo
 * @since 2026-08-15
 */
public class ScenarioSaveDTO {

    @NotBlank(message = "场景名不能为空")
    private String name;

    private String description;
    private String baseUrl;
    private Map<String, Object> variables;

    @Valid
    @NotEmpty(message = "步骤不能为空")
    private List<StepDTO> steps;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    public List<StepDTO> getSteps() { return steps; }
    public void setSteps(List<StepDTO> steps) { this.steps = steps; }
}

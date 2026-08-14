package org.dromara.demo.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 项目新增/编辑请求体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ProjectSaveDTO {

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotBlank(message = "二级分类不能为空")
    private String projectType;

    @NotNull(message = "投资金额不能为空")
    @DecimalMin(value = "0.01", message = "投资金额需大于 0")
    private BigDecimal investmentAmount;

    @NotNull(message = "所属单位不能为空")
    private Long deptId;

    private String description;
    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalDate validUntil;

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public BigDecimal getInvestmentAmount() { return investmentAmount; }
    public void setInvestmentAmount(BigDecimal investmentAmount) { this.investmentAmount = investmentAmount; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(LocalDate planStartDate) { this.planStartDate = planStartDate; }
    public LocalDate getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(LocalDate planEndDate) { this.planEndDate = planEndDate; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
}

package org.dromara.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 人员月度绩效实体，对应表 monthly_performance。
 * 关键口径：分值项 NULL = 「无该项工作」、数值 0 = 「有该项工作但得 0 分」，
 * 二者语义相反，实体必须原样保留 null 与 0，不得归并（展示规则在前端）。
 */
@TableName("monthly_performance")
public class Performance {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String realname;
    private String month;
    private BigDecimal taskFinishRate;
    private BigDecimal workEffectRate;
    private Integer workNormativity;
    private BigDecimal learningImprovement;
    private BigDecimal softwareDesign;
    private Integer preSalesSupport;   // 等级项（等级码 1~5）
    private BigDecimal bugCondition;
    private BigDecimal systemDesign;
    private BigDecimal codeReview;
    private Integer testQuality;       // 等级项（等级码 1~5）
    private Integer dept;
    private String role;
    private String roleName;
    private String deptName;
    private Integer topDeptId;
    private String topDeptName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getRealname() { return realname; }
    public void setRealname(String realname) { this.realname = realname; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getTaskFinishRate() { return taskFinishRate; }
    public void setTaskFinishRate(BigDecimal taskFinishRate) { this.taskFinishRate = taskFinishRate; }
    public BigDecimal getWorkEffectRate() { return workEffectRate; }
    public void setWorkEffectRate(BigDecimal workEffectRate) { this.workEffectRate = workEffectRate; }
    public Integer getWorkNormativity() { return workNormativity; }
    public void setWorkNormativity(Integer workNormativity) { this.workNormativity = workNormativity; }
    public BigDecimal getLearningImprovement() { return learningImprovement; }
    public void setLearningImprovement(BigDecimal learningImprovement) { this.learningImprovement = learningImprovement; }
    public BigDecimal getSoftwareDesign() { return softwareDesign; }
    public void setSoftwareDesign(BigDecimal softwareDesign) { this.softwareDesign = softwareDesign; }
    public Integer getPreSalesSupport() { return preSalesSupport; }
    public void setPreSalesSupport(Integer preSalesSupport) { this.preSalesSupport = preSalesSupport; }
    public BigDecimal getBugCondition() { return bugCondition; }
    public void setBugCondition(BigDecimal bugCondition) { this.bugCondition = bugCondition; }
    public BigDecimal getSystemDesign() { return systemDesign; }
    public void setSystemDesign(BigDecimal systemDesign) { this.systemDesign = systemDesign; }
    public BigDecimal getCodeReview() { return codeReview; }
    public void setCodeReview(BigDecimal codeReview) { this.codeReview = codeReview; }
    public Integer getTestQuality() { return testQuality; }
    public void setTestQuality(Integer testQuality) { this.testQuality = testQuality; }
    public Integer getDept() { return dept; }
    public void setDept(Integer dept) { this.dept = dept; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Integer getTopDeptId() { return topDeptId; }
    public void setTopDeptId(Integer topDeptId) { this.topDeptId = topDeptId; }
    public String getTopDeptName() { return topDeptName; }
    public void setTopDeptName(String topDeptName) { this.topDeptName = topDeptName; }
}

package org.dromara.demo.review.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 项目论证记录（4 项检查 + 意见 + 结论）。
 *
 * @author demo
 * @since 2026-08-14
 */
@TableName("project_review")
public class ProjectReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String infoComplete;
    private String threeImportant;
    private String splitProject;
    private String interfaceConfusion;
    private String opinion;
    private String result;
    private Long reviewBy;
    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getInfoComplete() { return infoComplete; }
    public void setInfoComplete(String infoComplete) { this.infoComplete = infoComplete; }
    public String getThreeImportant() { return threeImportant; }
    public void setThreeImportant(String threeImportant) { this.threeImportant = threeImportant; }
    public String getSplitProject() { return splitProject; }
    public void setSplitProject(String splitProject) { this.splitProject = splitProject; }
    public String getInterfaceConfusion() { return interfaceConfusion; }
    public void setInterfaceConfusion(String interfaceConfusion) { this.interfaceConfusion = interfaceConfusion; }
    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Long getReviewBy() { return reviewBy; }
    public void setReviewBy(Long reviewBy) { this.reviewBy = reviewBy; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public Long getCreateBy() { return createBy; }
    public void setCreateBy(Long createBy) { this.createBy = createBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Long getUpdateBy() { return updateBy; }
    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}

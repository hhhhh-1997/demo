package org.dromara.demo.review.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 论证执行请求体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ReviewCommand {

    private String infoComplete;
    private String threeImportant;
    private String splitProject;
    private String interfaceConfusion;
    private String opinion;

    @NotNull(message = "论证结论不能为空")
    private Boolean pass;

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
    public Boolean getPass() { return pass; }
    public void setPass(Boolean pass) { this.pass = pass; }
}

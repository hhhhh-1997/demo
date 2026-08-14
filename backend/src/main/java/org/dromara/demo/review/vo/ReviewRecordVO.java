package org.dromara.demo.review.vo;

import java.time.LocalDateTime;

/**
 * 论证记录展示对象。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ReviewRecordVO {

    private String infoComplete;
    private String threeImportant;
    private String splitProject;
    private String interfaceConfusion;
    private String opinion;
    private String result;
    private LocalDateTime reviewTime;

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
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
}

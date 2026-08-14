package org.dromara.demo.review.vo;

import org.dromara.demo.project.vo.ProjectVO;

/**
 * 论证详情展示对象（项目 + 最新论证记录）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class ReviewDetailVO {

    private ProjectVO project;
    private ReviewRecordVO review;

    public ProjectVO getProject() { return project; }
    public void setProject(ProjectVO project) { this.project = project; }
    public ReviewRecordVO getReview() { return review; }
    public void setReview(ReviewRecordVO review) { this.review = review; }
}

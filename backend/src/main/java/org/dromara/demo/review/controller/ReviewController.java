package org.dromara.demo.review.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.demo.common.PageResult;
import org.dromara.demo.common.Result;
import org.dromara.demo.project.dto.ProjectQuery;
import org.dromara.demo.project.service.ProjectService;
import org.dromara.demo.project.vo.ProjectVO;
import org.dromara.demo.review.dto.ReviewCommand;
import org.dromara.demo.review.vo.ReviewDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 储备项目论证接口。
 *
 * @author demo
 * @since 2026-08-14
 */
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ProjectService projectService;

    public ReviewController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @SaCheckPermission("review:view")
    @GetMapping("/page")
    public Result<PageResult<ProjectVO>> page(ProjectQuery q) {
        return Result.success(projectService.reviewPage(q));
    }

    @SaCheckPermission("review:view")
    @GetMapping("/{id}")
    public Result<ReviewDetailVO> detail(@PathVariable Long id) {
        return Result.success(projectService.reviewDetail(id));
    }

    @SaCheckPermission("review:execute")
    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id, @Validated @RequestBody ReviewCommand cmd) {
        projectService.review(id, cmd);
        return Result.success();
    }

    @SaCheckPermission("review:view")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(projectService.reviewStats());
    }
}

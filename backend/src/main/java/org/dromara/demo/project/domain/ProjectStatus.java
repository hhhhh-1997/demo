package org.dromara.demo.project.domain;

import org.dromara.demo.common.BusinessException;
import java.util.Arrays;

/**
 * 项目状态枚举（7 态，存库值为中文）。
 *
 * @author demo
 * @since 2026-08-14
 */
public enum ProjectStatus {

    DRAFT("草稿"),
    PENDING_REVIEW("待论证"),
    REVIEW_REJECTED("论证退回"),
    PENDING_AUDIT("待审核"),
    AUDIT_REJECTED("审核退回"),
    PENDING_ISSUE("待下达"),
    ISSUED("已下达");

    private final String code;

    ProjectStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ProjectStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new BusinessException("非法项目状态: " + code));
    }
}

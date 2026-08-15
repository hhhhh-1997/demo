package org.dromara.demo.autotest.domain;

/**
 * 步骤结果状态（存库 tinyint）。
 *
 * @author demo
 * @since 2026-08-15
 */
public enum StepStatus {

    PASS(0),
    FAIL(1),
    SKIP(2);

    private final int code;

    StepStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

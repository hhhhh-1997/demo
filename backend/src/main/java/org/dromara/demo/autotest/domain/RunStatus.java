package org.dromara.demo.autotest.domain;

/**
 * 运行状态（存库 tinyint）。
 *
 * @author demo
 * @since 2026-08-15
 */
public enum RunStatus {

    RUNNING(0),
    SUCCESS(1),
    FAILED(2);

    private final int code;

    RunStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

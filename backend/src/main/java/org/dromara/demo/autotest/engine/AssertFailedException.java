package org.dromara.demo.autotest.engine;

/**
 * 断言失败异常：携带已累积断言明细。
 *
 * @author demo
 * @since 2026-08-15
 */
public class AssertFailedException extends RuntimeException {

    private final String detail;

    public AssertFailedException(String detail, String message) {
        super(message);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }
}

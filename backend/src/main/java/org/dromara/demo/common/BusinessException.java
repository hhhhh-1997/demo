package org.dromara.demo.common;

/**
 * 业务异常，携带错误码与提示信息。
 *
 * @author demo
 * @since 2026-08-14
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() { return code; }
}

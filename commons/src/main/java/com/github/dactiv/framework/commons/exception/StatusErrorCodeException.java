package com.github.dactiv.framework.commons.exception;



/**
 * 带 http 状态和错误代码的异常
 *
 * @author maurice.chen
 */
public class StatusErrorCodeException extends ErrorCodeException {

    
    private static final long serialVersionUID = -8343474924487172853L;

    private final int status;

    public StatusErrorCodeException(String message, String errorCode, int status) {
        super(message, errorCode);
        this.status = status;
    }

    public StatusErrorCodeException(String message, Throwable cause, String errorCode, int status) {
        super(message, cause, errorCode);
        this.status = status;
    }

    public StatusErrorCodeException(Throwable cause, String errorCode, int status) {
        super(cause, errorCode);
        this.status = status;
    }

    /**
     * 获取状态值
     *
     * @return 状态值
     */
    public int getStatus() {
        return status;
    }
}

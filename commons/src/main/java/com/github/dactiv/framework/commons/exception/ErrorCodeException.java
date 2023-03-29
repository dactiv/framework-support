package com.github.dactiv.framework.commons.exception;

import java.io.Serial;

/**
 * 带错误代码的异常
 *
 * @author maurice
 */
public class ErrorCodeException extends SystemException {
    @Serial
    private static final long serialVersionUID = 6493664629125445834L;

    /**
     * 默认错误的异常信息
     */
    public static final String DEFAULT_ERROR_MESSAGE = "服务器异常，请稍后再试。";

    /**
     * 默认异常代码
     */
    public static final String DEFAULT_EXCEPTION_CODE = "500";

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 带错误代码的异常
     *
     * @param message   异常信息
     * @param errorCode 错误代码
     */
    public ErrorCodeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 带错误代码的异常
     *
     * @param message   异常信息
     * @param cause     异常类
     * @param errorCode 错误代码
     *
     * @since 1.4
     */
    public ErrorCodeException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 带错误代码的异常
     *
     * @param cause     异常类
     * @param errorCode 错误代码
     *
     * @since 1.4
     */
    public ErrorCodeException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * 错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 断言是否为 true， 如果不为 true 抛出指定代码异常
     *
     * @param value 断言值
     * @param message 错误消息
     * @param code 代码
     */
    public static void isTrue(boolean value, String message, String code) {
        if (!value) {
            throw new  ErrorCodeException(message, code);
        }
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause   异常类
     * @param code    错误代码
     * @param message 错误信息
     */
    public static void throwErrorCodeException(Throwable cause, String code, String message) {
        throw new ErrorCodeException(message, cause, code);
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause 异常类
     * @param code  错误代码
     */
    public static void throwErrorCodeException(Throwable cause, String code) {
        throwErrorCodeException(cause, code, cause.getMessage());
    }

    /**
     * 抛出带有错误代码的异常
     *
     * @param cause 异常类
     */
    public static void throwErrorCodeException(Throwable cause) {

        String message = DEFAULT_ERROR_MESSAGE;
        String executeCode = DEFAULT_EXCEPTION_CODE;

        if (cause instanceof ErrorCodeException || cause instanceof ServiceException) {
            message = cause.getMessage();
            if (cause instanceof ErrorCodeException) {
                executeCode = ((ErrorCodeException) cause).getErrorCode();
            }
        }


        throwErrorCodeException(cause, executeCode, message);
    }
}


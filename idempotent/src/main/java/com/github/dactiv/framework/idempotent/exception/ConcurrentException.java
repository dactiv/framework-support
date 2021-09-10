package com.github.dactiv.framework.idempotent.exception;

import com.github.dactiv.framework.commons.exception.SystemException;

/**
 * 并发异常
 *
 * @author maurice
 */
public class ConcurrentException extends SystemException {

    private static final long serialVersionUID = 2181395755678626994L;

    public ConcurrentException() {
    }

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentException(Throwable cause) {
        super(cause);
    }
}

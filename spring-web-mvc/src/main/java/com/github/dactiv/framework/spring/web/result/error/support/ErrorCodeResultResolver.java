package com.github.dactiv.framework.spring.web.result.error.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.exception.StatusErrorCodeException;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.springframework.http.HttpStatus;

/**
 * 错误代码结果集解析器实现
 *
 * @author maurice.chen
 */
public class ErrorCodeResultResolver implements ErrorResultResolver {

    @Override
    public boolean isSupport(Throwable error) {
        return ErrorCodeException.class.isAssignableFrom(error.getClass());
    }

    @Override
    public RestResult<Object> resolve(Throwable error) {

        ErrorCodeException exception = Casts.cast(error, ErrorCodeException.class);

        RestResult<Object> result = new RestResult<>();

        result.setExecuteCode(exception.getErrorCode());
        result.setMessage(exception.getMessage());
        result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (StatusErrorCodeException.class.isAssignableFrom(error.getClass())) {
            StatusErrorCodeException statusException = Casts.cast(error, StatusErrorCodeException.class);

            result.setStatus(statusException.getStatus());
        }

        return result;
    }
}

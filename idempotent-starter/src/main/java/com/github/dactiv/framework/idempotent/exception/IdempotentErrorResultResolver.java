package com.github.dactiv.framework.idempotent.exception;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.springframework.http.HttpStatus;

/**
 * 幂等错误结果集解析器
 *
 * @author maurice.chen
 */
public class IdempotentErrorResultResolver implements ErrorResultResolver {

    @Override
    public boolean isSupport(Throwable error) {
        return IdempotentException.class.isAssignableFrom(error.getClass());
    }

    @Override
    public RestResult<Object> resolve(Throwable error) {
        IdempotentException exception = Casts.cast(error);

        return RestResult.of(
                exception.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value())
        );
    }
}

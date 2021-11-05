package com.github.dactiv.framework.spring.web.result.error.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 没有指定参数错误解析器
 *
 * @author maurice.chen
 */
public class MissingServletRequestParameterResolver implements ErrorResultResolver {

    public final static String DEFAULT_OBJECT_NAME = "ServletRequestParameter";

    private final String objectName;

    public MissingServletRequestParameterResolver(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public boolean isSupport(Throwable error) {

        return MissingServletRequestParameterException.class.isAssignableFrom(error.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestResult<Object> resolve(Throwable error) {
        MissingServletRequestParameterException exception = Casts.cast(error);
        FieldError fieldError = new FieldError(objectName, exception.getParameterName(), error.getMessage());
        List<Map<String, Object>> data = new LinkedList<>();
        data.add(Casts.convertValue(fieldError, Map.class));

        return RestResult.of(
                "参数丢失",
                HttpStatus.BAD_REQUEST.value(),
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                data
        );
    }
}

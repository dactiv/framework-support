package com.github.dactiv.framework.spring.web.result.error.support;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 参数绑定结果集异常解析器实现
 *
 * @author maurice.chen
 */
public class BindingResultErrorResultResolver implements ErrorResultResolver {

    private static final List<String> DEFAULT_BINDING_RESULT_IGNORE_FIELD = Arrays.asList(
            "rejectedValue",
            "bindingFailure",
            "objectName",
            "source",
            "codes",
            "arguments"
    );

    @Override
    public boolean isSupport(Throwable error) {
        BindingResult bindingResult = extractBindingResult(error);
        return Objects.nonNull(bindingResult) && bindingResult.hasErrors();
    }

    @Override
    public RestResult<Object> resolve(Throwable error) {
        BindingResult bindingResult = extractBindingResult(error);

        List<FieldError> filedErrorResult = bindingResult.getAllErrors()
                .stream()
                .filter(o -> FieldError.class.isAssignableFrom(o.getClass()))
                .map(o -> (FieldError) o)
                .collect(Collectors.toList());

        List<Map<String, Object>> data = new LinkedList<>();

        for (FieldError fieldError : filedErrorResult) {
            //noinspection unchecked
            Map<String, Object> map = Casts.convertValue(fieldError, Map.class);
            map.entrySet().removeIf(i -> DEFAULT_BINDING_RESULT_IGNORE_FIELD.contains(i.getKey()));
            data.add(map);
        }

        return RestResult.of(
                "参数验证不通过",
                HttpStatus.BAD_REQUEST.value(),
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                data
        );
    }

    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return Casts.cast(error);
        } else if (error instanceof MethodArgumentNotValidException) {
            return Casts.cast(error, MethodArgumentNotValidException.class).getBindingResult();
        }
        return null;
    }
}

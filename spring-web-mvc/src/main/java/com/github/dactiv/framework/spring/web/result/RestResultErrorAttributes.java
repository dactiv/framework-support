package com.github.dactiv.framework.spring.web.result;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.web.result.error.ErrorResultResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

/**
 * rest 格式的全局错误实现
 *
 * @author maurice.chen
 */
public class RestResultErrorAttributes extends DefaultErrorAttributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResultErrorAttributes.class);

    public static final String DEFAULT_ERROR_EXECUTE_ATTR_NAME = "REST_ERROR_ATTRIBUTES_EXECUTE";

    public static final List<Class<? extends Exception>> DEFAULT_MESSAGE_EXCEPTION = Arrays.asList(
            SystemException.class,
            ServiceException.class,
            IllegalArgumentException.class
    );

    public static final List<HttpStatus> DEFAULT_HTTP_STATUSES_MESSAGE = Arrays.asList(
            HttpStatus.FORBIDDEN,
            HttpStatus.UNAUTHORIZED
    );

    private final List<ErrorResultResolver> resultResolvers;

    /**
     * 支持的异常抛出消息的类
     */
    private final List<Class<? extends Exception>> supportException;

    /**
     * 支持的 http 响应状态
     */
    private final List<HttpStatus> supportHttpStatus;

    public RestResultErrorAttributes(List<ErrorResultResolver> resultResolvers,
                                     List<Class<? extends Exception>> supportException,
                                     List<HttpStatus> supportHttpStatus) {
        this.resultResolvers = resultResolvers;
        this.supportException = supportException;
        this.supportHttpStatus = supportHttpStatus;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

        HttpStatus status = getStatus(webRequest);

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        RestResult<Object> result = RestResult.of(
                ErrorCodeException.DEFAULT_ERROR_MESSAGE,
                status.value(),
                ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                new LinkedHashMap<>()
        );

        if (supportHttpStatus.contains(status)) {
            result.setMessage(status.getReasonPhrase());
        }

        Throwable error = getError(webRequest);

        if (Objects.nonNull(error)) {
            Optional<ErrorResultResolver> optional = resultResolvers
                    .stream()
                    .filter(r -> r.isSupport(error))
                    .findFirst();

            if (optional.isPresent()) {
                result = optional.get().resolve(error);

                if (supportException.stream().anyMatch(e -> e.isAssignableFrom(error.getClass()))) {
                    result.setMessage(error.getMessage());
                }
            }
            LOGGER.error("服务器异常", error);
        } else {
            LOGGER.error(result.getMessage());
        }

        webRequest.setAttribute(DEFAULT_ERROR_EXECUTE_ATTR_NAME, true, RequestAttributes.SCOPE_REQUEST);

        return Casts.convertValue(result, Map.class);
    }

    /**
     * 获取 http 状态
     *
     * @param webRequest web 请求
     *
     * @return http 状态
     */
    private HttpStatus getStatus(WebRequest webRequest) {

        Integer status = Casts.cast(webRequest.getAttribute(
                "javax.servlet.error.status_code",
                RequestAttributes.SCOPE_REQUEST
        ));

        if (status == null) {
            return null;
        }

        try {
            return HttpStatus.valueOf(status);
        } catch (Exception e) {
            return null;
        }
    }
}

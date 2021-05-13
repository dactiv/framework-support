package com.github.dactiv.framework.spring.web.result;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * rest 响应同一格式实现类
 *
 * @author maurice.chen
 */
@ControllerAdvice
public class RestResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResponseBodyAdvice.class);

    private static final String DEFAULT_CLIENT_NAME = "X-REQUEST-CLIENT";

    public static final String DEFAULT_NOT_FORMAT_ATTR_NAME = "REST_RESULT_NOT_FORMAT";

    private static final List<String> DEFAULT_SUPPORT_CLIENT = Collections.singletonList("SPRING_GATEWAY");

    private static final String DEFAULT_INCLUDE_FIELDS_PARAM_NAME = "includeFields";

    private static final String DEFAULT_EXCLUDE_FIELDS_PARAM_NAME = "excludeFields";

    private List<String> supportClients = DEFAULT_SUPPORT_CLIENT;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        List<String> clients = request.getHeaders().get(DEFAULT_CLIENT_NAME);

        Boolean errorExecute = SpringMvcUtils.getRequestAttribute(RestResultErrorAttributes.DEFAULT_ERROR_EXECUTE_ATTR_NAME);

        Boolean notFormat = SpringMvcUtils.getRequestAttribute(DEFAULT_NOT_FORMAT_ATTR_NAME);

        ServletServerHttpRequest httpRequest = Casts.cast(request);
        ServletServerHttpResponse httpResponse = Casts.cast(response);

        if (notFormat == null || !notFormat) {
            List<String> list = request.getHeaders().get(DEFAULT_NOT_FORMAT_ATTR_NAME);

            if (CollectionUtils.isNotEmpty(list)) {
                notFormat = BooleanUtils.toBoolean(list.iterator().next());
            }
        }

        boolean execute = (notFormat == null || !notFormat) && (errorExecute == null || !errorExecute);

        boolean support = clients != null && clients.stream().anyMatch(this::isSupportClient);

        if (support && execute && MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {

            List<String> excludeFields = getParameterValues(httpRequest.getServletRequest(), DEFAULT_EXCLUDE_FIELDS_PARAM_NAME);
            List<String> includeFields = getParameterValues(httpRequest.getServletRequest(), DEFAULT_INCLUDE_FIELDS_PARAM_NAME);

            HttpStatus status = HttpStatus.valueOf(httpResponse.getServletResponse().getStatus());

            String message = status.getReasonPhrase();
            Object data = body == null ? new LinkedHashMap<>() : body;

            if (body != null && RestResult.Result.class.isAssignableFrom(body.getClass())) {
                RestResult.Result<?> result = Casts.cast(body);

                message = result.getMassage();
                data = result.getData();

            }

            if (Objects.nonNull(data)) {
                data = getFilterPropertyData(data, excludeFields, false);

                data = getFilterPropertyData(data, includeFields, true);
            }

            RestResult<Object> result = new RestResult<>(
                    message,
                    status.value(),
                    RestResult.SUCCESS_EXECUTE_CODE,
                    data
            );

            if (body != null && RestResult.class.isAssignableFrom(body.getClass())) {
                result = Casts.cast(body);
            } else if (HttpStatus.OK == status) {
                result.setExecuteCode(RestResult.SUCCESS_EXECUTE_CODE);
            } else {
                result.setExecuteCode(ErrorCodeException.DEFAULT_EXCEPTION_CODE);
            }

            return result;
        } else {
            return body;
        }

    }

    @SuppressWarnings("unchecked")
    private Object getFilterPropertyData(Object data, List<String> properties, boolean includeOrExclude) {

        Object result = filterDataProperty(
                data,
                properties
                        .stream()
                        .filter(s -> !StringUtils.contains(s, Casts.DEFAULT_POINT_SYMBOL))
                        .collect(Collectors.toList()),
                includeOrExclude
        );

        List<String> nextProperties = properties
                .stream()
                .filter(s -> StringUtils.contains(s, Casts.DEFAULT_POINT_SYMBOL))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(nextProperties)) {

            Map<String, Object> resultMap = Casts.convertValue(result, Map.class);

            Map<String, List<String>> excludeFieldsMap = new LinkedHashMap<>();

            for (String next : nextProperties) {

                String before = StringUtils.substringBefore(next, Casts.DEFAULT_POINT_SYMBOL);

                String after = StringUtils.substringAfter(next, Casts.DEFAULT_POINT_SYMBOL);

                excludeFieldsMap.computeIfAbsent(before, k -> new LinkedList<>()).add(after);

            }

            for (Map.Entry<String, List<String>> entry : excludeFieldsMap.entrySet()) {

                Object o = resultMap.get(entry.getKey());

                if (Objects.isNull(o)) {
                    continue;
                }

                Object newValue = getFilterPropertyData(o, entry.getValue(), includeOrExclude);

                resultMap.put(entry.getKey(), newValue);
            }

            result = resultMap;

        }

        return result;
    }

    /**
     * 过滤数据属性
     *
     * @param data 当前数据
     * @param properties 要过滤的属性名称
     * @param includeOrExclude true 仅仅引入 properties 的属性，false 仅仅去除 properties 的属性
     *
     * @return 新的数据信息
     */
    @SuppressWarnings("unchecked")
    public Object filterDataProperty(Object data, List<String> properties, boolean includeOrExclude) {

        if (CollectionUtils.isEmpty(properties)) {
            return data;
        }

        if (List.class.isAssignableFrom(data.getClass())) {

            List<Object> list = Casts.cast(data);

            return list.stream()
                    .map(o -> filterDataProperty(o, properties, includeOrExclude))
                    .collect(Collectors.toList());

        }

        try {

            Map<String, Object> newData = Casts.convertValue(data, Map.class);

            Map<String, Object> result = new LinkedHashMap<>();

            List<String> keys;

            if (includeOrExclude) {
                keys = newData
                        .keySet()
                        .stream()
                        .filter(properties::contains)
                        .collect(Collectors.toList());
            } else {
                keys = newData
                        .keySet()
                        .stream()
                        .filter(s -> !properties.contains(s))
                        .collect(Collectors.toList());
            }

            for (String key : keys) {
                result.put(key, newData.get(key));
            }

            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("json 处理出现异常", e);
        }

        return data;
    }

    private List<String> getParameterValues(HttpServletRequest request, String paramName) {

        String[] array = request.getParameterValues(paramName);

        List<String> result = new ArrayList<>();

        if (ArrayUtils.isNotEmpty(array)) {

            result = Arrays.asList(array);

            if (1 == array.length) {
                String value = request.getParameter(paramName);
                array = StringUtils.split(StringUtils.trimToEmpty(value), SpringMvcUtils.COMMA_STRING);
                result = Arrays.asList(array);
            }
        }

        return result;
    }

    private boolean isSupportClient(String client) {
        return supportClients.contains(client);
    }

    public void setSupportClients(List<String> supportClients) {
        this.supportClients = supportClients;
    }
}

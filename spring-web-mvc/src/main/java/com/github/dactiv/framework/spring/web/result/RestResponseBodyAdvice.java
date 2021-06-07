package com.github.dactiv.framework.spring.web.result;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.exception.SystemException;
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

        // 获取是否执行控制器的某个方法遇到错误走以下流程，该值在 RestResultErrorAttributes 中设置，
        // 仅仅是为了不跟 RestResultErrorAttributes 冲突而已
        Boolean errorExecute = SpringMvcUtils.getRequestAttribute(RestResultErrorAttributes.DEFAULT_ERROR_EXECUTE_ATTR_NAME);
        // 从请求属性中获取是否不需要格式发，
        Boolean notFormat = SpringMvcUtils.getRequestAttribute(DEFAULT_NOT_FORMAT_ATTR_NAME);

        ServletServerHttpRequest httpRequest = Casts.cast(request);
        ServletServerHttpResponse httpResponse = Casts.cast(response);

        // 如果为空，在 headers 中获取是否存在不需要格式化字段，兼容两个模式
        if (notFormat == null || !notFormat) {
            List<String> list = request.getHeaders().get(DEFAULT_NOT_FORMAT_ATTR_NAME);

            if (CollectionUtils.isNotEmpty(list)) {
                notFormat = BooleanUtils.toBoolean(list.iterator().next());
            }
        }

        // 判断是否执行格式化
        boolean execute = (notFormat == null || !notFormat) && (errorExecute == null || !errorExecute);

        // 判断是否支持格式发，目前针对只有头的 X-REQUEST-CLIENT = supportClients 变量集合才会格式化
        boolean support = clients != null && clients.stream().anyMatch(this::isSupportClient);

        if (support && execute && MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {

            // 获取要忽略的字段集合
            List<FilterProperty> excludeFields = getFilterProperty(httpRequest.getServletRequest(), DEFAULT_EXCLUDE_FIELDS_PARAM_NAME);
            // 获取要仅仅引入的字段集合
            List<FilterProperty> includeFields = getFilterProperty(httpRequest.getServletRequest(), DEFAULT_INCLUDE_FIELDS_PARAM_NAME);

            HttpStatus status = HttpStatus.valueOf(httpResponse.getServletResponse().getStatus());
            // 获取执行状态
            String message = status.getReasonPhrase();
            // 获取实际要响应的 data 内容
            Object data = body == null ? new LinkedHashMap<>() : body;

            RestResult<Object> result = RestResult.of(
                    message,
                    status.value(),
                    RestResult.SUCCESS_EXECUTE_CODE,
                    data
            );

            // 如果响应的 body 有值，并且是 RestResult，获取 data信息，看看是否需要过滤字段
            if (Objects.nonNull(body) && RestResult.class.isAssignableFrom(body.getClass())) {

                result = Casts.cast(body);

                data = result.getData();
            }

            // 如果 data 不为空。直接过滤一次属性内容
            if (Objects.nonNull(data)) {

                Object excludeObject = getFilterPropertyData(data, excludeFields, false);

                Object includeObject = getFilterPropertyData(excludeObject, includeFields, true);

                result.setData(includeObject);
            }

            // 如果没设置执行代码。根据状态值来设置执行代码
            if (Objects.isNull(result.getExecuteCode())){

                if (HttpStatus.OK == status) {
                    result.setExecuteCode(RestResult.SUCCESS_EXECUTE_CODE);
                } else {
                    result.setExecuteCode(ErrorCodeException.DEFAULT_EXCEPTION_CODE);
                }
            }

            return result;
        } else {
            return body;
        }

    }

    /**
     * 获取过滤后的数据
     *
     * @param data 当前数据
     * @param properties 要过滤的属性值
     * @param includeOrExclude true 仅仅引入 properties 参数的数据，false， 去除掉 properties 参数的数据
     *
     * @return 新的数据
     */
    @SuppressWarnings("unchecked")
    private Object getFilterPropertyData(Object data, List<FilterProperty> properties, boolean includeOrExclude) {

        // 如果是集合，从集合里逐个过滤对象信息。
        if (List.class.isAssignableFrom(data.getClass())) {

            List<Object> list = Casts.cast(data);

            return list
                    .stream()
                    .map(o -> getFilterPropertyData(o, properties, includeOrExclude))
                    .collect(Collectors.toList());

        } else {
            // 定义要忽略的字段对象返回值
            Map<String, Object> excludeResult = Casts.convertValue(data, Map.class);
            // 定义要引入的字段对象返回值
            Map<String, Object> includeResult = new LinkedHashMap<>();

            // 循环过滤对象
            for (FilterProperty property : properties) {

                // 如果属性里存在子属性递归一次过滤子属性条件
                if (!property.childrenList.isEmpty()) {
                    // 获取子属性
                    Object child = excludeResult.get(property.name);
                    // 如果为空什么都不做
                    if (Objects.isNull(child)) {
                        continue;
                    }
                    // 递归过滤子属性条件
                    Object childResult = getFilterPropertyData(child, property.childrenList, includeOrExclude);
                    // 得到返回值后覆盖当前 map值
                    excludeResult.put(property.name, childResult);
                } else {
                    // 如果是仅仅引入字段，通过 excludeResult 对象里将值添加到 includeResult，
                    // 否则从 excludeResult 对象里移除对象
                    if (includeOrExclude) {
                        includeResult.put(property.name, excludeResult.get(property.name));
                    } else {
                        excludeResult.remove(property.name);
                    }
                }
            }

            // 如果 includeResult 有值 表示仅仅引入数据操作。返回 includeResult 否则 返回 excludeResult
            return includeResult.isEmpty() ? excludeResult : includeResult;
        }

    }

    /**
     * 获取要过滤的属性
     *
     * @param request http servlet request
     * @param paramName 参数名称
     *
     * @return 过滤属性集合
     */
    private List<FilterProperty> getFilterProperty(HttpServletRequest request, String paramName) {

        String[] array = request.getParameterValues(paramName);

        if (ArrayUtils.isEmpty(array)) {
            return new ArrayList<>();
        }

        return Arrays.stream(array).flatMap(s -> splitFilterProperty(s).stream()).collect(Collectors.toList());
    }

    /**
     * 通过表达式分割过滤属性，表达式格式为"字段名"加逗号","分割，如果对象中存在子对象时，通过自对象名称加 "_[" "]" 符号括住
     *
     * <p>
     *     如: creationTime,id,subObject_[creationTime,id,subObject_[creationTime,id,]]
     * </p>
     *
     * @param value 表达式值
     *
     * @return 过滤属性集合
     */
    private List<FilterProperty> splitFilterProperty(String value) {

        // 定义过滤 builder
        StringBuilder filterBuilder = new StringBuilder(value);

        int end;

        List<FilterProperty> result = new LinkedList<>();
        // 从字符串的左到右去查找有没有 "]" 字符，如果有，优先处理这一类的字符
        while ((end = filterBuilder.indexOf(FilterProperty.FIELD_CLOSE_SUFFIX)) > 0) {
            // 获取字符串的左到右的 "]" 字符位置
            int lastIndex = end + FilterProperty.FIELD_CLOSE_SUFFIX.length();

            // 截取第一段字符
            String s = filterBuilder.substring(0, lastIndex);

            // 在从上一段字符中从右到左去查找 "_[" 字段服位置
            int firstIndex = s.lastIndexOf(FilterProperty.FIELD_OPEN_PREFIX);

            // 如果小于 0 表示表达式错误，少了开始符号，直接报错。
            if(firstIndex < 0) {
                throw new SystemException("过滤属性语法错误");
            }

            String bracketString = filterBuilder.substring(firstIndex + FilterProperty.FIELD_OPEN_PREFIX.length(), end);

            String bracketBeforeString = s.substring(0, s.indexOf(bracketString));

            // 获取开始符号前面的字段名称
            String name = bracketBeforeString.substring(
                    bracketBeforeString.lastIndexOf(SpringMvcUtils.COMMA_STRING) + 1,
                    bracketBeforeString.lastIndexOf(FilterProperty.FIELD_OPEN_PREFIX));

            // 创建过滤条件
            FilterProperty property = new FilterProperty(name);

            // 获取子属性集合
            String childrenString = filterBuilder.substring(firstIndex + FilterProperty.FIELD_OPEN_PREFIX.length(), end);

            // 递归在分割一次属性得到子属性值
            List<FilterProperty> properties = splitFilterProperty(childrenString);
            // 添加子属性集合
            property.childrenList.addAll(properties);
            // 添加返回值
            result.add(property);

            // 将处理过的字符串删除掉，让循环有结束条件
            filterBuilder.delete(s.lastIndexOf(name), lastIndex + SpringMvcUtils.COMMA_STRING.length());
        }

        // 分割字符串
        String[] fields = StringUtils.splitByWholeSeparator(filterBuilder.toString(), SpringMvcUtils.COMMA_STRING);

        List<FilterProperty> item = Arrays
                .stream(fields)
                .filter(StringUtils::isNotEmpty) // 过滤空字符串
                .map(FilterProperty::new)
                .collect(Collectors.toList());

        result.addAll(item);
        // 返回过滤属性
        return result;
    }

    /**
     * 是否支持客户端格式化
     *
     * @param client 客户端
     *
     * @return true 是，否则 false
     */
    private boolean isSupportClient(String client) {
        return supportClients.contains(client);
    }

    /**
     * 设置可支持格式化的客户端信息
     *
     * @param supportClients 客户端信息
     */
    public void setSupportClients(List<String> supportClients) {
        this.supportClients = supportClients;
    }

    /**
     * 过滤属性
     *
     * @author maurice.chen
     */
    private static class FilterProperty {

        // 子属性括号开始符
        public final static String FIELD_OPEN_PREFIX = "(";
        // 子属性括号结束符
        public final static String FIELD_CLOSE_SUFFIX = ")";

        /**
         * 属性名
         */
        public String name;

        /**
         * 子属性集合
         */
        public List<FilterProperty> childrenList = new LinkedList<>();

        public FilterProperty(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name + (childrenList.isEmpty() ? "" : "_" + childrenList);
        }
    }
}

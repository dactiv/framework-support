package com.github.dactiv.framework.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * 转型工具类
 *
 * @author maurice.chen
 **/
@SuppressWarnings("unchecked")
public class Casts {

    private static final Logger LOGGER = LoggerFactory.getLogger(Casts.class);

    /**
     * 默认点符号
     */
    public static final String DEFAULT_POINT_SYMBOL = ".";

    /**
     * 默认等于符号
     */
    public static final String DEFAULT_EQ_SYMBOL = "=";

    /**
     * 默认 and 符号
     */
    public static final String DEFAULT_AND_SYMBOL = "&";

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    public void setObjectMapper(ObjectMapper objectMapper) {
        Casts.objectMapper = objectMapper;
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type 指定类型
     * @param <T> 对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, Class<T> type) {
        return objectMapper.convertValue(value, type);
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type 引用类型
     * @param <T> 对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, TypeReference<T> type) {
        return objectMapper.convertValue(value, type);
    }

    /**
     * 将值转换为 json 字符串
     *
     * @param value 值
     *
     * @return json 字符串
     */
    public static String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 json 字符串转换为指定类型的对象
     *
     * @param json json 字符串
     * @param type 指定类型的对象 class
     * @param <T> 对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, Class<T> type) {

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SystemException(e);
        }

    }

    /**
     * 将 json 字符串转换为指定类型的对象
     *
     * @param json json 字符串
     * @param type 引用类型
     * @param <T> 对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, TypeReference<T> type) {

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new SystemException(e);
        }

    }

    /**
     *
     * 将格式为 name=value&name2=value2&name3=value3 的字符串转型为成 MultiValueMap
     *
     * @param body 数据题
     *
     * @return 转换后的 MultiValueMap 对象
     */
    public static MultiValueMap<String, String> castRequestBodyMap(String body) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        Arrays.stream(StringUtils.split(body, DEFAULT_AND_SYMBOL)).forEach(b -> {
            String key = StringUtils.substringBefore(b, DEFAULT_EQ_SYMBOL);
            String value = StringUtils.substringAfter(b, DEFAULT_EQ_SYMBOL);
            result.add(key, value);
        });

        return result;
    }

    /**
     * 将 MultiValueMap 对象转换为 name=value&name2=value2&name3=value3 格式字符串
     *
     * @param newRequestBody MultiValueMap 对象
     *
     * @return 转换后的字符串
     */
    public static String castRequestBodyMapToString(MultiValueMap<String, String> newRequestBody) {
        StringBuilder result = new StringBuilder();

        newRequestBody
                .forEach((key, value) -> value
                        .forEach(
                                v -> result
                                        .append(key)
                                        .append(DEFAULT_EQ_SYMBOL)
                                        .append(value.size() > 1 ? value : value.get(0))
                                        .append(DEFAULT_AND_SYMBOL)
                        )
                );

        if (result.length() > 1) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * 集合转换器的实现
     *
     * @author maurice.chen
     */
    @SuppressWarnings("rawtypes")
    private static class CollectionConverter implements Converter {

        @Override
        public <T> T convert(Class<T> type, Object value) {
            Class<?> typeInstance;

            if (type.isInterface() && Set.class.isAssignableFrom(type)) {
                typeInstance = LinkedHashSet.class;
            } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))) {
                typeInstance = LinkedList.class;
            } else if (!type.isInterface()) {
                typeInstance = type;
            } else {
                typeInstance = value.getClass();
            }

            Object obj = ClassUtils.newInstance(typeInstance);
            Collection<?> collection = null;

            if (Collection.class.isAssignableFrom(obj.getClass())) {
                collection = (Collection<?>) obj;
            }

            if (collection == null) {
                return type.cast(value);
            }

            if (Collection.class.isAssignableFrom(value.getClass())) {
                Collection values = (Collection) value;
                collection.addAll(values);
            }

            return type.cast(obj);
        }

    }

    static {
        registerDateConverter("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
        registerCollectionConverter();
    }

    /**
     * 注册集合类型的转换器
     */
    private static void registerCollectionConverter() {
        ConvertUtils.register(new CollectionConverter(), Collection.class);
        ConvertUtils.register(new CollectionConverter(), List.class);
        ConvertUtils.register(new CollectionConverter(), ArrayList.class);
        ConvertUtils.register(new CollectionConverter(), LinkedList.class);
        ConvertUtils.register(new CollectionConverter(), Set.class);
        ConvertUtils.register(new CollectionConverter(), HashSet.class);
        ConvertUtils.register(new CollectionConverter(), LinkedHashSet.class);
    }

    /**
     * 注册一个时间类型的转换器,当前默认的格式为：yyyy-MM-dd
     *
     * @param patterns 日期格式
     */
    private static void registerDateConverter(String... patterns) {
        DateConverter dc = new DateConverter();
        dc.setUseLocaleFormat(true);
        dc.setPatterns(patterns);
        ConvertUtils.register(dc, Date.class);
    }

    /**
     * 通过路径获取 map 实体
     *
     * @param source map 数据源
     * @param path 路径，多个以点(".")分割
     *
     * @return map 实体
     */
    public static Map<String, Object> getPathMap(Map<String, Object> source, String path) {

        Map<String, Object> result = new LinkedHashMap<>(source);

        String[] strings = StringUtils.split(path, DEFAULT_POINT_SYMBOL);

        for (String s: strings) {
            result = Casts.cast(result.get(s));
        }

        return result;

    }

    /**
     * 将 value 转型为返回值类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T cast(Object value) {
        if (value == null) {
            return null;
        }
        return (T) cast(value, value.getClass());
    }

    /**
     * 如果 value 不为 null 值，将 value 转型为返回值类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T castIfNotNull(Object value) {
        if (value == null) {
            return null;
        }

        return cast(value);
    }

    /**
     * 将 value 转型为返回值类型
     *
     * @param value 值
     * @param type  值类型 class
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T cast(Object value, Class<T> type) {
        return (T) (value == null ? null : ConvertUtils.convert(value, type));
    }

    /**
     * 讲值转型为 Optional 类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return Optional
     */
    public static <T> Optional<T> castOptional(Object value) {
        return Optional.ofNullable((T) value);
    }

    /**
     * 如果 value 不为 null 值，将 value 转型为返回值类型
     *
     * @param value 值
     * @param type  值类型 class
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T castIfNotNull(Object value, Class<T> type) {

        if (value == null) {
            return null;
        }

        return cast(value, type);
    }

}

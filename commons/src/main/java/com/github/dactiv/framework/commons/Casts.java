package com.github.dactiv.framework.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.annotation.IgnoreField;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转型工具类
 *
 * @author maurice.chen
 **/
@SuppressWarnings("unchecked")
public class Casts {

    /**
     * 默认点符号
     */
    public static final String DEFAULT_DOT_SYMBOL = ".";

    /**
     * 负极符号
     */
    public final static String NEGATIVE_SYMBOL = "-";

    /**
     * 分号
     */
    public final static String SEMICOLON = ";";

    /**
     * 默认等于符号
     */
    public static final String DEFAULT_EQ_SYMBOL = "=";

    /**
     * 默认 and 符号
     */
    public static final String DEFAULT_AND_SYMBOL = "&";

    /**
     * 路径变量开始符号
     */
    public static final String PATH_VARIABLE_SYMBOL_START = "{";

    /**
     * 路径变量结束符号
     */
    public static final String PATH_VARIABLE_SYMBOL_END = "}";

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 设置 jackson objectMapper
     *
     * @param objectMapper objectMapper
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        Casts.objectMapper = objectMapper;
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type  指定类型
     * @param <T>   对象范型实体值
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
     * @param toValueType 指定类型
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, JavaType toValueType) {
        return objectMapper.convertValue(value, toValueType);
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type  引用类型
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, TypeReference<T> type) {
        return objectMapper.convertValue(value, type);
    }

    /**
     * 获取 object mapper
     *
     * @return object mapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
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
     * @param <T>  对象范型实体值
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
     * @param <T>  对象范型实体值
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
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param json  json 字符串
     * @param type  用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  指定类型的对象 class
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, Class<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, JavaType type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>   对象范型实体值
     *
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, TypeReference<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将格式为 http query string 的字符串转型为成 MultiValueMap
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
     * 将 MultiValueMap 对象转换为 name=value&amp;name2=value2&amp;name3=value3 格式字符串
     *
     * @param newRequestBody MultiValueMap 对象
     *
     * @return 转换后的字符串
     */
    public static String castRequestBodyMapToString(MultiValueMap<String, String> newRequestBody) {
        return castRequestBodyMapToString(newRequestBody, (s) -> s);
    }

    /**
     * 将 MultiValueMap 对象转换为 name=value&amp;name2=value2&amp;name3=value3 格式字符串
     *
     * @param newRequestBody MultiValueMap 对象
     * @param function 处理字符串的功能
     *
     * @return 转换后的字符串
     */
    public static String castRequestBodyMapToString(MultiValueMap<String, String> newRequestBody, Function<String, String> function) {
        StringBuilder result = new StringBuilder();

        newRequestBody
                .forEach((key, value) -> value
                        .forEach(
                                v -> result
                                        .append(key)
                                        .append(DEFAULT_EQ_SYMBOL)
                                        .append(value.size() > 1 ? value.stream().map(function).toList() : function.apply(value.get(0)))
                                        .append(DEFAULT_AND_SYMBOL)
                        )
                );

        if (result.length() > 1) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * 将 key 为 String， value 为 String 数组的 map 数据转换成 key 为 String，value 为 object 的 map 对象
     *
     * @param map key 为 String， value 为 String 数组的 map
     *
     * @return key 为 String，value 为 object 的 map 对象
     */
    public static Map<String, Object> castMapToStringObject(Map<String, String[]> map) {
        return castMapToStringObject(map, s -> s);
    }

    /**
     * 将 key 为 String， value 为 String 数组的 map 数据转换成 key 为 String，value 为 object 的 map 对象
     *
     * @param map key 为 String， value 为 String 数组的 map
     * @param function 处理字符串的功能
     *
     * @return key 为 String，value 为 object 的 map 对象
     */
    public static Map<String, Object> castMapToStringObject(Map<String, String[]> map, Function<String, Object> function) {
        Map<String, Object> result = new LinkedHashMap<>();

        map.forEach((k,v) -> {
            if (v.length > 1) {
                result.put(k, Arrays.stream(v).map(function).collect(Collectors.toList()));
            } else {
                result.put(k, function.apply(v[0]));
            }
        });

        return result;
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
     * @param path   路径，多个以点(".")分割
     *
     * @return map 实体
     */
    public static Map<String, Object> getPathMap(Map<String, Object> source, String path) {

        Map<String, Object> result = new LinkedHashMap<>(source);

        String[] strings = StringUtils.split(path, DEFAULT_DOT_SYMBOL);

        for (String s : strings) {
            result = Casts.cast(result.get(s));
        }

        return result;

    }

    /**
     * 将 value 转型为返回值类型
     *
     * @param value 值
     * @param <T>   值类型
     *
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
     *
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
     *
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
     *
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
     *
     * @return 转型后的值
     */
    public static <T> T castIfNotNull(Object value, Class<T> type) {

        if (value == null) {
            return null;
        }

        return cast(value, type);
    }

    /**
     * 设置 url 路径变量值
     *
     * @param url           url 路径
     * @param variableValue url 路径的变量对应值 map
     *
     * @return 新的 url 路径
     */
    public static String setUrlPathVariableValue(String url, Map<String, String> variableValue) {

        String[] vars = StringUtils.substringsBetween(url, PATH_VARIABLE_SYMBOL_START, PATH_VARIABLE_SYMBOL_END);

        List<String> varList = Arrays.asList(vars);

        List<String> existList = varList
                .stream()
                .map(StringUtils::trimToEmpty)
                .filter(variableValue::containsKey)
                .toList();

        String temp = url;

        for (String s : existList) {
            String searchString = PATH_VARIABLE_SYMBOL_START + s + PATH_VARIABLE_SYMBOL_END;
            temp = StringUtils.replace(temp, searchString, variableValue.get(s));
        }

        return temp;
    }

    /**
     * 创建一个新的对象，并将 source 属性内容拷贝到创建的对象中
     *
     * @param source           原数据
     * @param targetClass      新的对象类型
     * @param ignoreProperties 要忽略的属性名称
     *
     * @return 新的对象内容
     */
    public static <T> T of(Object source, Class<T> targetClass, String ...ignoreProperties) {

        T result = ClassUtils.newInstance(targetClass);

        BeanUtils.copyProperties(source, result, ignoreProperties);

        return result;
    }

    public static List<Field> getIgnoreField(Class<?> targetClass) {
        List<Field> fields = new LinkedList<>();
        for (Field o : targetClass.getDeclaredFields()) {
            IgnoreField ignoreField = o.getAnnotation(IgnoreField.class);
            if (Objects.isNull(ignoreField)) {
                continue;
            }
            fields.add(o);
        }
        return fields;
    }

}

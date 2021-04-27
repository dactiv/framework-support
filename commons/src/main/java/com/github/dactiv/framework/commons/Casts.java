package com.github.dactiv.framework.commons;

import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 转型工具类
 *
 * @author maurice.chen
 **/
@SuppressWarnings("unchecked")
public class Casts {

    public static final String DEFAULT_POINT_SYMBOL = ".";

    public static final String DEFAULT_EQ_SYMBOL = "=";

    public static final String DEFAULT_AND_SYMBOL = "&";

    private static final Logger LOGGER = LoggerFactory.getLogger(Casts.class);

    public static MultiValueMap<String, String> castRequestBodyMap(String body) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        Arrays.stream(StringUtils.split(body, DEFAULT_AND_SYMBOL)).forEach(b -> {
            String key = StringUtils.substringBefore(b, DEFAULT_EQ_SYMBOL);
            String value = StringUtils.substringAfter(b, DEFAULT_EQ_SYMBOL);
            result.add(key, value);
        });

        return result;
    }

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

            Object obj = newInstance(typeInstance);
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

    /**
     * 创建一个新实例
     *
     * @param targetClass 目标类型 class
     * @param <T>         目标类型
     * @return 新实例
     */
    public static <T> T newInstance(Class<T> targetClass) {
        try {
            return targetClass.newInstance();
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 设置对象的字段值
     *
     * @param o 对象
     * @param name 字段名称
     * @param value 值
     */
    public static void setFieldValue(Object o, String name, Object value) {
        Field field = ReflectionUtils.findField(o.getClass(), name);

        if (field == null) {
            throw new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + name + "] 字段");
        }

        field.setAccessible(true);

        ReflectionUtils.setField(field, o, value);
    }

    /**
     * 获取对象的字段值
     *
     * @param o 对象
     * @param name 字段名称
     *
     * @return 值
     */
    public static Object getFieldValue(Object o, String name) {
        Field field = ReflectionUtils.findField(o.getClass(), name);

        if (field == null) {
            throw new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + name + "] 字段");
        }

        field.setAccessible(true);

        return ReflectionUtils.getField(field, o);
    }

    /**
     * 通过 get 方法获取字段内容
     *
     * @param o 对象
     * @param name 字段名称
     * @param args 参数
     *
     * @return 字段内容
     */
    public static Object getReadProperty(Object o, String name, Object... args) {

        PropertyDescriptor propertyDescriptor = findPropertyDescriptor(o, name);

        if (!Modifier.isPublic(propertyDescriptor.getReadMethod().getDeclaringClass().getModifiers())) {
            throw new SystemException("[" + o.getClass() + "] 的 [" + name + "] 属性为非 public 属性");
        }

        return invokeMethod(o, propertyDescriptor.getReadMethod(), Arrays.asList(args));
    }

    /**
     * 通过 set 方法设置字段内容
     *
     * @param o 对象
     * @param name 字段名称
     * @param value 值
     */
    public static void setWriteProperty(Object o, String name, Object value) {

        PropertyDescriptor propertyDescriptor = findPropertyDescriptor(o, name);

        if (!Modifier.isPublic(propertyDescriptor.getWriteMethod().getDeclaringClass().getModifiers())) {
            throw new SystemException("[" + o.getClass() + "] 的 [" + name + "] 属性为非 public 属性");
        }

        invokeMethod(o, propertyDescriptor.getWriteMethod(), Collections.singletonList(value));
    }

    /**
     * 执行对象方法
     *
     * @param o 对象
     * @param methodName 方法名称
     * @param args 参数值
     * @param paramTypes 参数类型
     *
     * @return 返回值
     */
    public static Object invokeMethod(Object o, String methodName, List<Object> args, Class<?>... paramTypes) {
        Method method = ReflectionUtils.findMethod(o.getClass(), methodName, paramTypes);

        if (method == null) {
            throw new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + methodName + "] 方法");
        }

        return invokeMethod(o, method, args);
    }

    /**
     * 执行对象方法
     *
     * @param o 对象
     * @param method 方法
     * @param args 参数
     *
     * @return 返回值
     */
    public static Object invokeMethod(Object o, Method method, List<Object> args) {
        method.setAccessible(true);

        return ReflectionUtils.invokeMethod(method, o, args.toArray());
    }

    /**
     * 查找对象中的字段属性说明
     *
     * @param o 对象
     * @param name 字段名称
     *
     * @return 字段属性说明
     */
    private static PropertyDescriptor findPropertyDescriptor(Object o, String name) {
        return Arrays.stream(BeanUtils.getPropertyDescriptors(o.getClass()))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + name + "] 属性"));
    }

}

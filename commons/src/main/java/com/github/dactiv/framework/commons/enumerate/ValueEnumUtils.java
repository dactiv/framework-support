package com.github.dactiv.framework.commons.enumerate;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.annotation.GetValueStrategy;
import com.github.dactiv.framework.commons.exception.ValueEnumNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * key value 枚举 工具类
 *
 * @author maurice.chen
 */
public class ValueEnumUtils {

    /**
     * 通过{@link ValueEnum} 接口子类 class 获取 map 集合
     *
     * @param enumClass key value 枚举
     *
     * @return 以 {@link #getName(ValueEnum)} 为 key {@link ValueEnum#getValue()} 位置的 map 集合
     */
    public static Map<String, Object> castMap(Class<? extends Enum<? extends ValueEnum<?>>> enumClass) {
        return castMap(enumClass, (Object) null);
    }

    /**
     * 通过{@link ValueEnum} 接口子类 class 获取 map 集合
     *
     * @param enumClass key value 枚举
     * @param ignore    要忽略的值
     *
     * @return 以 {@link #getName(ValueEnum)}} 为 key {@link ValueEnum#getValue()} 位置的 map 集合
     */
    public static Map<String, Object> castMap(Class<? extends Enum<? extends ValueEnum<?>>> enumClass, Object... ignore) {

        Map<String, Object> result = new LinkedHashMap<>();

        Enum<? extends ValueEnum<?>>[] enums = enumClass.getEnumConstants();
        if (ArrayUtils.isEmpty(enums)) {
            return result;
        }

        List<Enum<? extends ValueEnum<?>>> values = new LinkedList<>();
        CollectionUtils.addAll(values, enums);

        if (CollectionUtils.isEmpty(values)) {
            return result;
        }

        List<Field> fields = Casts.getIgnoreField(enumClass);
        values.removeIf(s -> fields.stream().anyMatch(f -> StringUtils.equals(f.getName(), s.toString())));

        if (CollectionUtils.isEmpty(values)) {
            return result;
        }

        List<Object> ignoreList = new ArrayList<>(16);

        if (ArrayUtils.isNotEmpty(ignore)) {
            ignoreList = Arrays.asList(ignore);
        }

        List<String> jsonIgnoreList = NameEnumUtils.getJsonIgnoreList(enumClass);

        for (Enum<? extends ValueEnum<?>> o : values) {

            ValueEnum<?> ve = (ValueEnum<?>) o;
            if (jsonIgnoreList.contains(o.toString())) {
                continue;
            }

            Object value = getValueByStrategyAnnotation(ve);
            if (ignoreList.contains(value)) {
                continue;
            }

            result.put(getName(ve), value);
        }

        return result;
    }

    /**
     * 通过值获取 enumClass 的对应名称
     *
     * @param value     值
     * @param enumClass key value 枚举
     *
     * @return 对应的名称值
     */
    public static String getName(Object value, Class<? extends Enum<? extends ValueEnum<?>>> enumClass) {
        return getName(value, enumClass, false);
    }

    /**
     * 通过值获取 enumClass 的对应名称
     *
     * @param value          值
     * @param enumClass      key value 枚举
     * @param ignoreNotFound 如果找不到是否抛出异常, true:抛出，否则 false
     *
     * @return 对应的名称值
     */
    public static String getName(Object value, Class<? extends Enum<? extends ValueEnum<?>>> enumClass, boolean ignoreNotFound) {
        Enum<? extends ValueEnum<?>>[] values = enumClass.getEnumConstants();

        for (Enum<? extends ValueEnum<?>> o : values) {
            ValueEnum<?> ve = Casts.cast(o);
            Object enumValue = getValueByStrategyAnnotation(ve);
            if (Objects.equals(enumValue, value)) {
                return getName(ve);
            }
        }

        throwNotFoundExceptionIfNecessary(value, enumClass, ignoreNotFound);

        return null;
    }

    private static String getName(ValueEnum<?> ve){
        String result = ve.toString();
        if (NameEnum.class.isAssignableFrom(ve.getClass())) {
            NameEnum nameEnum = Casts.cast(ve);
            result = nameEnum.getName();
        }
        return result;
    }

    private static void throwNotFoundExceptionIfNecessary(Object value, Class<? extends Enum<? extends ValueEnum<?>>> enumClass, boolean ignoreNotFound) {
        if (!ignoreNotFound) {
            String msg = enumClass.getName() + " 中找不到值为: " + value + " 的对应名称，" + enumClass.getName() + "信息为:" + castMap(enumClass);
            throw new ValueEnumNotFoundException(msg);
        }
    }

    /**
     * 将值转型为枚举类
     *
     * @param value     值
     * @param enumClass key value 枚举
     * @param <E>       key value 枚举实现类
     *
     * @return key value 枚举实现类
     */
    public static <E extends Enum<? extends ValueEnum<?>>> E parse(Object value, Class<E> enumClass) {
        return parse(value, enumClass, false);
    }

    /**
     * 将值转型为枚举类
     *
     * @param value          值
     * @param enumClass      key value 枚举
     * @param ignoreNotFound 如果找不到是否抛出异常, true:抛出，否则 false
     * @param <E>            key value 枚举实现类
     *
     * @return key value 枚举实现类
     */
    public static <E extends Enum<? extends ValueEnum<?>>> E parse(Object value, Class<E> enumClass, boolean ignoreNotFound) {
        Enum<? extends ValueEnum<?>>[] values = enumClass.getEnumConstants();

        for (Enum<? extends ValueEnum<?>> o : values) {
            ValueEnum<?> ve = Casts.cast(o);
            Object enumValue = getValueByStrategyAnnotation(ve);
            if (Objects.equals(enumValue, value)) {
                return Casts.cast(ve);
            }
        }

        throwNotFoundExceptionIfNecessary(value, enumClass, ignoreNotFound);

        return null;
    }

    public static Object getValueByStrategyAnnotation(ValueEnum<?> valueEnum) {
        GetValueStrategy getValueStrategy = AnnotatedElementUtils.findMergedAnnotation(valueEnum.getClass(), GetValueStrategy.class);
        if (Objects.isNull(getValueStrategy)) {
            return valueEnum.getValue();
        }

        GetValueStrategy.Type type = getValueStrategy.type();

        if (GetValueStrategy.Type.Value.equals(type)) {
            return valueEnum.getValue();
        } else if (GetValueStrategy.Type.Name.equals(type) && NameEnum.class.isAssignableFrom(valueEnum.getClass())) {
            NameEnum nameEnum = Casts.cast(valueEnum);
            return nameEnum.getName();
        }

        Enum<? extends ValueEnum<?>> e = Casts.cast(valueEnum);
        return e.name();
    }

}

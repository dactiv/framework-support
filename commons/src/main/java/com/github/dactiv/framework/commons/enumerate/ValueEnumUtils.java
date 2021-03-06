package com.github.dactiv.framework.commons.enumerate;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.ValueEnumNotFoundException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * key value 枚举 工具类
 *
 * @author maurice.chen
 */
@SuppressWarnings("unchecked")
public class ValueEnumUtils {

    /**
     * 通过{@link ValueEnum} 接口子类 class 获取 map 集合
     *
     * @param enumClass key value 枚举
     * @param <V>       值泛型类型
     *
     * @return 以 {@link #getName(ValueEnum)} 为 key {@link ValueEnum#getValue()} 位置的 map 集合
     */
    public static <V> Map<String, V> castMap(Class<? extends Enum<? extends ValueEnum<V>>> enumClass) {
        return castMap(enumClass, (V) null);
    }

    /**
     * 通过{@link ValueEnum} 接口子类 class 获取 map 集合
     *
     * @param enumClass key value 枚举
     * @param ignore    要忽略的值
     * @param <V>       值泛型类型
     *
     * @return 以 {@link #getName(ValueEnum)}} 为 key {@link ValueEnum#getValue()} 位置的 map 集合
     */
    public static <V> Map<String, V> castMap(Class<? extends Enum<? extends ValueEnum<V>>> enumClass, V... ignore) {

        Map<String, V> result = new LinkedHashMap<>();
        Enum<? extends ValueEnum<V>>[] values = enumClass.getEnumConstants();

        if (ArrayUtils.isEmpty(values)) {
            return result;
        }

        List<V> ignoreList = new ArrayList<>(16);

        if (ArrayUtils.isNotEmpty(ignore)) {
            ignoreList = Arrays.asList(ignore);
        }

        List<String> jsonIgnoreList = NameEnumUtils.getJsonIgnoreList(enumClass);

        for (Enum<? extends ValueEnum<V>> o : values) {

            ValueEnum<V> ve = (ValueEnum<V>) o;
            if (jsonIgnoreList.contains(o.toString())) {
                continue;
            }

            V value = ve.getValue();
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
            if (Objects.equals(ve.getValue(), value)) {
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
            String msg = enumClass.getName() + " 中找不到值为: " + value + " 的对应名称，" + enumClass.getName() +
                    "信息为:" + castMap((Class<? extends Enum<? extends ValueEnum<Object>>>) enumClass);
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
            if (Objects.equals(ve.getValue(), value)) {
                return Casts.cast(ve);
            }
        }

        throwNotFoundExceptionIfNecessary(value, enumClass, ignoreNotFound);

        return null;
    }

}

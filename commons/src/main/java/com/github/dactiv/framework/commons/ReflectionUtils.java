package com.github.dactiv.framework.commons;

import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.objenesis.instantiator.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 反射工具类
 *
 * @author maurice.chen
 */
public class ReflectionUtils {

    /**
     * 设置对象的字段值
     *
     * @param o 对象
     * @param name 字段名称
     * @param value 值
     */
    public static void setFieldValue(Object o, String name, Object value) {
        Field field = org.springframework.util.ReflectionUtils.findField(o.getClass(), name);

        if (field == null) {
            throw new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + name + "] 字段");
        }

        field.setAccessible(true);

        org.springframework.util.ReflectionUtils.setField(field, o, value);
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
        Field field = org.springframework.util.ReflectionUtils.findField(o.getClass(), name);

        if (field == null) {
            throw new SystemException("在 [" + o.getClass() + "] 类中找不到 [" + name + "] 字段");
        }

        field.setAccessible(true);

        return org.springframework.util.ReflectionUtils.getField(field, o);
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
        Method method = org.springframework.util.ReflectionUtils.findMethod(o.getClass(), methodName, paramTypes);

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

        return org.springframework.util.ReflectionUtils.invokeMethod(method, o, args.toArray());
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
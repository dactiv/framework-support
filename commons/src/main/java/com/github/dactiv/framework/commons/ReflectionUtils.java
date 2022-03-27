package com.github.dactiv.framework.commons;

import com.github.dactiv.framework.commons.exception.SystemException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 反射工具类
 *
 * @author maurice.chen
 */
public class ReflectionUtils {

    /**
     * 获取所有字段信息
     *
     * @param targetClass 目标类
     *
     * @return 字段集合
     */
    public static List<Field> findFields(Class<?> targetClass) {

        Field[] fields = targetClass.getDeclaredFields();

        List<Field> result = new LinkedList<>(Arrays.asList(fields));

        if (!targetClass.getName().equals(Object.class.getName())) {
            result.addAll(findFields(targetClass.getSuperclass()));
        }

        return result;
    }

    /**
     * 查找对象中的字段
     *
     * @param o    对象
     * @param name 字段名称
     *
     * @return 字段信息
     */
    public static Field findFiled(Object o, String name) {
        return org.springframework.util.ReflectionUtils.findField(o.getClass(), name);
    }

    /**
     * 获取字段值
     *
     * @param o     对象
     * @param field 字段
     *
     * @return 字段值
     */
    public static Object getFieldValue(Object o, Field field) {
        field.setAccessible(true);

        return org.springframework.util.ReflectionUtils.getField(field, o);
    }

    /**
     * 设置对象的字段值
     *
     * @param o     对象
     * @param name  字段名称
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
     * @param o    对象
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
     * @param o    对象
     * @param name 字段名称
     * @param args 参数
     *
     * @return 字段内容
     */
    public static Object getReadProperty(Object o, String name, Object... args) {

        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, o.getClass());

            if (!Modifier.isPublic(propertyDescriptor.getReadMethod().getDeclaringClass().getModifiers())) {
                throw new SystemException("[" + o.getClass() + "] 的 [" + name + "] 属性为非 public 属性");
            }

            return invokeMethod(o, propertyDescriptor.getReadMethod(), Arrays.asList(args));
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * 通过 set 方法设置字段内容
     *
     * @param o     对象
     * @param name  字段名称
     * @param value 值
     */
    public static void setWriteProperty(Object o, String name, Object value) {

        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, o.getClass());

            if (!Modifier.isPublic(propertyDescriptor.getWriteMethod().getDeclaringClass().getModifiers())) {
                throw new SystemException("[" + o.getClass() + "] 的 [" + name + "] 属性为非 public 属性");
            }

            invokeMethod(o, propertyDescriptor.getWriteMethod(), Collections.singletonList(value));
        } catch (Exception e) {
            throw new SystemException(e);
        }

    }

    /**
     * 执行对象方法
     *
     * @param o          对象
     * @param methodName 方法名称
     * @param args       参数值
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
     * @param o      对象
     * @param method 方法
     * @param args   参数
     *
     * @return 返回值
     */
    public static Object invokeMethod(Object o, Method method, List<Object> args) {
        method.setAccessible(true);
        return org.springframework.util.ReflectionUtils.invokeMethod(method, o, args.toArray());
    }

    /**
     * 获取范型对象类型
     *
     * @param target 目标对象
     * @param index  范型索引位置
     *
     * @return 范型类型
     */
    public static <T> Class<T> getGenericClass(Object target, int index) {
        return getGenericClass(target.getClass(), index);
    }

    /**
     * 获取范型对象类型
     *
     * @param target 目标对象
     * @param index  范型索引位置
     *
     * @return 范型类型
     */
    public static <T> Class<T> getGenericClass(Class<?> target, int index) {

        Type genType = target.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            return Casts.cast(Object.class);
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            throw new RuntimeException("Index outof bounds");
        }

        if (!(params[index] instanceof Class)) {
            return Casts.cast(Object.class);
        }

        return Casts.cast(params[index]);
    }
}

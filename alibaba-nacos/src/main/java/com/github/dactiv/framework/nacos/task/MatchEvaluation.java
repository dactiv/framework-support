package com.github.dactiv.framework.nacos.task;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * 匹配定值类，用于通过某个对象的某个属性与某个值是否相等，在不相等的情况将最新值覆盖到对象中
 *
 * @author maurice.chen
 */
public class MatchEvaluation {

    /**
     * 匹配条件
     */
    private Object match;

    /**
     * 属性名称
     */
    private String propertyName;

    /**
     * 当前值
     */
    private Object value;

    /**
     * 值转换器
     */
    private ValueConvert convert;

    public MatchEvaluation() {
    }

    public MatchEvaluation(Object match, String propertyName, Object value) {
        this(match, propertyName, value, null);
    }

    public MatchEvaluation(Object match, String propertyName, Object value, ValueConvert convert) {
        this.match = match;
        this.propertyName = propertyName;
        this.value = value;
        this.convert = convert;
    }

    /**
     * 获取匹配条件
     *
     * @return 匹配条件
     */
    public Object getMatch() {
        return match;
    }

    /**
     * 设置匹配条件
     *
     * @param match 匹配条件
     */
    public void setMatch(Object match) {
        this.match = match;
    }

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 设置名称
     *
     * @param propertyName 名称
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * 获取当前值
     *
     * @return 当前值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置设置当前值
     *
     * @param value 当前值
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * 定植
     *
     * @param value  值
     * @param target 目标对象
     *
     * @return 定值成功返回 true，否则 false
     */
    public boolean evaluation(Object value, Object target) {

        Field field = ReflectionUtils.findField(target.getClass(), getPropertyName());

        if (Objects.isNull(field)) {
            throw new IllegalArgumentException("在 [" + target.getClass().getName() + "] 中找不到 [" + getPropertyName() + "] 的属性");
        }

        field.setAccessible(true);

        // 反射出的实际值
        Object o = ReflectionUtils.getField(field, target);

        // 如果存在值转换，转换出实际值
        if (Objects.nonNull(convert)) {
            o = convert.convert(o, target);
        }

        // 如果值发生了变更，覆盖原始值和匹配值
        if (!Objects.equals(value, o)) {

            setValue(value);

            ReflectionUtils.setField(field, target, value);

            return true;
        }

        return false;
    }

    /**
     * 值转换器
     *
     * @author maurice.chen
     */
    interface ValueConvert {

        /**
         * 转换值
         *
         * @param value  当前值
         * @param target 目标对象
         *
         * @return 转换后的值
         */
        Object convert(Object value, Object target);
    }
}

package com.github.dactiv.framework.spring.web.query;

/**
 * 属性信息，用户记录对应字段名称和值信息
 *
 * @author maurice.chen
 */
public class Property {

    /**
     * 值字段
     */
    public static final String VALUE_FIELD = "value";

    /**
     * 属性名称字段
     */
    public static final String PROPERTY_NAME = "propertyName";

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 值
     */
    private Object value;

    public Property(String propertyName, Object value) {
        this.propertyName = propertyName;
        this.value = value;
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
     * 设置属性名称
     *
     * @param propertyName 属性名称
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * 获取条件值
     *
     * @return 条件值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 设置条件值
     *
     * @param value 条件值
     */
    public void setValue(Object value) {
        this.value = value;
    }
}

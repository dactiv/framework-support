package com.github.dactiv.framework.spring.web.query.condition;

import com.github.dactiv.framework.spring.web.query.Property;

/**
 * 条件信息, 用于记录一个条件里包含的过滤查询内容
 *
 * @author maurice.chen
 */
public class Condition {

    /**
     * 名称
     */
    private final String name;

    /**
     * 类型
     */
    private final ConditionType type;

    /**
     * 属性
     */
    private final Property property;

    public Condition(String name, ConditionType type, Property property) {
        this.name = name;
        this.type = type;
        this.property = property;
    }

    /**
     * 获取条件名称
     *
     * @return 条件名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取条件类型
     *
     * @return 条件类型
     */
    public ConditionType getType() {
        return type;
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    public Property getProperty() {
        return property;
    }

}

package com.github.dactiv.framework.spring.web.query.condition;

import com.github.dactiv.framework.spring.web.query.Property;

/**
 * 条件信息, 用于记录一个条件里包含的过滤查询内容
 *
 * @param name     名称
 * @param type     类型
 * @param property 属性
 * @author maurice.chen
 */
public record Condition(String name, ConditionType type, Property property) {

    /**
     * 获取条件名称
     *
     * @return 条件名称
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * 获取条件类型
     *
     * @return 条件类型
     */
    @Override
    public ConditionType type() {
        return type;
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    @Override
    public Property property() {
        return property;
    }

}

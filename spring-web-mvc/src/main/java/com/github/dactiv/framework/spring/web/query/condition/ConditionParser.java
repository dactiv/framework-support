package com.github.dactiv.framework.spring.web.query.condition;

import java.util.List;

/**
 * 条件解析，用于解析 http 以 filter 开头的参数得到对应的条件值
 *
 * @author maurice.chen
 */
public interface ConditionParser {

    /**
     * 是否支持参数名
     *
     * @param name 参数名
     *
     * @return true 是，否则 false
     */
    boolean isSupport(String name);

    /**
     * 获取条件
     *
     * @param name  参数名称
     * @param value 参数值
     *
     * @return 条件
     */
    List<Condition> getCondition(String name, List<Object> value);
}

package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

/**
 * 小于等于查询通配符实现
 *
 * @author maurice.chen
 */
public class LteWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    private final static String DEFAULT_WILDCARD_NAME = "lte";

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        queryWrapper.le(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

package com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.filter.Property;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;

/**
 * 小于等于查询通配符实现
 *
 * @author maurice.chen
 */
public class LeWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "le";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        queryWrapper.le(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

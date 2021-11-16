package com.github.dactiv.framework.spring.web.query.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

/**
 * 小于查询通配符实现
 *
 * @author maurice.chen
 */
public class LtWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "lt";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        queryWrapper.lt(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

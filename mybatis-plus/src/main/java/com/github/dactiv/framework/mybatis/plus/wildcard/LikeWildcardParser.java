package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

/**
 * 模糊查询通配符实现
 *
 * @author maurice.chen
 */
public class LikeWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "like";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        queryWrapper.like(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

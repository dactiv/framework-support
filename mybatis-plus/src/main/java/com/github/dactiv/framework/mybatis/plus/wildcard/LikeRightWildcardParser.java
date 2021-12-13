package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

/**
 * 模糊查询的右匹配通配符实现
 *
 * @author maurice.chen
 */
public class LikeRightWildcardParser implements WildcardParser<QueryWrapper<?>> {

    private final static String DEFAULT_WILDCARD_NAME = "rlike";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        queryWrapper.likeRight(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

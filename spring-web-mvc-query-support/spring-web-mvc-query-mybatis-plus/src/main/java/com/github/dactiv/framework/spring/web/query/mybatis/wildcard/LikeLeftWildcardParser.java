package com.github.dactiv.framework.spring.web.query.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

/**
 * 模糊查询的左匹配通配符实现
 *
 * @author maurice.chen
 */
public class LikeLeftWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "llike";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        queryWrapper.likeRight(property.getPropertyName(), property.getValue());
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

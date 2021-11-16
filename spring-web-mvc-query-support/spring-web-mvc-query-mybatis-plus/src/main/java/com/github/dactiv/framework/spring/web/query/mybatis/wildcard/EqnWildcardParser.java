package com.github.dactiv.framework.spring.web.query.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 等于 null 的通配符实现
 *
 * @author maurice.chen
 */
public class EqnWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "eqn";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        if (BooleanUtils.toBoolean(property.getValue().toString())) {
            queryWrapper.isNull(property.getPropertyName());
        }
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

package com.github.dactiv.framework.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.generator.WildcardParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 等于查询通配符实现
 *
 * @author maurice.chen
 */
public class InWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "in";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {

            Iterable<?> iterable = (Iterable<?>) property.getValue();
            List<Object> values = new ArrayList<>();

            iterable.forEach(values::add);
            queryWrapper.in(property.getPropertyName(), values.toArray());
        } else {
            queryWrapper.in(property.getPropertyName(), property.getValue());
        }
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}

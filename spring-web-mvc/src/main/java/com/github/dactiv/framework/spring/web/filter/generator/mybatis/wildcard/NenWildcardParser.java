package com.github.dactiv.framework.spring.web.filter.generator.mybatis.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.spring.web.filter.Property;
import com.github.dactiv.framework.spring.web.filter.generator.WildcardParser;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 不等于 null 的通配符实现
 *
 * @author maurice.chen
 */
public class NenWildcardParser implements WildcardParser {

    private final static String DEFAULT_WILDCARD_NAME = "nen";

    @Override
    public void structure(Property property, QueryWrapper<?> queryWrapper) {
        if(BooleanUtils.toBoolean(property.getValue().toString())) {
            queryWrapper.isNotNull(property.getPropertyName());
        }
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_NAME.equals(condition);
    }
}
